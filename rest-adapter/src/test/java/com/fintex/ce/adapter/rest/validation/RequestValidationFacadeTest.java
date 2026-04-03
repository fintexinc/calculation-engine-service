package com.fintex.ce.adapter.rest.validation;

import com.fintex.ce.domain.dto.command.AverageMerCommand;
import com.fintex.ce.domain.dto.command.BestWorstPeriodsCommand;
import com.fintex.ce.domain.dto.command.DistributionOfReturnsCommand;
import com.fintex.ce.domain.dto.command.IncomeForecastCommand;
import com.fintex.ce.domain.dto.command.LeadingTotalReturnCommand;
import com.fintex.ce.domain.dto.command.MultiplePortfoliosCommand;
import com.fintex.ce.domain.dto.command.PeriodCommand;
import com.fintex.ce.domain.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.domain.dto.command.ReturnCommand;
import com.fintex.ce.domain.dto.command.RollingCalculationCommand;
import com.fintex.ce.domain.dto.command.TopCommonHoldingsCommand;
import com.fintex.ce.domain.model.holding.Holding;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RequestValidationFacadeTest {

  @Mock
  private PeriodsReqDtoValidator periodsReqDtoValidator;
  @Mock
  private PeriodReqDtoForBenchmarkCalculationsValidator benchmarkPeriodsValidator;
  @Mock
  private TrailingTotalReturnsReqValidator trailingTotalReturnsValidator;
  @Mock
  private MaxDrawdownReqValidator maxDrawdownValidator;
  @Mock
  private MarRatioReqValidator marRatioValidator;
  @Mock
  private CorrelationReqValidator correlationValidator;
  @Mock
  private RollingTotalReturnsCalculationReqValidator rollingTotalReturnsValidator;
  @Mock
  private RollingCalculationReqDtoValidator rollingCalculationValidator;
  @Mock
  private RollingCorrelationReqValidator rollingCorrelationValidator;
  @Mock
  private LeadingTotalReturnsReqValidator leadingTotalReturnsValidator;
  @Mock
  private PortfolioHoldingsReqDtoValidator portfolioHoldingsValidator;
  @Mock
  private ReturnReqDtoValidator returnValidator;
  @Mock
  private AverageMerRequestValidator averageMerValidator;
  @Mock
  private CommonDatesRequestValidator commonDatesValidator;
  @Mock
  private BestWorstPeriodsReqValidator bestWorstPeriodsValidator;
  @Mock
  private TopCommonHoldingsReqValidator topCommonHoldingsValidator;
  @Mock
  private ClassificationAllocationReqValidator classificationAllocationValidator;
  @Mock
  private DistributionOfReturnsReqValidator distributionOfReturnsValidator;
  @Mock
  private IncomeForecastReqValidation incomeForecastValidator;

  private RequestValidationFacade facade;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    facade = new RequestValidationFacade(
        periodsReqDtoValidator,
        benchmarkPeriodsValidator,
        trailingTotalReturnsValidator,
        maxDrawdownValidator,
        marRatioValidator,
        correlationValidator,
        rollingTotalReturnsValidator,
        rollingCalculationValidator,
        rollingCorrelationValidator,
        leadingTotalReturnsValidator,
        portfolioHoldingsValidator,
        returnValidator,
        averageMerValidator,
        commonDatesValidator,
        bestWorstPeriodsValidator,
        topCommonHoldingsValidator,
        classificationAllocationValidator,
        distributionOfReturnsValidator,
        incomeForecastValidator);
  }

  @Test
  void validatePeriodsRequest_delegatesToPeriodsValidator() {
    PeriodCommand dto = mock(PeriodCommand.class);
    facade.validatePeriodsRequest(dto);
    verify(periodsReqDtoValidator).validate(dto);
  }

  @Test
  void validateBenchmarkPeriodsRequest_delegatesToBenchmarkValidator() {
    PeriodCommand dto = mock(PeriodCommand.class);
    facade.validateBenchmarkPeriodsRequest(dto);
    verify(benchmarkPeriodsValidator).validate(dto);
  }

  @Test
  void validateTrailingTotalReturn_delegatesToTrailingValidator() {
    PeriodCommand dto = mock(PeriodCommand.class);
    facade.validateTrailingTotalReturn(dto);
    verify(trailingTotalReturnsValidator).validate(dto);
  }

  @Test
  void validateMaxDrawdown_delegatesToMaxDrawdownValidator() {
    PeriodCommand dto = mock(PeriodCommand.class);
    facade.validateMaxDrawdown(dto);
    verify(maxDrawdownValidator).validate(dto);
  }

  @Test
  void validateMarRatio_delegatesToMarRatioValidator() {
    PeriodCommand dto = mock(PeriodCommand.class);
    facade.validateMarRatio(dto);
    verify(marRatioValidator).validate(dto);
  }

  @Test
  void validateCorrelation_delegatesToCorrelationValidator() {
    PeriodCommand dto = mock(PeriodCommand.class);
    facade.validateCorrelation(dto);
    verify(correlationValidator).validate(dto);
  }

  @Test
  void validateRollingTotalReturns_delegatesToRollingTotalReturnsValidator() {
    RollingCalculationCommand dto = mock(RollingCalculationCommand.class);
    facade.validateRollingTotalReturns(dto);
    verify(rollingTotalReturnsValidator).validate(dto);
  }

  @Test
  void validateRollingCalculation_delegatesToRollingCalculationValidator() {
    RollingCalculationCommand dto = mock(RollingCalculationCommand.class);
    facade.validateRollingCalculation(dto);
    verify(rollingCalculationValidator).validate(dto);
  }

  @Test
  void validateRollingCorrelation_delegatesToRollingCorrelationValidator() {
    RollingCalculationCommand dto = mock(RollingCalculationCommand.class);
    facade.validateRollingCorrelation(dto);
    verify(rollingCorrelationValidator).validate(dto);
  }

  @Test
  void validateLeadingTotalReturn_delegatesToLeadingTotalReturnsValidator() {
    LeadingTotalReturnCommand dto = mock(LeadingTotalReturnCommand.class);
    facade.validateLeadingTotalReturn(dto);
    verify(leadingTotalReturnsValidator).validate(dto);
  }

  @Test
  void validatePortfolioHoldingsRequest_delegatesToPortfolioHoldingsValidator() {
    PortfolioHoldingsCommand dto = mock(PortfolioHoldingsCommand.class);
    facade.validatePortfolioHoldingsRequest(dto);
    verify(portfolioHoldingsValidator).validate(dto);
  }

  @Test
  void validateReturnRequest_delegatesToReturnValidator() {
    ReturnCommand dto = mock(ReturnCommand.class);
    facade.validateReturnRequest(dto);
    verify(returnValidator).validate(dto);
  }

  @Test
  void validateAverageMerRequest_delegatesToAverageMerValidator() {
    AverageMerCommand dto = mock(AverageMerCommand.class);
    facade.validateAverageMerRequest(dto);
    verify(averageMerValidator).validate(dto);
  }

  @Test
  @SuppressWarnings("unchecked")
  void validateCommonDatesRequest_delegatesToCommonDatesValidator() {
    MultiplePortfoliosCommand dto = mock(MultiplePortfoliosCommand.class);
    List<Holding> benchmarkHoldings = mock(List.class);
    Set<MultiplePortfoliosCommand.Portfolio> portfolios = mock(Set.class);
    when(dto.getBenchmarkHoldings()).thenReturn(benchmarkHoldings);
    when(dto.getPortfolios()).thenReturn(portfolios);
    facade.validateCommonDatesRequest(dto);
    verify(commonDatesValidator).validate(benchmarkHoldings, portfolios);
  }

  @Test
  void validateBestWorstPeriods_delegatesToBestWorstPeriodsValidator() {
    BestWorstPeriodsCommand dto = mock(BestWorstPeriodsCommand.class);
    facade.validateBestWorstPeriods(dto);
    verify(bestWorstPeriodsValidator).validate(dto);
  }

  @Test
  void validateTopCommonHoldings_delegatesToTopCommonHoldingsValidator() {
    TopCommonHoldingsCommand dto = mock(TopCommonHoldingsCommand.class);
    facade.validateTopCommonHoldings(dto);
    verify(topCommonHoldingsValidator).validate(dto);
  }

  @Test
  void validateClassificationAllocation_delegatesToClassificationAllocationValidator() {
    PortfolioHoldingsCommand dto = mock(PortfolioHoldingsCommand.class);
    facade.validateClassificationAllocation(dto);
    verify(classificationAllocationValidator).validate(dto);
  }

  @Test
  void validateDistributionOfReturns_delegatesToDistributionOfReturnsValidator() {
    DistributionOfReturnsCommand dto = mock(DistributionOfReturnsCommand.class);
    facade.validateDistributionOfReturns(dto);
    verify(distributionOfReturnsValidator).validate(dto);
  }

  @Test
  void validateIncomeForecast_delegatesToIncomeForecastValidator() {
    IncomeForecastCommand dto = mock(IncomeForecastCommand.class);
    facade.validateIncomeForecast(dto);
    verify(incomeForecastValidator).validate(dto);
  }
}