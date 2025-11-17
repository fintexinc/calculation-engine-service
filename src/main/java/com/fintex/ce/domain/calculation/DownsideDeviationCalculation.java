package com.fintex.ce.domain.calculation;

import com.fintex.ce.domain.calculation.core.PeriodCalculationAbstract;
import com.fintex.ce.dto.calculation.CalculationDTO;
import com.fintex.ce.dto.response.DownsideDeviationResDTO;
import com.fintex.ce.dto.response.core.PeriodResDTO;
import com.fintex.ce.dto.response.core.TimeIntervalResDTO;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import static com.fintex.ce.config.constant.BigDecimalConstants.TWELVE;
import static com.fintex.ce.config.constant.BigDecimalConstants.TWO;
import static com.fintex.ce.util.CalculationUtils.sum;
import static com.fintex.ce.util.CollectorUtils.toTreeMap;
import static com.fintex.ce.util.DecimalUtils.*;

public class DownsideDeviationCalculation<T extends PeriodResDTO> extends PeriodCalculationAbstract<T, BigDecimal> {

    NavigableMap<LocalDate, BigDecimal> tBills;
    NavigableMap<LocalDate, BigDecimal> portfolioExcessReturn;

    public DownsideDeviationCalculation(final CalculationDTO input,
                                        final Set<String> defaultPeriods,
                                        final NavigableMap<LocalDate, BigDecimal> tBills) {
        super(input, defaultPeriods);
        this.tBills = restrictTBillsRange(tBills);
        portfolioExcessReturn = calculateExcessReturn(getPortfolioTotalReturns(), tBills);
    }

    @Override
    public BigDecimal calculatePeriodForNumberOfMonths(final int numberOfMonths) {
        if (numberOfMonths > getPortfolioTotalReturns().size()
                || numberOfMonths > portfolioExcessReturn.size()
                || numberOfMonths < TWELVE.intValue()) {
            return null;
        }
        final LocalDate periodStartDate = getPeriodStartDate(numberOfMonths, getPortfolioTotalReturns());
        final SortedMap<LocalDate, BigDecimal> portfolioExcessReturnsInPeriod = getSubMapByPeriodStartDate(periodStartDate, portfolioExcessReturn);
        final TreeMap<LocalDate, BigDecimal> downsideReturnSquared = calculateDownsideReturnSquared(portfolioExcessReturnsInPeriod);
        return calculateDownsideDeviation(numberOfMonths, downsideReturnSquared);
    }

    @Override
    public T defineResponseType(final Set<Pair<String, BigDecimal>> result) {
        final DownsideDeviationResDTO downsideDeviationResDTO = new DownsideDeviationResDTO();
        final Set<TimeIntervalResDTO> timeIntervals = formTimeIntervalResDTO(result);
        downsideDeviationResDTO.setDownsideDeviation(timeIntervals);
        return (T) downsideDeviationResDTO;
    }


    /**
     * calculates downside deviation by formula
     *
     * @param numberOfMonths        number of month in period
     * @param downsideReturnSquared calculated downside return squared values
     * @return calculated value
     */
    BigDecimal calculateDownsideDeviation(final int numberOfMonths, final TreeMap<LocalDate, BigDecimal> downsideReturnSquared) {
        return squareRoot(divide(sum(downsideReturnSquared), numberOfMonths)).multiply(squareRoot(TWELVE));
    }

    /**
     * calculates downside return squared values. (negativeValueInPeriod^2)
     *
     * @param portfolioExcessReturnsInPeriod portfolio excess returns values
     * @return squared values
     */
    TreeMap<LocalDate, BigDecimal> calculateDownsideReturnSquared(final SortedMap<LocalDate, BigDecimal> portfolioExcessReturnsInPeriod) {
        return portfolioExcessReturnsInPeriod.entrySet().stream()
                .filter(e -> e.getValue().compareTo(BigDecimal.ZERO) < 0)
                .collect(toTreeMap(Map.Entry::getKey, e -> pow(e.getValue(), TWO)));
    }
}