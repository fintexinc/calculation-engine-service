package com.fintex.ce.domain.calculation;

import com.fintex.ce.dto.response.BestWorstPeriodsResponseDTO;
import com.fintex.ce.dto.response.bestworstperiods.BestWorstPeriodDTO;
import com.fintex.ce.dto.response.bestworstperiods.IntervalDTO;
import com.fintex.ce.dto.response.bestworstperiods.PeriodDateDTO;
import com.fintex.ce.dto.response.bestworstperiods.PeriodValueDTO;
import com.fintex.ce.util.DecimalUtils;
import lombok.EqualsAndHashCode;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static com.fintex.ce.util.DateTimeUtils.toLastDayOfMonth;
import static java.math.BigDecimal.ZERO;

@EqualsAndHashCode
public class BestWorstPeriodCalculation {

    private static final int ONE = 1;
    private static final int TWELVE = 12;

    private final Set<Long> periods;
    BestWorstPeriodDTO bestWorstPeriodDTO = new BestWorstPeriodDTO();

    private final NavigableMap<LocalDate, BigDecimal> portfolioReturns;

    public BestWorstPeriodCalculation(final NavigableMap<LocalDate, BigDecimal> portfolioReturns, final Set<Long> periods) {
        this.portfolioReturns = portfolioReturns;
        this.periods = periods;
    }

    public BestWorstPeriodsResponseDTO calculate() {
        if (CollectionUtils.isEmpty(portfolioReturns)) {
            return null;
        }
        for (final Long period : periods) {
            final TreeMap<LocalDate, BigDecimal> rollingCumulativeReturns = calculateMonthRollingCumulativeReturns(period);
            calculateBestWorstPeriodValues(period, rollingCumulativeReturns);
        }
        return new BestWorstPeriodsResponseDTO(portfolioReturns.lastKey(), portfolioReturns.firstKey(), bestWorstPeriodDTO);
    }

    void calculateBestWorstPeriodValues(final Long period, final TreeMap<LocalDate, BigDecimal> rollingCumulativeReturns) {
        final LocalDate startOfPeriods = getStartOfPeriodsDate(period, rollingCumulativeReturns);
        if (startOfPeriods.compareTo(rollingCumulativeReturns.lastKey()) <= 0) {
            final TreeMap<LocalDate, BigDecimal> subMapByStartPeriodDate = getMapByPeriodStartDate(rollingCumulativeReturns, startOfPeriods);
            calculateNumberOfPeriods(period, subMapByStartPeriodDate);
            calculateAverage(period, subMapByStartPeriodDate);
            calculateBestPeriodValue(period, subMapByStartPeriodDate);
            calculateWorstPeriodValue(period, subMapByStartPeriodDate);
            calculatePositive(period, subMapByStartPeriodDate);
        } else {
            addDefaultValues(period);
        }
    }

    void addDefaultValues(final Long period) {
        bestWorstPeriodDTO.getNumberOfPeriods().add(new PeriodValueDTO(period, ZERO));
        bestWorstPeriodDTO.getAverage().add(new PeriodValueDTO(period, null));
        bestWorstPeriodDTO.getBestPeriodPct().add(new PeriodValueDTO(period, null));
        bestWorstPeriodDTO.getBestPeriodDate().add(new PeriodDateDTO(period, null));
        bestWorstPeriodDTO.getWorstPeriodPct().add(new PeriodValueDTO(period, null));
        bestWorstPeriodDTO.getWorstPeriodDate().add(new PeriodDateDTO(period, null));
        bestWorstPeriodDTO.getPctPositive().add(new PeriodValueDTO(period, null));
    }

    void calculateNumberOfPeriods(final Long period, final TreeMap<LocalDate, BigDecimal> subMapRollingCumulativeReturns) {
        final int numOfPeriods = subMapRollingCumulativeReturns.size();
        bestWorstPeriodDTO.getNumberOfPeriods().add(new PeriodValueDTO(period, BigDecimal.valueOf(numOfPeriods)));
    }

    void calculatePositive(final Long period, final TreeMap<LocalDate, BigDecimal> subMapRollingCumulativeReturns) {
        final long numberOfPositiveValues = subMapRollingCumulativeReturns.values().stream().filter(v -> v.compareTo(ZERO) > 0).count();
        final BigDecimal positive = DecimalUtils.divide(BigDecimal.valueOf(numberOfPositiveValues), getNumberOfPeriodsByPeriod(period).getValue());
        bestWorstPeriodDTO.getPctPositive().add(new PeriodValueDTO(period, DecimalUtils.toUserScale(positive)));
    }

    void calculateAverage(final Long period, final TreeMap<LocalDate, BigDecimal> subMapRollingCumulativeReturns) {
        final BigDecimal rollingComulativeReturtsSum = subMapRollingCumulativeReturns.values().stream().reduce(ZERO, BigDecimal::add);
        final BigDecimal average = DecimalUtils.divide(rollingComulativeReturtsSum, subMapRollingCumulativeReturns.size());
        bestWorstPeriodDTO.getAverage().add(new PeriodValueDTO(period, annualize(average, period)));
    }

    void calculateBestPeriodValue(final Long period, final TreeMap<LocalDate, BigDecimal> rollingCumulativeReturns) {
        final LocalDate bestPeriodDate = rollingCumulativeReturns.entrySet().stream().
                sorted(Map.Entry.comparingByValue()).
                map(Map.Entry::getKey).
                toList().get(rollingCumulativeReturns.size() - ONE);
        bestWorstPeriodDTO.getBestPeriodPct().add(new PeriodValueDTO(period, annualize(rollingCumulativeReturns.get(bestPeriodDate), period)));
        bestWorstPeriodDTO.getBestPeriodDate().add(new PeriodDateDTO(period, new IntervalDTO(getStartDate(period, bestPeriodDate), bestPeriodDate)));
    }

    void calculateWorstPeriodValue(final Long period, final TreeMap<LocalDate, BigDecimal> rollingCumulativeReturns) {
        final LocalDate worstPeriodDate = rollingCumulativeReturns.entrySet().stream().
                sorted(Map.Entry.<LocalDate, BigDecimal>comparingByValue().reversed()).
                map(Map.Entry::getKey).toList().get(rollingCumulativeReturns.size() - ONE);
        bestWorstPeriodDTO.getWorstPeriodPct().add(new PeriodValueDTO(period, annualize(rollingCumulativeReturns.get(worstPeriodDate), period)));
        bestWorstPeriodDTO.getWorstPeriodDate().add(new PeriodDateDTO(period, new IntervalDTO(getStartDate(period, worstPeriodDate), worstPeriodDate)));
    }

    private LocalDate getStartDate(final Long period, final LocalDate bestPeriodDate) {
        return bestPeriodDate.minusMonths(period - ONE).with(TemporalAdjusters.firstDayOfMonth());
    }

    /**
     * returns annualized value by period. If period >= 12 returns (value+1)^(12/period) . If period < 12 returns value.</>
     *
     * @param value
     * @param period
     * @return
     */
    BigDecimal annualize(final BigDecimal value, final Long period) {
        if (period >= TWELVE) {
            return DecimalUtils.toUserScale(DecimalUtils.pow(BigDecimal.ONE.add(value), DecimalUtils.divide(new BigDecimal(TWELVE), period)).subtract(BigDecimal.ONE));
        } else {
            return DecimalUtils.toUserScale(value);
        }
    }

    /**
     * returns rolling cumulative returns calculated by period
     *
     * @param period - number of months
     * @return rolling cumulative returns
     */
    TreeMap<LocalDate, BigDecimal> calculateMonthRollingCumulativeReturns(final Long period) {
        final TreeMap<LocalDate, BigDecimal> cumulativeReturns = new TreeMap<>();
        portfolioReturns.forEach((key, value) -> {
            final SortedMap<LocalDate, BigDecimal> periodDates = portfolioReturns.subMap(key.minusMonths(period - ONE), true, key, true);
            final BigDecimal cumulativeReturn = DecimalUtils.toUserScale(periodDates.values().stream().reduce(BigDecimal.ONE, BigDecimal::multiply).subtract(BigDecimal.ONE));
            cumulativeReturns.put(key, cumulativeReturn);
        });
        return cumulativeReturns;
    }

    /**
     * returns number of periods by period value from bestWorstPeriodDTO
     *
     * @param period - number of months
     * @return PeriodValueDTO with value for the current period
     */
    PeriodValueDTO getNumberOfPeriodsByPeriod(final Long period) {
        return bestWorstPeriodDTO.getNumberOfPeriods().stream().filter(n -> n.getPeriod().equals(period)).findFirst().orElseThrow();
    }

    /**
     * returns period start date by number of months. Last date in return factor list minus numberOfMonths-1
     *
     * @param period                   number of month in period
     * @param rollingCumulativeReturns rolling cumulative returns
     * @return period start date
     */
    LocalDate getStartOfPeriodsDate(final Long period, final TreeMap<LocalDate, BigDecimal> rollingCumulativeReturns) {
        return toLastDayOfMonth(rollingCumulativeReturns.firstKey().plusMonths(period - ONE));
    }

    /**
     * Returns sub map from startOfPeriodsDate to the end of the rollingCumulativeReturns
     *
     * @param rollingCumulativeReturns rolling cumulative returns
     * @param startOfPeriods           date when periods start
     * @return calculated map
     */
    TreeMap<LocalDate, BigDecimal> getMapByPeriodStartDate(final TreeMap<LocalDate, BigDecimal> rollingCumulativeReturns, final LocalDate startOfPeriods) {
        return new TreeMap<>(rollingCumulativeReturns.subMap(startOfPeriods, true, rollingCumulativeReturns.lastKey(), true));
    }

}
