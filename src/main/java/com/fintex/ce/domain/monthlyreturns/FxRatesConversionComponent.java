package com.fintex.ce.domain.monthlyreturns;

import com.fintex.smclient.dto.FxRatesDTO;
import com.fintex.ce.config.enumeration.Currency;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.util.PortfolioUtils;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static com.fintex.ce.config.constant.BigDecimalConstants.HUNDRED;
import static com.fintex.ce.config.enumeration.ExceptionCode.ERR_RRC_MFR_001;
import static com.fintex.ce.util.CollectorUtils.toMap;
import static com.fintex.ce.util.CollectorUtils.toTreeMap;
import static com.fintex.ce.util.DateTimeUtils.toLastDayOfMonth;
import static com.fintex.ce.util.DecimalUtils.divide;
import static java.math.BigDecimal.ONE;

@EqualsAndHashCode
public class FxRatesConversionComponent {

    private final Map<LocalDate, FxRatesDTO> fxRates;
    private final Currency toCurrency;

    public FxRatesConversionComponent(final Map<LocalDate, FxRatesDTO> fxRates,
                                      final Currency toCurrency) {
        this.fxRates = makeCopy(fxRates);
        this.toCurrency = toCurrency;
    }

    public Map<Holding, TreeMap<LocalDate, BigDecimal>> convert(final Map<Holding, TreeMap<LocalDate, BigDecimal>> returns,
                                                                final Map<Holding, Currency> holdingCurrencies) {
        final Map<Holding, Map<LocalDate, BigDecimal>> mapperFxRates = PortfolioUtils.fxRatesForHoldings(holdingCurrencies, toCurrency, fxRates);
        return getHoldingMapMap(returns, mapperFxRates);
    }


    private Map<Holding, TreeMap<LocalDate, BigDecimal>> getHoldingMapMap(Map<Holding, TreeMap<LocalDate, BigDecimal>> mReturns, Map<Holding, Map<LocalDate, BigDecimal>> mappedFxRates) {
        return mReturns.entrySet().stream().collect(toMap(Map.Entry::getKey, entry -> holdingPortfolioBaseTotalReturn(mappedFxRates.get(entry.getKey()), entry.getValue())));
    }

    private TreeMap<LocalDate, BigDecimal> holdingPortfolioBaseTotalReturn(final Map<LocalDate, BigDecimal> fxRates, final Map<LocalDate, BigDecimal> pReturns) {
        return pReturns.entrySet().stream().collect(toTreeMap(Map.Entry::getKey, entry -> holdingPortfolioBaseTotalReturnFormula(entry.getKey(), entry.getValue(), fxRates)));
    }

    private BigDecimal holdingPortfolioBaseTotalReturnFormula(final LocalDate date, final BigDecimal value, final Map<LocalDate, BigDecimal> fxRates) {
        final LocalDate previousDate = toLastDayOfMonth(date.minusMonths(1));
        final BigDecimal fxRateValue = validateFxRates(date, fxRates.get(date));
        final BigDecimal previousFxValue = validateFxRates(previousDate, fxRates.get(previousDate));
        final BigDecimal subtract = ONE.add(value).multiply(divide(fxRateValue, previousFxValue)).subtract(ONE);
        return subtract.multiply(HUNDRED);
    }

    private BigDecimal validateFxRates(final LocalDate date, final BigDecimal fxRateValue) {
        if (fxRateValue == null) {
            throw ERR_RRC_MFR_001.error(date);
        }
        return fxRateValue;
    }

    private Map<LocalDate, FxRatesDTO> makeCopy(final Map<LocalDate, FxRatesDTO> fxRates) {
        return fxRates.entrySet().stream().collect(
                Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> new FxRatesDTO(entry.getValue().getUsdCad(), entry.getValue().getCadUsd())
                ));
    }
}
