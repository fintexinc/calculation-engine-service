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

  public void validatePeriodsRequest(PeriodCommand dto) {
    periodsReqDtoValidator.validate(dto);
  }

  public void validateBenchmarkPeriodsRequest(PeriodCommand dto) {
    benchmarkPeriodsValidator.validate(dto);
  }

  public void validateTrailingTotalReturn(PeriodCommand dto) {
    trailingTotalReturnsValidator.validate(dto);
  }

  public void validateMaxDrawdown(PeriodCommand dto) {
    maxDrawdownValidator.validate(dto);
  }

  public void validateMarRatio(PeriodCommand dto) {
    marRatioValidator.validate(dto);
  }

  public void validateCorrelation(PeriodCommand dto) {
    correlationValidator.validate(dto);
  }

  public void validateRollingTotalReturns(RollingCalculationCommand dto) {
    rollingTotalReturnsValidator.validate(dto);
  }

  public void validateRollingCalculation(RollingCalculationCommand dto) {
    rollingCalculationValidator.validate(dto);
  }

  public void validateRollingCorrelation(RollingCalculationCommand dto) {
    rollingCorrelationValidator.validate(dto);
  }

  public void validateLeadingTotalReturn(LeadingTotalReturnCommand dto) {
    leadingTotalReturnsValidator.validate(dto);
  }

  public void validatePortfolioHoldingsRequest(PortfolioHoldingsCommand dto) {
    portfolioHoldingsValidator.validate(dto);
  }

  public void validateReturnRequest(ReturnCommand dto) {
    returnValidator.validate(dto);
  }

  public void validateAverageMerRequest(AverageMerCommand dto) {
    averageMerValidator.validate(dto);
  }

  public void validateCommonDatesRequest(MultiplePortfoliosCommand dto) {
    commonDatesValidator.validate(dto.getBenchmarkHoldings(), dto.getPortfolios());
  }

  public void validateBestWorstPeriods(BestWorstPeriodsCommand dto) {
    bestWorstPeriodsValidator.validate(dto);
  }

  public void validateTopCommonHoldings(TopCommonHoldingsCommand dto) {
    topCommonHoldingsValidator.validate(dto);
  }

  public void validateClassificationAllocation(PortfolioHoldingsCommand dto) {
    classificationAllocationValidator.validate(dto);
  }

  public void validateDistributionOfReturns(DistributionOfReturnsCommand dto) {
    distributionOfReturnsValidator.validate(dto);
  }

  public void validateIncomeForecast(IncomeForecastCommand dto) {
    incomeForecastValidator.validate(dto);
  }
}
