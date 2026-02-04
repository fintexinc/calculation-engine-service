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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RequestValidationFacade {

  private final PeriodsReqDtoValidator periodsReqDtoValidator;
  private final PeriodReqDtoForBenchmarkCalculationsValidator benchmarkPeriodsValidator;
  private final TrailingTotalReturnsReqValidator trailingTotalReturnsValidator;
  private final MaxDrawdownReqValidator maxDrawdownValidator;
  private final MarRatioReqValidator marRatioValidator;
  private final CorrelationReqValidator correlationValidator;
  private final RollingTotalReturnsCalculationReqValidator rollingTotalReturnsValidator;
  private final RollingCalculationReqDtoValidator rollingCalculationValidator;
  private final RollingCorrelationReqValidator rollingCorrelationValidator;
  private final LeadingTotalReturnsReqValidator leadingTotalReturnsValidator;
  private final PortfolioHoldingsReqDtoValidator portfolioHoldingsValidator;
  private final ReturnReqDtoValidator returnValidator;
  private final AverageMerRequestValidator averageMerValidator;
  private final CommonDatesRequestValidator commonDatesValidator;
  private final BestWorstPeriodsReqValidator bestWorstPeriodsValidator;
  private final TopCommonHoldingsReqValidator topCommonHoldingsValidator;
  private final ClassificationAllocationReqValidator classificationAllocationValidator;
  private final DistributionOfReturnsReqValidator distributionOfReturnsValidator;
  private final IncomeForecastReqValidation incomeForecastValidator;

  public void validatePeriodsRequest(PeriodsReqDTO dto) {
    periodsReqDtoValidator.validate(dto);
  }

  public void validateBenchmarkPeriodsRequest(PeriodsReqDTO dto) {
    benchmarkPeriodsValidator.validate(dto);
  }

  public void validateTrailingTotalReturn(PeriodsReqDTO dto) {
    trailingTotalReturnsValidator.validate(dto);
  }

  public void validateMaxDrawdown(PeriodsReqDTO dto) {
    maxDrawdownValidator.validate(dto);
  }

  public void validateMarRatio(PeriodsReqDTO dto) {
    marRatioValidator.validate(dto);
  }

  public void validateCorrelation(PeriodsReqDTO dto) {
    correlationValidator.validate(dto);
  }

  public void validateRollingTotalReturns(RollingCalculationReqDTO dto) {
    rollingTotalReturnsValidator.validate(dto);
  }

  public void validateRollingCalculation(RollingCalculationReqDTO dto) {
    rollingCalculationValidator.validate(dto);
  }

  public void validateRollingCorrelation(RollingCalculationReqDTO dto) {
    rollingCorrelationValidator.validate(dto);
  }

  public void validateLeadingTotalReturn(LeadingTotalReturnPeriodsReqDTO dto) {
    leadingTotalReturnsValidator.validate(dto);
  }

  public void validatePortfolioHoldingsRequest(PortfolioHoldingsReqDTO dto) {
    portfolioHoldingsValidator.validate(dto);
  }

  public void validateReturnRequest(ReturnReqDTO dto) {
    returnValidator.validate(dto);
  }

  public void validateAverageMerRequest(AverageMerRequestDTO dto) {
    averageMerValidator.validate(dto);
  }

  public void validateCommonDatesRequest(MultiplePortfoliosReqDTO dto) {
    commonDatesValidator.validate(dto.getBenchmarkHoldings(), dto.getPortfolios());
  }

  public void validateBestWorstPeriods(BestWorstPeriodsReqDTO dto) {
    bestWorstPeriodsValidator.validate(dto);
  }

  public void validateTopCommonHoldings(TopCommonHoldingsReqDTO dto) {
    topCommonHoldingsValidator.validate(dto);
  }

  public void validateClassificationAllocation(PortfolioHoldingsReqDTO dto) {
    classificationAllocationValidator.validate(dto);
  }

  public void validateDistributionOfReturns(DistributionOfReturnsReqDTO dto) {
    distributionOfReturnsValidator.validate(dto);
  }

  public void validateIncomeForecast(IncomeForecastReqDTO dto) {
    incomeForecastValidator.validate(dto);
  }
}