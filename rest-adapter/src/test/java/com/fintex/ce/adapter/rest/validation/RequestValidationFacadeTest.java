package com.fintex.ce.adapter.rest.validation;

import com.fintex.ce.adapter.rest.dto.request.AverageMerRequestDTO;
import com.fintex.ce.adapter.rest.dto.request.BestWorstPeriodsReqDTO;
import com.fintex.ce.adapter.rest.dto.request.DistributionOfReturnsReqDTO;
import com.fintex.ce.adapter.rest.dto.request.IncomeForecastReqDTO;
import com.fintex.ce.adapter.rest.dto.request.LeadingTotalReturnPeriodsReqDTO;
import com.fintex.ce.adapter.rest.dto.request.MultiplePortfoliosReqDTO;
import com.fintex.ce.adapter.rest.dto.request.PeriodsReqDTO;
import com.fintex.ce.adapter.rest.dto.request.PortfolioHoldingsReqDTO;
import com.fintex.ce.adapter.rest.dto.request.ReturnReqDTO;
import com.fintex.ce.adapter.rest.dto.request.RollingCalculationReqDTO;
import com.fintex.ce.adapter.rest.dto.request.TopCommonHoldingsReqDTO;
import com.fintex.ce.domain.model.holding.Holding;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Set;

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
    PeriodsReqDTO dto = mock(PeriodsReqDTO.class);
    facade.validatePeriodsRequest(dto);
    verify(periodsReqDtoValidator).validate(dto);
  }

  @Test
  void validateBenchmarkPeriodsRequest_delegatesToBenchmarkValidator() {
    PeriodsReqDTO dto = mock(PeriodsReqDTO.class);
    facade.validateBenchmarkPeriodsRequest(dto);
    verify(benchmarkPeriodsValidator).validate(dto);
  }

  @Test
  void validateTrailingTotalReturn_delegatesToTrailingValidator() {
    PeriodsReqDTO dto = mock(PeriodsReqDTO.class);
    facade.validateTrailingTotalReturn(dto);
    verify(trailingTotalReturnsValidator).validate(dto);
  }

  @Test
  void validateMaxDrawdown_delegatesToMaxDrawdownValidator() {
    PeriodsReqDTO dto = mock(PeriodsReqDTO.class);
    facade.validateMaxDrawdown(dto);
    verify(maxDrawdownValidator).validate(dto);
  }

  @Test
  void validateMarRatio_delegatesToMarRatioValidator() {
    PeriodsReqDTO dto = mock(PeriodsReqDTO.class);
    facade.validateMarRatio(dto);
    verify(marRatioValidator).validate(dto);
  }

  @Test
  void validateCorrelation_delegatesToCorrelationValidator() {
    PeriodsReqDTO dto = mock(PeriodsReqDTO.class);
    facade.validateCorrelation(dto);
    verify(correlationValidator).validate(dto);
  }

  @Test
  void validateRollingTotalReturns_delegatesToRollingTotalReturnsValidator() {
    RollingCalculationReqDTO dto = mock(RollingCalculationReqDTO.class);
    facade.validateRollingTotalReturns(dto);
    verify(rollingTotalReturnsValidator).validate(dto);
  }

  @Test
  void validateRollingCalculation_delegatesToRollingCalculationValidator() {
    RollingCalculationReqDTO dto = mock(RollingCalculationReqDTO.class);
    facade.validateRollingCalculation(dto);
    verify(rollingCalculationValidator).validate(dto);
  }

  @Test
  void validateRollingCorrelation_delegatesToRollingCorrelationValidator() {
    RollingCalculationReqDTO dto = mock(RollingCalculationReqDTO.class);
    facade.validateRollingCorrelation(dto);
    verify(rollingCorrelationValidator).validate(dto);
  }

  @Test
  void validateLeadingTotalReturn_delegatesToLeadingTotalReturnsValidator() {
    LeadingTotalReturnPeriodsReqDTO dto = mock(LeadingTotalReturnPeriodsReqDTO.class);
    facade.validateLeadingTotalReturn(dto);
    verify(leadingTotalReturnsValidator).validate(dto);
  }

  @Test
  void validatePortfolioHoldingsRequest_delegatesToPortfolioHoldingsValidator() {
    PortfolioHoldingsReqDTO dto = mock(PortfolioHoldingsReqDTO.class);
    facade.validatePortfolioHoldingsRequest(dto);
    verify(portfolioHoldingsValidator).validate(dto);
  }

  @Test
  void validateReturnRequest_delegatesToReturnValidator() {
    ReturnReqDTO dto = mock(ReturnReqDTO.class);
    facade.validateReturnRequest(dto);
    verify(returnValidator).validate(dto);
  }

  @Test
  void validateAverageMerRequest_delegatesToAverageMerValidator() {
    AverageMerRequestDTO dto = mock(AverageMerRequestDTO.class);
    facade.validateAverageMerRequest(dto);
    verify(averageMerValidator).validate(dto);
  }

  @Test
  @SuppressWarnings("unchecked")
  void validateCommonDatesRequest_delegatesToCommonDatesValidator() {
    MultiplePortfoliosReqDTO dto = mock(MultiplePortfoliosReqDTO.class);
    List<Holding> benchmarkHoldings = mock(List.class);
    Set<MultiplePortfoliosReqDTO.Portfolio> portfolios = mock(Set.class);
    when(dto.getBenchmarkHoldings()).thenReturn(benchmarkHoldings);
    when(dto.getPortfolios()).thenReturn(portfolios);
    facade.validateCommonDatesRequest(dto);
    verify(commonDatesValidator).validate(benchmarkHoldings, portfolios);
  }

  @Test
  void validateBestWorstPeriods_delegatesToBestWorstPeriodsValidator() {
    BestWorstPeriodsReqDTO dto = mock(BestWorstPeriodsReqDTO.class);
    facade.validateBestWorstPeriods(dto);
    verify(bestWorstPeriodsValidator).validate(dto);
  }

  @Test
  void validateTopCommonHoldings_delegatesToTopCommonHoldingsValidator() {
    TopCommonHoldingsReqDTO dto = mock(TopCommonHoldingsReqDTO.class);
    facade.validateTopCommonHoldings(dto);
    verify(topCommonHoldingsValidator).validate(dto);
  }

  @Test
  void validateClassificationAllocation_delegatesToClassificationAllocationValidator() {
    PortfolioHoldingsReqDTO dto = mock(PortfolioHoldingsReqDTO.class);
    facade.validateClassificationAllocation(dto);
    verify(classificationAllocationValidator).validate(dto);
  }

  @Test
  void validateDistributionOfReturns_delegatesToDistributionOfReturnsValidator() {
    DistributionOfReturnsReqDTO dto = mock(DistributionOfReturnsReqDTO.class);
    facade.validateDistributionOfReturns(dto);
    verify(distributionOfReturnsValidator).validate(dto);
  }

  @Test
  void validateIncomeForecast_delegatesToIncomeForecastValidator() {
    IncomeForecastReqDTO dto = mock(IncomeForecastReqDTO.class);
    facade.validateIncomeForecast(dto);
    verify(incomeForecastValidator).validate(dto);
  }
}