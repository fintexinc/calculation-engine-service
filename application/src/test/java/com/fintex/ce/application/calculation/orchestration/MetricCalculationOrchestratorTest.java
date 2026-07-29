package com.fintex.ce.application.calculation.orchestration;

import com.fintex.ce.application.config.DefaultDataProperties;
import com.fintex.ce.calculation.CalculationService;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.BaseCalculationResult;
import com.fintex.ce.model.domain.result.composite.CompositeCalculationResult;
import com.fintex.ce.model.domain.security.SecurityData;
import com.fintex.ce.model.dto.command.CalculationCommand;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.exceptions.CalculationException;
import com.fintex.ce.model.error.exceptions.ExternalServiceUnavailableException;
import com.fintex.ce.port.webclient.sm.SecurityAttributesFetcher;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.enumeration.CompositeSecurityAttribute;
import com.fintex.wm.commons.domain.enumeration.Country;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MetricCalculationOrchestratorTest {

  private static final List<DataProvider> DEFAULT_PROVIDERS = List.of(DataProvider.MORNINGSTAR, DataProvider.FMP);

  @Mock
  private SecurityAttributesFetcher securityAttributesFetcher;

  @Mock
  private CalculationService<CalculationCommand, Object, BaseCalculationResult> assetAllocationService;

  @Mock
  private CalculationService<CalculationCommand, Object, BaseCalculationResult> sharpeRatioService;

  private MetricCalculationOrchestrator orchestrator;

  @BeforeEach
  void setUp() {
    lenient().when(assetAllocationService.getMetric()).thenReturn(CalculationMetric.ASSET_ALLOCATIONS);
    lenient().when(assetAllocationService.requiredAttributes()).thenReturn(List.of(
        CompositeSecurityAttribute.ASSET_ALLOCATION, CompositeSecurityAttribute.GEOGRAPHY));
    lenient().when(assetAllocationService.prepareData(any())).thenAnswer(invocation -> invocation.getArgument(0));
    lenient().when(sharpeRatioService.getMetric()).thenReturn(CalculationMetric.SHARPE_RATIO);
    lenient().when(sharpeRatioService.requiredAttributes())
        .thenReturn(List.of(CompositeSecurityAttribute.MONTHLY_RETURNS));
    lenient().when(sharpeRatioService.prepareData(any())).thenAnswer(invocation -> invocation.getArgument(0));
    orchestrator = new MetricCalculationOrchestrator(
        List.<CalculationService<?, ?, ?>>of(assetAllocationService, sharpeRatioService),
        securityAttributesFetcher,
        new DefaultDataProperties(DEFAULT_PROVIDERS));
  }

  @Test
  void shouldFetchViaSingleAttributeCall_whenServiceRequiresOneAttribute() {
    PeriodCommand command = sharpeCommand(null);
    PortfolioHolding holding = command.getHoldings().getFirst();
    BaseCalculationResult expected = new BaseCalculationResult() {};
    when(securityAttributesFetcher.fetch(anyList(), eq(CompositeSecurityAttribute.MONTHLY_RETURNS),
        eq(DEFAULT_PROVIDERS))).thenReturn(Map.of(holding, "returns-data"));
    when(sharpeRatioService.perform(eq(command), any())).thenReturn(expected);

    BaseCalculationResult result = orchestrator.calculate(command);

    assertThat(result).isSameAs(expected);
    verify(securityAttributesFetcher, never()).fetch(anyList(), anyCollection(), anyList());
    ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
    verify(sharpeRatioService).perform(eq(command), captor.capture());
    assertThat(((SecurityData) captor.getValue()).<String>get(CompositeSecurityAttribute.MONTHLY_RETURNS))
        .containsEntry(holding, "returns-data");
  }

  @Test
  void shouldFetchViaCompositeCall_whenServiceRequiresSeveralAttributes() {
    PortfolioHoldingsCommand command = allocationCommand();
    PortfolioHolding holding = command.getHoldings().getFirst();
    BaseCalculationResult expected = new BaseCalculationResult() {};
    when(securityAttributesFetcher.fetch(anyList(), anyCollection(), eq(DEFAULT_PROVIDERS)))
        .thenReturn(SecurityData.builder()
            .with(CompositeSecurityAttribute.ASSET_ALLOCATION, Map.of(holding, "allocation-data"))
            .with(CompositeSecurityAttribute.GEOGRAPHY, Map.of(holding, "geography-data"))
            .build());
    when(assetAllocationService.perform(eq(command), any())).thenReturn(expected);

    BaseCalculationResult result = orchestrator.calculate(command);

    assertThat(result).isSameAs(expected);
    verify(securityAttributesFetcher).fetch(anyList(),
        argThat((Collection<CompositeSecurityAttribute> attributes) -> attributes.containsAll(
            Set.of(CompositeSecurityAttribute.ASSET_ALLOCATION, CompositeSecurityAttribute.GEOGRAPHY))),
        eq(DEFAULT_PROVIDERS));
    verify(securityAttributesFetcher, never()).fetch(anyList(), any(CompositeSecurityAttribute.class), anyList());
    ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
    verify(assetAllocationService).perform(eq(command), captor.capture());
    SecurityData passed = (SecurityData) captor.getValue();
    assertThat(passed.<String>get(CompositeSecurityAttribute.ASSET_ALLOCATION))
        .containsExactly(Map.entry(holding, "allocation-data"));
    assertThat(passed.<String>get(CompositeSecurityAttribute.GEOGRAPHY))
        .containsExactly(Map.entry(holding, "geography-data"));
  }

  @Test
  void shouldFetchBenchmarkReturnsSeparately_whenCommandCarriesBenchmarkHoldings() {
    PortfolioHolding benchmarkHolding = holding("BENCH");
    PeriodCommand command = sharpeCommand(benchmarkHolding);
    PortfolioHolding portfolioHolding = command.getHoldings().getFirst();
    when(securityAttributesFetcher.fetch(eq(List.of(portfolioHolding)),
        eq(CompositeSecurityAttribute.MONTHLY_RETURNS), eq(DEFAULT_PROVIDERS)))
        .thenReturn(Map.of(portfolioHolding, "portfolio-returns"));
    when(securityAttributesFetcher.fetch(eq(List.of(benchmarkHolding)),
        eq(CompositeSecurityAttribute.MONTHLY_RETURNS), eq(DEFAULT_PROVIDERS)))
        .thenReturn(Map.of(benchmarkHolding, "benchmark-returns"));
    when(sharpeRatioService.perform(eq(command), any())).thenReturn(new BaseCalculationResult() {});

    orchestrator.calculate(command);

    ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
    verify(sharpeRatioService).perform(eq(command), captor.capture());
    SecurityData securityData = (SecurityData) captor.getValue();
    assertThat(securityData.<String>get(CompositeSecurityAttribute.MONTHLY_RETURNS))
        .containsEntry(portfolioHolding, "portfolio-returns")
        .doesNotContainKey(benchmarkHolding);
    assertThat(securityData.<String>getBenchmark(CompositeSecurityAttribute.MONTHLY_RETURNS))
        .containsEntry(benchmarkHolding, "benchmark-returns")
        .doesNotContainKey(portfolioHolding);
  }

  @Test
  void shouldFetchAllRequiredAttributesForBenchmark_whenBenchmarkServiceRequiresSeveral() {
    when(sharpeRatioService.requiredAttributes()).thenReturn(List.of(
        CompositeSecurityAttribute.MONTHLY_RETURNS, CompositeSecurityAttribute.FEES));
    PortfolioHolding benchmarkHolding = holding("BENCH");
    PeriodCommand command = sharpeCommand(benchmarkHolding);
    PortfolioHolding portfolioHolding = command.getHoldings().getFirst();
    when(securityAttributesFetcher.fetch(eq(List.of(portfolioHolding)), anyCollection(), eq(DEFAULT_PROVIDERS)))
        .thenReturn(SecurityData.ofAttribute(CompositeSecurityAttribute.MONTHLY_RETURNS,
            Map.of(portfolioHolding, "portfolio-returns")));
    when(securityAttributesFetcher.fetch(eq(List.of(benchmarkHolding)), anyCollection(), eq(DEFAULT_PROVIDERS)))
        .thenReturn(SecurityData.builder()
            .with(CompositeSecurityAttribute.MONTHLY_RETURNS, Map.of(benchmarkHolding, "benchmark-returns"))
            .with(CompositeSecurityAttribute.FEES, Map.of(benchmarkHolding, "benchmark-fees"))
            .build());
    when(sharpeRatioService.perform(eq(command), any())).thenReturn(new BaseCalculationResult() {});

    orchestrator.calculate(command);

    verify(securityAttributesFetcher).fetch(eq(List.of(benchmarkHolding)),
        argThat((Collection<CompositeSecurityAttribute> attributes) -> attributes.containsAll(
            Set.of(CompositeSecurityAttribute.MONTHLY_RETURNS, CompositeSecurityAttribute.FEES))),
        eq(DEFAULT_PROVIDERS));
    ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
    verify(sharpeRatioService).perform(eq(command), captor.capture());
    SecurityData securityData = (SecurityData) captor.getValue();
    assertThat(securityData.<String>get(CompositeSecurityAttribute.MONTHLY_RETURNS))
        .containsEntry(portfolioHolding, "portfolio-returns");
    assertThat(securityData.<String>getBenchmark(CompositeSecurityAttribute.MONTHLY_RETURNS))
        .containsEntry(benchmarkHolding, "benchmark-returns");
    assertThat(securityData.<String>getBenchmark(CompositeSecurityAttribute.FEES))
        .containsEntry(benchmarkHolding, "benchmark-fees");
  }

  @Test
  void shouldUseCommandProviders_whenCommandSpecifiesThem() {
    PeriodCommand command = sharpeCommand(null);
    command.setDataProviders(List.of(DataProvider.FMP));
    PortfolioHolding holding = command.getHoldings().getFirst();
    BaseCalculationResult expected = new BaseCalculationResult() {};
    when(securityAttributesFetcher.fetch(anyList(), eq(CompositeSecurityAttribute.MONTHLY_RETURNS),
        eq(List.of(DataProvider.FMP)))).thenReturn(Map.of(holding, "returns-data"));
    when(sharpeRatioService.perform(eq(command), any())).thenReturn(expected);

    BaseCalculationResult result = orchestrator.calculate(command);

    assertThat(result).isSameAs(expected);
    verify(securityAttributesFetcher).fetch(anyList(), eq(CompositeSecurityAttribute.MONTHLY_RETURNS),
        eq(List.of(DataProvider.FMP)));
    ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
    verify(sharpeRatioService).perform(eq(command), captor.capture());
    assertThat(((SecurityData) captor.getValue()).<String>get(CompositeSecurityAttribute.MONTHLY_RETURNS))
        .containsExactly(Map.entry(holding, "returns-data"));
  }

  @Test
  void shouldSkipFetchingAndPassEmptyData_whenServiceRequiresNoAttributes() {
    when(assetAllocationService.requiredAttributes()).thenReturn(List.of());
    PortfolioHoldingsCommand command = allocationCommand();
    BaseCalculationResult expected = new BaseCalculationResult() {};
    when(assetAllocationService.perform(eq(command), any())).thenReturn(expected);

    BaseCalculationResult result = orchestrator.calculate(command);

    assertThat(result).isSameAs(expected);
    verify(securityAttributesFetcher, never()).fetch(anyList(), anyCollection(), anyList());
    verify(securityAttributesFetcher, never()).fetch(anyList(), any(CompositeSecurityAttribute.class), anyList());
    ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
    verify(assetAllocationService).perform(eq(command), captor.capture());
    SecurityData passed = (SecurityData) captor.getValue();
    assertThat(passed.asMap()).isEmpty();
    assertThat(passed.<Object>getBenchmark(CompositeSecurityAttribute.MONTHLY_RETURNS)).isEmpty();
  }

  @Test
  void shouldThrowException_whenMetricHasNoRegisteredService() {
    PortfolioHoldingsCommand command = allocationCommand();
    command.setMetric(CalculationMetric.YIELD);

    assertThatThrownBy(() -> orchestrator.calculate(command))
        .isInstanceOf(CalculationException.class)
        .hasMessageContaining("yield");
  }

  @Test
  void shouldThrowException_whenCommandHasNoMetric() {
    PortfolioHoldingsCommand command = allocationCommand();
    command.setMetric(null);

    assertThatThrownBy(() -> orchestrator.calculate(command))
        .isInstanceOf(CalculationException.class);
  }

  @ParameterizedTest
  @MethodSource("emptyCommandLists")
  void shouldThrowMetricRequired_whenCommandsAreEmptyOrNull(List<CalculationCommand> commands) {
    assertThatThrownBy(() -> orchestrator.calculateAll(commands))
        .isInstanceOf(CalculationException.class)
        .satisfies(exception -> assertThat(((CalculationException) exception).getErrorCode())
            .isEqualTo(ErrorCode.METRIC_REQUIRED));
  }

  static Stream<Arguments> emptyCommandLists() {
    return Stream.of(Arguments.of((List<CalculationCommand>) null), Arguments.of(List.of()));
  }

  @Test
  void shouldReturnResultPerMetric_whenAllServicesSucceed() {
    PortfolioHoldingsCommand allocationCommand = allocationCommand();
    PeriodCommand sharpeCommand = sharpeCommand(null);
    BaseCalculationResult allocationResult = new BaseCalculationResult() {};
    BaseCalculationResult sharpeResult = new BaseCalculationResult() {};
    when(securityAttributesFetcher.fetch(anyList(), anyCollection(), anyList())).thenReturn(SecurityData.EMPTY);
    when(assetAllocationService.perform(eq(allocationCommand), any())).thenReturn(allocationResult);
    when(sharpeRatioService.perform(eq(sharpeCommand), any())).thenReturn(sharpeResult);

    CompositeCalculationResult result = orchestrator.calculateAll(List.of(allocationCommand, sharpeCommand));

    assertThat(result.getResults())
        .containsEntry(CalculationMetric.ASSET_ALLOCATIONS, allocationResult)
        .containsEntry(CalculationMetric.SHARPE_RATIO, sharpeResult);
    assertThat(result.getFailures()).isEmpty();
  }

  @Test
  void shouldPropagateAsServiceUnavailable_whenSingleCommandFetchFails() {
    PeriodCommand command = sharpeCommand(null);
    when(securityAttributesFetcher.fetch(anyList(), any(CompositeSecurityAttribute.class), anyList()))
        .thenThrow(new ExternalServiceUnavailableException("Security Master"));

    assertThatThrownBy(() -> orchestrator.calculate(command))
        .isInstanceOf(ExternalServiceUnavailableException.class)
        .satisfies(exception -> assertThat(((ExternalServiceUnavailableException) exception).getErrorCode())
            .isEqualTo(ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE));
  }

  @Test
  void shouldPropagateAsServiceUnavailable_whenAttributeFetchFails() {
    PortfolioHoldingsCommand allocationCommand = allocationCommand();
    PeriodCommand sharpeCommand = sharpeCommand(null);
    when(securityAttributesFetcher.fetch(anyList(), anyCollection(), anyList()))
        .thenThrow(new ExternalServiceUnavailableException("Security Master"));

    assertThatThrownBy(() -> orchestrator.calculateAll(List.of(allocationCommand, sharpeCommand)))
        .isInstanceOf(ExternalServiceUnavailableException.class)
        .satisfies(exception -> assertThat(((ExternalServiceUnavailableException) exception).getErrorCode())
            .isEqualTo(ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE));
  }

  @Test
  void shouldPropagateAsServiceUnavailable_whenBenchmarkFetchFails() {
    PortfolioHoldingsCommand allocationCommand = allocationCommand();
    PeriodCommand sharpeCommand = sharpeCommand(holding("BENCH"));
    when(securityAttributesFetcher.fetch(anyList(), anyCollection(), anyList())).thenReturn(SecurityData.EMPTY);
    when(securityAttributesFetcher.fetch(anyList(), eq(CompositeSecurityAttribute.MONTHLY_RETURNS), anyList()))
        .thenThrow(new ExternalServiceUnavailableException("Security Master"));

    assertThatThrownBy(() -> orchestrator.calculateAll(List.of(allocationCommand, sharpeCommand)))
        .isInstanceOf(ExternalServiceUnavailableException.class);
  }

  @Test
  void shouldIsolatePerMetric_whenServiceThrowsClientError() {
    PortfolioHoldingsCommand allocationCommand = allocationCommand();
    PeriodCommand sharpeCommand = sharpeCommand(null);
    BaseCalculationResult allocationResult = new BaseCalculationResult() {};
    when(securityAttributesFetcher.fetch(anyList(), anyCollection(), anyList())).thenReturn(SecurityData.EMPTY);
    when(assetAllocationService.perform(eq(allocationCommand), any())).thenReturn(allocationResult);
    when(sharpeRatioService.perform(eq(sharpeCommand), any()))
        .thenThrow(ErrorCode.MISSING_MONTHLY_RETURNS.toException("XIU"));

    CompositeCalculationResult result = orchestrator.calculateAll(List.of(allocationCommand, sharpeCommand));

    assertThat(result.getResults())
        .containsOnlyKeys(CalculationMetric.ASSET_ALLOCATIONS)
        .containsEntry(CalculationMetric.ASSET_ALLOCATIONS, allocationResult);
    assertThat(result.getFailures()).containsOnlyKeys(CalculationMetric.SHARPE_RATIO);
    assertThat(result.getFailures().get(CalculationMetric.SHARPE_RATIO))
        .singleElement()
        .satisfies(notification -> assertThat(notification.getCode())
            .isEqualTo(ErrorCode.MISSING_MONTHLY_RETURNS.getCode()));
  }

  @Test
  void shouldFailWholeRequest_whenServiceThrowsServerError() {
    PortfolioHoldingsCommand allocationCommand = allocationCommand();
    PeriodCommand sharpeCommand = sharpeCommand(null);
    when(securityAttributesFetcher.fetch(anyList(), anyCollection(), anyList())).thenReturn(SecurityData.EMPTY);
    when(sharpeRatioService.perform(eq(sharpeCommand), any()))
        .thenThrow(new ExternalServiceUnavailableException("Security Master"));

    assertThatThrownBy(() -> orchestrator.calculateAll(List.of(allocationCommand, sharpeCommand)))
        .isInstanceOf(ExternalServiceUnavailableException.class)
        .satisfies(exception -> assertThat(((ExternalServiceUnavailableException) exception).getErrorCode())
            .isEqualTo(ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE));
  }

  @Test
  void shouldFailWholeRequest_whenServiceThrowsUnexpectedException() {
    PortfolioHoldingsCommand allocationCommand = allocationCommand();
    PeriodCommand sharpeCommand = sharpeCommand(null);
    when(securityAttributesFetcher.fetch(anyList(), anyCollection(), anyList())).thenReturn(SecurityData.EMPTY);
    when(sharpeRatioService.perform(eq(sharpeCommand), any()))
        .thenThrow(new NullPointerException("unexpected bug in calculator"));

    assertThatThrownBy(() -> orchestrator.calculateAll(List.of(allocationCommand, sharpeCommand)))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void shouldThrowException_whenTwoServicesRegisterSameMetric() {
    CalculationService<?, ?, ?> duplicate = mock(CalculationService.class);
    when(duplicate.getMetric()).thenReturn(CalculationMetric.SHARPE_RATIO);
    List<CalculationService<?, ?, ?>> services = List.<CalculationService<?, ?, ?>>of(sharpeRatioService, duplicate);
    DefaultDataProperties defaults = new DefaultDataProperties(DEFAULT_PROVIDERS);

    assertThatThrownBy(() -> new MetricCalculationOrchestrator(services, securityAttributesFetcher, defaults))
        .isInstanceOf(CalculationException.class);
  }

  private static PortfolioHoldingsCommand allocationCommand() {
    PortfolioHoldingsCommand command = PortfolioHoldingsCommand.builder()
        .holdings(List.of(holding("XIU")))
        .build();
    command.setMetric(CalculationMetric.ASSET_ALLOCATIONS);
    return command;
  }

  private static PeriodCommand sharpeCommand(PortfolioHolding benchmarkHolding) {
    PeriodCommand command = new PeriodCommand();
    command.setMetric(CalculationMetric.SHARPE_RATIO);
    command.setHoldings(List.of(holding("XIU")));
    if (benchmarkHolding != null) {
      command.setBenchmarkHoldings(List.of(benchmarkHolding));
    }
    return command;
  }

  private static PortfolioHolding holding(String ticker) {
    return new PortfolioHolding(BigDecimal.ONE, FinancialInstrumentType.ETF, Country.CANADA,
        new SecurityIdentifier(ticker, FiIdentifierType.TICKER));
  }
}
