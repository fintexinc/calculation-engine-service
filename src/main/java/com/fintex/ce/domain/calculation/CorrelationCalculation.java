package com.fintex.ce.domain.calculation;

import com.fintex.ce.domain.calculation.core.PeriodCalculationAbstract;
import com.fintex.ce.dto.calculation.CalculationDTO;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.response.CorrelationResDTO;
import com.fintex.ce.dto.response.correlation.CorrelationKeyValueDTO;
import com.fintex.ce.dto.response.correlation.CorrelationPeriodDTO;
import com.fintex.ce.dto.response.correlation.HoldingsKeyDTO;
import com.fintex.ce.util.CalculationUtils;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static com.fintex.ce.config.constant.BigDecimalConstants.TWELVE;
import static com.fintex.ce.config.constant.BigDecimalConstants.TWO;
import static com.fintex.ce.util.DecimalUtils.*;
import static com.fintex.ce.util.PortfolioUtils.createKey;

@EqualsAndHashCode(callSuper = true)
public class CorrelationCalculation extends PeriodCalculationAbstract<CorrelationResDTO, List<CorrelationPeriodDTO>> {

    private final Map<Holding, Map<LocalDate, BigDecimal>> portfolioBaseTotalReturn;

    public CorrelationCalculation(final CalculationDTO calculationDTO,
                                  final Map<Holding, Map<LocalDate, BigDecimal>> portfolioBaseTotalReturn,
                                  final Set<String> defaultPeriods) {
        super(calculationDTO, defaultPeriods);
        this.portfolioBaseTotalReturn = portfolioBaseTotalReturn;
    }

    @Override
    public List<CorrelationPeriodDTO> calculatePeriodForNumberOfMonths(final int numberOfMonths) {
        if (numberOfMonths < TWELVE.intValue()) {
            return null;
        }
        final Map<Holding, Map<LocalDate, BigDecimal>> returns = portfolioBaseTotalReturn.entrySet().stream()
                .filter(holdingReturns -> hasEnoughReturns(numberOfMonths, holdingReturns))
                .collect(Collectors.toMap(Map.Entry::getKey, e -> calculatePortfolioBaseTotalReturnValuesByPeriod(numberOfMonths, e.getValue())));
        return returns.keySet().stream()
                .map(localDateBigDecimalMap -> getCorrelationPeriod(localDateBigDecimalMap, returns, numberOfMonths))
                .collect(Collectors.toList());
    }

    boolean hasEnoughReturns(int numberOfMonths, final Map.Entry<Holding, Map<LocalDate, BigDecimal>> holdingReturns) {
        return holdingReturns.getValue().size() >= numberOfMonths;
    }

    @Override
    public List<CorrelationPeriodDTO> toUserFormat(final List<CorrelationPeriodDTO> correlationPeriodDTOS) {
        if (correlationPeriodDTOS == null) {
            return null;
        }

        for (final var dto : correlationPeriodDTOS) {
            if (!CollectionUtils.isEmpty(dto.getCorrelations())) {
                dto.getCorrelations().forEach(e -> e.setValue(toUserScale(e.getValue())));
            }
        }
        return correlationPeriodDTOS;
    }

    @Override
    public CorrelationResDTO defineResponseType(final Set<Pair<String, List<CorrelationPeriodDTO>>> result) {
        final List<CorrelationPeriodDTO> correlationPeriods = result.stream()
                .filter(v -> Objects.nonNull(v.getValue()))
                .flatMap(l -> setPeriod(l.getKey(), l.getValue()).stream())
                .collect(Collectors.toList());
        final List<HoldingsKeyDTO> holdingsKeys = portfolioBaseTotalReturn.keySet().stream()
                .map(HoldingsKeyDTO::buildHoldingsKeyDTO)
                .collect(Collectors.toList());
        return new CorrelationResDTO()
                .setCorrelationPeriods(correlationPeriods)
                .setHoldingsKey(holdingsKeys);
    }

    /**
     * Method is used to set a period.
     * E.g the period for CIPSD is 20 but in response should be "SINCE_CUSTOM_PERFORMANCE_START_DATE" instead of 20.
     *
     * @param period  period to set.
     * @param periods list of all periods.
     * @return mapped period.
     */
    List<CorrelationPeriodDTO> setPeriod(final String period, final List<CorrelationPeriodDTO> periods) {
        return periods.stream().map(e -> e.setPeriod(period)).collect(Collectors.toList());
    }

    /**
     * calculates portfolio base total return values (x - average). x is the return of holding, average is average value by period.
     *
     * @param numberOfMonths            number of month in period
     * @param portfolioTotalReturnValue portfolioBaseTotalReturn (Calculated off local and FX Rates)
     * @return calculated portfolio base total return values
     */
    Map<LocalDate, BigDecimal> calculatePortfolioBaseTotalReturnValuesByPeriod(final int numberOfMonths,
                                                                               final Map<LocalDate, BigDecimal> portfolioTotalReturnValue) {
        final TreeMap<LocalDate, BigDecimal> portfolioTotalReturnByHolding = new TreeMap<>(portfolioTotalReturnValue);
        final LocalDate periodStartDate = getPeriodStartDate(numberOfMonths, portfolioTotalReturnByHolding);
        final SortedMap<LocalDate, BigDecimal> portfolioBaseTotalReturnByPeriodStartDate = getSubMapByPeriodStartDate(periodStartDate, portfolioTotalReturnByHolding);
        final BigDecimal average = CalculationUtils.average(portfolioBaseTotalReturnByPeriodStartDate);
        return portfolioBaseTotalReturnByPeriodStartDate.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, v -> v.getValue().subtract(average)));
    }

    /**
     * calculates correlation and maps to CorrelationPeriodDTO
     *
     * @param keyHolding     key holding in current correlation
     * @param returns        calculated portfolio base total return values by period
     * @param numberOfMonths
     * @return mapped CorrelationPeriodDTO
     */
    CorrelationPeriodDTO getCorrelationPeriod(final Holding keyHolding,
                                              final Map<Holding, Map<LocalDate, BigDecimal>> returns,
                                              final int numberOfMonths) {
        final Map<LocalDate, BigDecimal> keyHoldingValues = returns.get(keyHolding);
        final Map<Holding, BigDecimal> correlations = returns.entrySet().stream()
                .filter(e -> !e.getKey().equals(keyHolding))
                .collect(Collectors.toMap(Map.Entry::getKey, e -> calculateCorrelation(keyHoldingValues, e.getValue())));
        return mapToCorrelationPeriodDTO(keyHolding, numberOfMonths, correlations);
    }

    /**
     * @param keyHolding     key holding in current correlation
     * @param numberOfMonths number of month in period
     * @param correlations   calculated correlations
     * @return CorrelationPeriodDTO
     */
    CorrelationPeriodDTO mapToCorrelationPeriodDTO(final Holding keyHolding,
                                                   final int numberOfMonths,
                                                   final Map<Holding, BigDecimal> correlations) {
        return new CorrelationPeriodDTO()
                .setPeriod(String.valueOf(numberOfMonths))
                .setKey(createKey(keyHolding))
                .setCorrelations(correlations.entrySet().stream()
                        .map(c -> new CorrelationKeyValueDTO(createKey(c.getKey()), c.getValue()))
                        .collect(Collectors.toList()));
    }

    /**
     * calculates correlation by formula
     *
     * @param keyHoldingValues portfolio base total return values by period of key holding
     * @param holdingValues    portfolio base total return values by period of another holding needed for correlation
     * @return correlation
     */
    BigDecimal calculateCorrelation(final Map<LocalDate, BigDecimal> keyHoldingValues,
                                    final Map<LocalDate, BigDecimal> holdingValues) {
        return divide(calculateNumerator(keyHoldingValues, holdingValues), calculateDenominator(keyHoldingValues, holdingValues));
    }

    /**
     * calculates numerator for correlation formula
     *
     * @param keyHoldingValues portfolio base total return values by period of key holding
     * @param holdingValues    portfolio base total return values by period of another holding needed for correlation
     * @return numerator
     */
    BigDecimal calculateNumerator(final Map<LocalDate, BigDecimal> keyHoldingValues,
                                  final Map<LocalDate, BigDecimal> holdingValues) {
        return keyHoldingValues.entrySet().stream()
                .map(e -> e.getValue().multiply(holdingValues.get(e.getKey())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * calculates denominator for correlation formula
     *
     * @param keyHoldingValues portfolio base total return values by period of key holding
     * @param holdingValues    portfolio base total return values by period of another holding needed for correlation
     * @return denominator
     */
    BigDecimal calculateDenominator(final Map<LocalDate, BigDecimal> keyHoldingValues,
                                    final Map<LocalDate, BigDecimal> holdingValues) {
        final BigDecimal holdingX = getSumOfSquaredValues(keyHoldingValues);
        final BigDecimal holdingY = getSumOfSquaredValues(holdingValues);
        return squareRoot(holdingX.multiply(holdingY));
    }

    /**
     * calculates sum of squared values
     *
     * @param holdingValues portfolio base total return values by period
     * @return sum of squared values
     */
    BigDecimal getSumOfSquaredValues(final Map<LocalDate, BigDecimal> holdingValues) {
        return holdingValues.values().stream().map(v -> pow(v, TWO)).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
