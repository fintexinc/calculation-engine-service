package ca.tangerine.pce.rest.validation.validators;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import ca.tangerine.pce.model.domain.enumeration.CalculationMetric;
import ca.tangerine.pce.model.domain.holding.CashHolding;
import ca.tangerine.pce.model.domain.holding.GicHolding;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.pce.model.dto.command.CalculationCommand;
import ca.tangerine.pce.model.dto.command.MultiplePortfoliosCommand;
import ca.tangerine.pce.model.dto.command.contract.BenchmarkHoldingsProvider;
import ca.tangerine.pce.model.dto.command.contract.HoldingsProvider;
import ca.tangerine.pce.model.error.ErrorCode;
import ca.tangerine.pce.rest.validation.RequestValidator;
import ca.tangerine.wm.commons.domain.currency.Currency;

/**
 * Validates that every holdings list in a calculation command contains only unique holdings: at most one cash holding
 * per currency, at most one GIC holding per currency, term and interest rate combination, and no repeated security
 * identifiers for all other holding types.
 */
@Component
@Order(430)
public class UniqueHoldingsReqValidator implements RequestValidator {

  @Override
  public List<CalculationMetric> supportedMetrics() {
    return List.of(CalculationMetric.values());
  }

  @Override
  public void validate(CalculationCommand command) {
    if (command instanceof HoldingsProvider provider) {
      validateUniqueHoldings(provider.getHoldings());
    }
    if (command instanceof BenchmarkHoldingsProvider provider) {
      validateUniqueHoldings(provider.getBenchmarkHoldings());
    }
    if (command instanceof MultiplePortfoliosCommand mpc && !CollectionUtils.isEmpty(mpc.getPortfolios())) {
      mpc.getPortfolios().forEach(portfolio -> validateUniqueHoldings(portfolio.getHoldings()));
    }
  }

  private static void validateUniqueHoldings(List<PortfolioHolding> holdings) {
    if (CollectionUtils.isEmpty(holdings)) {
      return;
    }
    validateUniqueCashCurrencies(holdings);
    validateUniqueGicHoldings(holdings);
    validateUniqueSecurityIdentifiers(holdings);
  }

  private static void validateUniqueCashCurrencies(List<PortfolioHolding> holdings) {
    List<CashHolding> cashHoldings = holdings.stream()
        .filter(CashHolding.class::isInstance)
        .map(CashHolding.class::cast)
        .filter(holding -> Objects.nonNull(holding.getCurrency()))
        .toList();
    findFirstDuplicateBy(cashHoldings, CashHolding::getCurrency).ifPresent(holding -> {
      throw ErrorCode.DUPLICATE_CASH_HOLDING.toValidationException(holding.getCurrency());
    });
  }

  private static void validateUniqueGicHoldings(List<PortfolioHolding> holdings) {
    List<GicHolding> gicHoldings = holdings.stream()
        .filter(GicHolding.class::isInstance)
        .map(GicHolding.class::cast)
        .filter(holding -> Objects.nonNull(holding.getCurrency()) && Objects.nonNull(holding.getTerm())
            && Objects.nonNull(holding.getClientIntRate()))
        .toList();
    findFirstDuplicateBy(gicHoldings, GicHoldingKey::of).ifPresent(holding -> {
      throw ErrorCode.DUPLICATE_GIC_HOLDING.toValidationException(holding.getCurrency(), holding.getTerm(),
          holding.getClientIntRate());
    });
  }

  private static void validateUniqueSecurityIdentifiers(List<PortfolioHolding> holdings) {
    List<PortfolioHolding> securityHoldings = holdings.stream()
        .filter(holding -> !(holding instanceof CashHolding) && !(holding instanceof GicHolding))
        .filter(holding -> Objects.nonNull(holding.getSecurityIdentifier()))
        .toList();
    findFirstDuplicateBy(securityHoldings, PortfolioHolding::getSecurityIdentifier).ifPresent(holding -> {
      throw ErrorCode.DUPLICATE_HOLDING.toValidationExceptionForHolding(holding);
    });
  }

  private static <T, K> Optional<T> findFirstDuplicateBy(List<T> items, Function<T, K> keyExtractor) {
    Set<K> seen = new HashSet<>();
    return items.stream()
        .filter(item -> !seen.add(keyExtractor.apply(item)))
        .findFirst();
  }

  private record GicHoldingKey(Currency currency, BigDecimal term, BigDecimal clientIntRate) {

    private static GicHoldingKey of(GicHolding holding) {
      return new GicHoldingKey(holding.getCurrency(), normalize(holding.getTerm()),
          normalize(holding.getClientIntRate()));
    }

    private static BigDecimal normalize(BigDecimal value) {
      return value == null ? null : value.stripTrailingZeros();
    }
  }
}
