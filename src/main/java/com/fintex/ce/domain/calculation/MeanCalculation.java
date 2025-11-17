package com.fintex.ce.domain.calculation;

import com.fintex.ce.domain.calculation.core.PeriodCalculationAbstract;
import com.fintex.ce.dto.calculation.CalculationDTO;
import com.fintex.ce.dto.response.MeanResDTO;
import com.fintex.ce.dto.response.core.PeriodResDTO;
import com.fintex.ce.dto.response.core.TimeIntervalResDTO;
import com.fintex.ce.util.CalculationUtils;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NavigableMap;
import java.util.Set;
import java.util.SortedMap;

import static com.fintex.ce.config.constant.BigDecimalConstants.TWELVE;
import static com.fintex.ce.util.DecimalUtils.INTERNAL_SCALE;
import static com.fintex.ce.util.DecimalUtils.toScale;

@Accessors(chain = true)
@EqualsAndHashCode
public class MeanCalculation<T extends PeriodResDTO> extends PeriodCalculationAbstract<T, BigDecimal> {

    @Setter
    private int scale = INTERNAL_SCALE;

    public MeanCalculation(final CalculationDTO input,
                           final Set<String> defaultPeriods) {
        super(input, defaultPeriods);
    }

    @Override
    public BigDecimal calculatePeriodForNumberOfMonths(final int numberOfMonths) {
        return calculatePeriodForNumberOfMonths(numberOfMonths, getPortfolioTotalReturns());
    }

    private BigDecimal calculatePeriodForNumberOfMonths(final int numberOfMonths,
                                                        final NavigableMap<LocalDate, BigDecimal> returns) {
        if (numberOfMonths > returns.size() || numberOfMonths < TWELVE.intValue()) {
            return null;
        }
        final LocalDate periodStartDate = getPeriodStartDate(numberOfMonths, returns);
        final SortedMap<LocalDate, BigDecimal> portfolioTotalReturnsByPeriod = getSubMapByPeriodStartDate(periodStartDate, returns);

        return calculateMean(portfolioTotalReturnsByPeriod);
    }

    /**
     * calculates mean
     *
     * @param portfolioTotalReturnsByPeriod portfolio total return values from start of period to the end
     * @return mean value
     */
    private BigDecimal calculateMean(final SortedMap<LocalDate, BigDecimal> portfolioTotalReturnsByPeriod) {
        final BigDecimal mean = CalculationUtils.average(portfolioTotalReturnsByPeriod);
        return toScale(mean, scale);
    }

    @Override
    public T defineResponseType(final Set<Pair<String, BigDecimal>> result) {
        final MeanResDTO meanResDTO = new MeanResDTO();
        final Set<TimeIntervalResDTO> timeIntervals = formTimeIntervalResDTO(result);
        meanResDTO.setMean(timeIntervals);
        return (T) meanResDTO;
    }

}
