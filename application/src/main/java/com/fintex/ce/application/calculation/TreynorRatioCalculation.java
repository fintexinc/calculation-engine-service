package com.fintex.ce.application.calculation;

import com.fintex.ce.application.calculation.core.PeriodCalculationAbstract;
import com.fintex.ce.domain.model.calculation.CalculationDTO;
import com.fintex.ce.application.result.TreynorRatioResult;
import com.fintex.ce.application.result.core.TimeIntervalResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;

import static com.fintex.ce.domain.constant.BigDecimalConstants.TWELVE;
import static com.fintex.ce.util.DecimalUtils.divide;

@Slf4j
public class TreynorRatioCalculation extends PeriodCalculationAbstract<TreynorRatioResult, BigDecimal> {

  private final NavigableMap<LocalDate, BigDecimal> tBills;
  private final BetaCalculation betaCalculation;

  public TreynorRatioCalculation(final CalculationDTO input,
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
  public TreynorRatioResult defineResponseType(final Set<Pair<String, BigDecimal>> result) {
    final var treynorRatioResDTO = new TreynorRatioResult();
    final Set<TimeIntervalResult> timeIntervals = formTimeIntervalResult(result);
    treynorRatioResDTO.setTreynorRatio(timeIntervals);
    return treynorRatioResDTO;
  }

  public BigDecimal calculateTreynorRatio(final BigDecimal annualizedPortfolioReturn,
      final BigDecimal annualizedRiskFreeRate,
      final BigDecimal beta) {
    log.info("annualizedPortfolioReturn: {}, annualizedRiskFreeRate: {}, beta: {}",
        annualizedPortfolioReturn, annualizedRiskFreeRate, beta);
    return divide(annualizedPortfolioReturn.subtract(annualizedRiskFreeRate), beta);
  }

}
