package com.fintex.ce.service.impl.calculation.period.core;

import com.fintex.ce.domain.calculation.PeriodBasedCalculation;
import com.fintex.ce.domain.monthlyreturns.Returns;
import com.fintex.ce.dto.calculation.CalculationDTO;
import com.fintex.ce.dto.request.PeriodsReqDTO;
import com.fintex.ce.dto.response.core.PeriodResDTO;
import com.fintex.ce.model.redis.RMonthlyReturns;
import com.fintex.ce.service.impl.calculation.MonthlyReturnsService;
import com.fintex.ce.util.ReturnFactorScale;
import com.fintex.ce.util.validation.request.RequestValidator;
import com.fintex.ce.config.enumeration.ExceptionCode;
import com.fintex.ce.config.enumeration.Period;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;

/**
 * @param <E> response object
 */
public abstract class PeriodAbstractService<E extends PeriodResDTO, R extends PeriodsReqDTO> {
    protected final Set<String> defaultPeriods;
    protected MonthlyReturnsService monthlyReturnsService;

    protected RequestValidator<R> requestValidator;

    protected PeriodAbstractService(MonthlyReturnsService monthlyReturnsService,
                                    Set<String> defaultPeriods,
                                    RequestValidator<R> periodReqDtoValidator) {
        this.monthlyReturnsService = monthlyReturnsService;
        this.defaultPeriods = defaultPeriods;
        this.requestValidator = periodReqDtoValidator;
    }

    protected abstract PeriodBasedCalculation<E> defineCalculationMethod(final R reqDTO);

    public E perform(final R reqDTO) {
        requestValidator.validate(reqDTO);
        final PeriodBasedCalculation<E> calculationMethod = defineCalculationMethod(reqDTO);
        return calculationMethod.calculate(reqDTO.getPeriods());
    }

    public CalculationDTO buildCalculationDto(final R reqDTO, final ReturnFactorScale returnFactorScale) {
        final Returns<RMonthlyReturns> monthlyReturns = monthlyReturnsService.getPortfolioMonthlyReturns(
                reqDTO.getHoldings(), reqDTO.getCurrency(), returnFactorScale);

        final NavigableMap<LocalDate, BigDecimal> portfolioTotalReturns = monthlyReturnsService
                .getWeightedAverageWithCpedValidation(monthlyReturns, reqDTO.getCustomPed());

        return new CalculationDTO(reqDTO.getCustomIntervalPsd(), portfolioTotalReturns);
    }

    public void addSpecificChecks(final PeriodsReqDTO reqDTO) {
        if (CollectionUtils.isEmpty(reqDTO.getPeriods())) {
            return;
        }
        for (String period : reqDTO.getPeriods()) {
            if (StringUtils.isNumeric(period) && Integer.parseInt(period) < 12) {
                throw ExceptionCode.ERR_RRC_TIP_001.reqValidationError();
            }
            if (Period.YEAR_TO_DATE.name().equalsIgnoreCase(period)) {
                throw ExceptionCode.ERR_RRC_TIP_002.reqValidationError();
            }
        }
    }
}
