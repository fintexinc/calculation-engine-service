package com.fintex.ce.application.calculation.orchestration;

import com.fintex.ce.application.config.DefaultDataProperties;
import com.fintex.ce.calculation.CalculationOrchestrator;
import com.fintex.ce.calculation.CalculationService;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.BaseCalculationResult;
import com.fintex.ce.model.domain.result.composite.CompositeCalculationResult;
import com.fintex.ce.model.domain.security.SecurityData;
import com.fintex.ce.model.dto.command.CalculationCommand;
import com.fintex.ce.model.dto.command.MultiplePortfoliosCommand;
import com.fintex.ce.model.dto.command.contract.BenchmarkHoldingsProvider;
import com.fintex.ce.model.dto.command.contract.HoldingsProvider;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.exceptions.BasePceException;
import com.fintex.ce.model.error.exceptions.CalculationsFailedException;
import com.fintex.ce.port.webclient.sm.SecurityAttributesFetcher;
import com.fintex.ce.util.FilterUtils;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.enumeration.CompositeSecurityAttribute;
import com.fintex.wm.commons.error.Notification;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

import static java.util.stream.Collectors.groupingBy;

/**
 * Entry point for executing already-validated portfolio calculations. For every request — one command or a composite of
 * several — it resolves each metric's {@link CalculationService}, fetches the Security Master attributes the services
 * declare, prepares each service's typed data via {@link CalculationService#prepareData} and dispatches. Data providers
 * are resolved uniformly: the command's providers when present, otherwise the configured defaults; commands sharing the
 * same resolved providers share their Security Master calls. Portfolio and benchmark holdings are fetched separately —
 * one call covering all required attributes for the portfolio holdings, plus one call covering the attributes required
 * by the benchmark-carrying commands for the benchmark holdings. The two results stay in separate sections of the
 * {@link SecurityData} — the same security may appear in both holdings lists, so the sides never share a lookup table.
 * Composite requests isolate failures per metric: a failing calculation is reported as notifications while the
 * remaining metrics still return their results, and a failed fetch fails only the metrics that depend on it.
 */
@Slf4j
@Service
public class MetricCalculationOrchestrator implements CalculationOrchestrator {

  private static final int SERVER_ERROR_STATUS = 500;

  private final Map<CalculationMetric, CalculationService<?, ?, ?>> serviceMap;
  private final SecurityAttributesFetcher securityAttributesFetcher;
  private final DefaultDataProperties defaultDataProperties;

  public MetricCalculationOrchestrator(
      List<CalculationService<?, ?, ?>> calculationServices,
      SecurityAttributesFetcher securityAttributesFetcher,
      DefaultDataProperties defaultDataProperties) {
    this.serviceMap = calculationServices.stream()
        .collect(Collectors.toMap(CalculationService::getMetric, Function.identity(),
            (existing, duplicate) -> {
              throw ErrorCode.INTERNAL_SERVER_ERROR.toException();
            }));
    this.securityAttributesFetcher = securityAttributesFetcher;
    this.defaultDataProperties = defaultDataProperties;
  }

  @Override
  public BaseCalculationResult calculate(CalculationCommand command) {
    CalculationService<?, ?, ?> service = requireService(command);
    FetchOutcome outcome = fetchForProviderGroup(List.of(command), resolveProviders(command));
    return execute(service, command, outcome.securityDataFor(command, service));
  }

  @Override
  public CompositeCalculationResult calculateAll(List<CalculationCommand> commands) {
    if (CollectionUtils.isEmpty(commands)) {
      throw ErrorCode.METRIC_REQUIRED.toException();
    }
    commands.forEach(this::requireService);

    Map<List<DataProvider>, FetchOutcome> fetchedByProviders = commands.stream()
        .collect(groupingBy(this::resolveProviders, LinkedHashMap::new, Collectors.toList()))
        .entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, group -> fetchForProviderGroup(group.getValue(), group.getKey()),
            (existing, duplicate) -> existing, LinkedHashMap::new));

    List<MetricOutcome> outcomes = commands.stream()
        .map(command -> executeIsolated(command, fetchedByProviders.get(resolveProviders(command))))
        .toList();

    return CompositeCalculationResult.builder()
        .results(outcomes.stream()
            .filter(outcome -> outcome.result() != null)
            .collect(Collectors.toMap(MetricOutcome::metric, MetricOutcome::result,
                (existing, duplicate) -> existing, LinkedHashMap::new)))
        .failures(outcomes.stream()
            .filter(outcome -> outcome.notifications() != null)
            .collect(Collectors.toMap(MetricOutcome::metric, MetricOutcome::notifications,
                (existing, duplicate) -> existing, LinkedHashMap::new)))
        .build();
  }

  private MetricOutcome executeIsolated(CalculationCommand command, FetchOutcome outcome) {
    CalculationMetric metric = command.getMetric();
    CalculationService<?, ?, ?> service = serviceMap.get(metric);
    try {
      return new MetricOutcome(metric, execute(service, command, outcome.securityDataFor(command, service)), null);
    } catch (RuntimeException exception) {
      if (isServerError(exception)) {
        throw exception;
      }
      log.error("Composite calculation failed for metric: {}", metric.getValue(), exception);
      return new MetricOutcome(metric, null, toNotifications(exception));
    }
  }

  /**
   * Decides whether a failure must fail the whole composite request rather than be isolated as one metric's failure.
   * Only a client-level (4xx) domain error is isolated per metric: it is a data or input problem specific to that
   * metric, reported under {@code failures} while the other metrics still return their results. Everything else is a
   * server-side failure that must surface with its own 5xx status instead of being hidden inside an otherwise
   * successful (HTTP 200) composite response — a downstream 5xx from Security Master (bad gateway / unavailable), an
   * aggregate calculation failure carrying any such 5xx, and, critically, any unexpected non-domain runtime exception
   * (e.g. a bug in a calculator) which would otherwise be masked as a per-metric {@code INTERNAL_SERVER_ERROR}
   * notification.
   */
  private static boolean isServerError(RuntimeException exception) {
    if (exception instanceof CalculationsFailedException calculationsFailed) {
      return calculationsFailed.getExceptions().stream().anyMatch(MetricCalculationOrchestrator::isServerErrorStatus);
    }
    if (exception instanceof BasePceException pceException) {
      return isServerErrorStatus(pceException);
    }
    return true;
  }

  private static boolean isServerErrorStatus(BasePceException exception) {
    return exception.getErrorCode().getHttpStatus().getValue() >= SERVER_ERROR_STATUS;
  }

  @SuppressWarnings("unchecked")
  private BaseCalculationResult execute(CalculationService<?, ?, ?> service, CalculationCommand command,
      SecurityData securityData) {
    CalculationService<CalculationCommand, Object, ?> typedService = (CalculationService<CalculationCommand, Object, ?>) service;
    return typedService.perform(command, typedService.prepareData(securityData));
  }

  private CalculationService<?, ?, ?> requireService(CalculationCommand command) {
    if (command == null || command.getMetric() == null) {
      throw ErrorCode.METRIC_REQUIRED.toException();
    }
    CalculationService<?, ?, ?> service = serviceMap.get(command.getMetric());
    if (service == null) {
      throw ErrorCode.UNSUPPORTED_METRIC.toException(command.getMetric().getValue());
    }
    return service;
  }

  private FetchOutcome fetchForProviderGroup(List<CalculationCommand> commands, List<DataProvider> providers) {
    Set<CompositeSecurityAttribute> attributes = commands.stream()
        .flatMap(command -> serviceMap.get(command.getMetric()).requiredAttributes().stream())
        .collect(Collectors.toCollection(() -> EnumSet.noneOf(CompositeSecurityAttribute.class)));
    Set<PortfolioHolding> portfolioHoldings = commands.stream()
        .flatMap(command -> portfolioHoldings(command).stream())
        .filter(Objects::nonNull)
        .collect(Collectors.toCollection(LinkedHashSet::new));
    List<CalculationCommand> benchmarkCommands = commands.stream()
        .filter(MetricCalculationOrchestrator::hasBenchmarkHoldings)
        .toList();
    Set<PortfolioHolding> benchmarkHoldings = benchmarkCommands.stream()
        .flatMap(command -> ((BenchmarkHoldingsProvider) command).getBenchmarkHoldings().stream())
        .filter(Objects::nonNull)
        .collect(Collectors.toCollection(LinkedHashSet::new));
    Set<CompositeSecurityAttribute> benchmarkAttributes = benchmarkCommands.stream()
        .flatMap(command -> serviceMap.get(command.getMetric()).requiredAttributes().stream())
        .collect(Collectors.toCollection(() -> EnumSet.noneOf(CompositeSecurityAttribute.class)));

    SecurityData portfolioData = SecurityData.EMPTY;
    RuntimeException portfolioFailure = null;
    try {
      portfolioData = fetchAttributes(List.copyOf(portfolioHoldings), attributes, providers);
    } catch (RuntimeException exception) {
      log.error("Fetching security attributes {} failed", attributes, exception);
      portfolioFailure = exception;
    }

    SecurityData benchmarkData = SecurityData.EMPTY;
    RuntimeException benchmarkFailure = null;
    // Only worth asking for benchmark attributes while the portfolio fetch is still viable: every command that carries
    // benchmark holdings also requires portfolio attributes, so FetchOutcome#securityDataFor rethrows the portfolio
    // failure before it ever looks at the benchmark side. Fetching anyway would double the load we put on a Security
    // Master that has just failed, retries included, to produce data no caller can reach.
    if (portfolioFailure == null) {
      try {
        benchmarkData = fetchAttributes(List.copyOf(benchmarkHoldings), benchmarkAttributes, providers);
      } catch (RuntimeException exception) {
        log.error("Fetching benchmark attributes {} failed", benchmarkAttributes, exception);
        benchmarkFailure = exception;
      }
    }

    return new FetchOutcome(SecurityData.of(portfolioData.asMap(), benchmarkData.asMap()), portfolioFailure,
        benchmarkFailure);
  }

  private SecurityData fetchAttributes(List<PortfolioHolding> holdings, Set<CompositeSecurityAttribute> attributes,
      List<DataProvider> providers) {
    if (holdings.isEmpty() || attributes.isEmpty()) {
      return SecurityData.EMPTY;
    }
    if (attributes.size() == 1) {
      CompositeSecurityAttribute attribute = attributes.iterator().next();
      return SecurityData.ofAttribute(attribute, securityAttributesFetcher.fetch(holdings, attribute, providers));
    }
    return securityAttributesFetcher.fetch(holdings, attributes, providers);
  }

  private List<DataProvider> resolveProviders(CalculationCommand command) {
    return FilterUtils.getSpecifiedIfEmpty(command.getDataProviders(), defaultDataProperties.getDataProviders());
  }

  private List<PortfolioHolding> portfolioHoldings(CalculationCommand command) {
    if (command instanceof MultiplePortfoliosCommand multiplePortfolios) {
      if (CollectionUtils.isEmpty(multiplePortfolios.getPortfolios())) {
        return List.of();
      }
      return multiplePortfolios.getPortfolios().stream()
          .map(MultiplePortfoliosCommand.Portfolio::getHoldings)
          .filter(Objects::nonNull)
          .flatMap(List::stream)
          .toList();
    }
    if (command instanceof HoldingsProvider provider && provider.getHoldings() != null) {
      return provider.getHoldings();
    }
    return List.of();
  }

  private static boolean hasBenchmarkHoldings(CalculationCommand command) {
    return command instanceof BenchmarkHoldingsProvider provider
        && !CollectionUtils.isEmpty(provider.getBenchmarkHoldings());
  }

  private static List<Notification> toNotifications(RuntimeException exception) {
    if (exception instanceof CalculationsFailedException calculationsFailed) {
      return calculationsFailed.getExceptions().stream()
          .map(MetricCalculationOrchestrator::toNotification)
          .toList();
    }
    if (exception instanceof BasePceException pceException) {
      return List.of(toNotification(pceException));
    }
    return List.of(ErrorCode.INTERNAL_SERVER_ERROR.asNotification());
  }

  private static Notification toNotification(BasePceException exception) {
    return exception.getErrorCode().toNotification(exception.getId(), exception.getFieldName(),
        exception.getMessage(), exception.getMetadata());
  }

  private record FetchOutcome(SecurityData securityData, RuntimeException portfolioFailure,
      RuntimeException benchmarkFailure) {

    SecurityData securityDataFor(CalculationCommand command, CalculationService<?, ?, ?> service) {
      if (portfolioFailure != null && !service.requiredAttributes().isEmpty()) {
        throw portfolioFailure;
      }
      if (benchmarkFailure != null && hasBenchmarkHoldings(command)) {
        throw benchmarkFailure;
      }
      return securityData;
    }
  }

  private record MetricOutcome(CalculationMetric metric, BaseCalculationResult result,
      List<Notification> notifications) {
  }
}
