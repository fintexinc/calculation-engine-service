package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.application.calculation.metric.core.PeriodCalculationAbstract;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.result.TimeIntervalResult;
import com.fintex.ce.model.domain.result.risk.TreynorRatioResult;

import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

import static com.fintex.ce.application.util.DecimalUtils.divide;
import static com.fintex.ce.model.util.BigDecimalConstants.TWELVE;

@Slf4j
public class TreynorRatioCalculation extends PeriodCalculationAbstract<TreynorRatioResult, BigDecimal> {

  private final NavigableMap<LocalDate, BigDecimal> tBills;
  private final BetaCalculation betaCalculation;

  public TreynorRatioCalculation(final PeriodCalculationInput input,
      final Set<String> defaultPeriods,
      final NavigableMap<LocalDate, BigDecimal> tBills,
      final BetaCalculation betaCalculation) {
    super(input, defaultPeriods);
    this.tBills = tBills;
    this.betaCalculation = betaCalculation;
  }

  @Override
  public BigDecimal calculatePeriodForNumberOfMonths(int numberOfMonths) {
    if (numberOfMonths > getPortfolioTotalReturns().size() || numberOfMonths < TWELVE.intValue()) {
      return null;
    }
    final LocalDate periodStartDate = getPeriodStartDate(numberOfMonths, getPortfolioTotalReturns());
    final BigDecimal annualizedPortfolioReturn = calculateAverageArithmeticAnnualizedReturn(getPortfolioTotalReturns(),
        periodStartDate, numberOfMonths);
    final BigDecimal annualizedRiskFreeRate = calculateAverageArithmeticAnnualizedReturn(restrictTBillsRange(tBills),
        periodStartDate, numberOfMonths);
    final BigDecimal beta = betaCalculation.calculatePeriodForNumberOfMonths(numberOfMonths);
    if (Objects.isNull(beta)) {
      return null;
    }
    return calculateTreynorRatio(annualizedPortfolioReturn, annualizedRiskFreeRate, beta);
  }

  @Override
  public TreynorRatioResult defineResponseType(final Set<Pair<String, BigDecimal>> periodValues) {
    final var result = new TreynorRatioResult();
    final Set<TimeIntervalResult> timeIntervals = formTimeIntervalResult(periodValues);
    result.setTreynorRatio(timeIntervals);
    return result;
  }

  public BigDecimal calculateTreynorRatio(final BigDecimal annualizedPortfolioReturn,
      final BigDecimal annualizedRiskFreeRate,
      final BigDecimal beta) {
    log.info("annualizedPortfolioReturn: {}, annualizedRiskFreeRate: {}, beta: {}",
        annualizedPortfolioReturn, annualizedRiskFreeRate, beta);
    return divide(annualizedPortfolioReturn.subtract(annualizedRiskFreeRate), beta);
  }

}
