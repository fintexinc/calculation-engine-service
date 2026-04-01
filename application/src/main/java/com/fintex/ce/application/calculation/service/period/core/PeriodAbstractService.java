package com.fintex.ce.application.calculation.service.period.core;

import com.fintex.ce.application.calculation.metric.core.PeriodCalculationAbstract;
import com.fintex.ce.application.calculation.service.MonthlyReturnsService;
import com.fintex.ce.application.returns.Returns;
import com.fintex.ce.calculation.PeriodCalculationService;
import com.fintex.ce.domain.dto.calculation.CalculationDTO;
import com.fintex.ce.domain.dto.command.PeriodCommand;
import com.fintex.ce.domain.model.HoldingMonthlyReturns;
import com.fintex.ce.domain.model.enumeration.ExceptionCode;
import com.fintex.ce.domain.model.enumeration.Period;
import com.fintex.ce.domain.model.result.PeriodResult;
import com.fintex.ce.util.ReturnFactorScale;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;

/**
 * @param <E>
 *          result object
 */
public abstract class PeriodAbstractService<E extends PeriodResult, R extends PeriodCommand>
    implements
      PeriodCalculationService<E, R> {
  protected final Set<String> defaultPeriods;
  public MonthlyReturnsService monthlyReturnsService;

  protected PeriodAbstractService(MonthlyReturnsService monthlyReturnsService,
      Set<String> defaultPeriods) {
    this.monthlyReturnsService = monthlyReturnsService;
    this.defaultPeriods = defaultPeriods;
  }

  public abstract PeriodCalculationAbstract<E, ?> defineCalculationMethod(final R command);

  @Override
  public E perform(final R command) {
    final PeriodCalculationAbstract<E, ?> calculationMethod = defineCalculationMethod(command);
    return calculationMethod.calculate(command.getPeriods());
  }

  public CalculationDTO buildCalculationDto(final R command, final ReturnFactorScale returnFactorScale) {
    final Returns<HoldingMonthlyReturns> monthlyReturns = monthlyReturnsService.getPortfolioMonthlyReturns(
        command.getHoldings(), command.getCurrency(), returnFactorScale);

    final NavigableMap<LocalDate, BigDecimal> portfolioTotalReturns = monthlyReturnsService
        .getWeightedAverageWithCpedValidation(monthlyReturns, command.getCustomPed());

    return new CalculationDTO(command.getCustomIntervalPsd(), portfolioTotalReturns);
  }

  public void addSpecificChecks(final PeriodCommand command) {
    if (CollectionUtils.isEmpty(command.getPeriods())) {
      return;
    }
    for (String period : command.getPeriods()) {
      if (StringUtils.isNumeric(period) && Integer.parseInt(period) < 12) {
        throw ExceptionCode.ERR_RRC_TIP_001.reqValidationError();
      }
      if (Period.YEAR_TO_DATE.name().equalsIgnoreCase(period)) {
        throw ExceptionCode.ERR_RRC_TIP_002.reqValidationError();
      }
    }
  }
}
