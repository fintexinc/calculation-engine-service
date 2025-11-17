package com.fintex.ce.domain.monthlyreturns;

import com.fintex.ce.config.enumeration.Currency;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.request.DailyPerformanceReqDTO;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.exception.DataErrorException;
import com.fintex.ce.exception.SystemException;
import com.fintex.ce.exception.code.ErrorCode;
import com.fintex.ce.exception.notification.pattern.Notification;
import com.fintex.ce.model.redis.RHistoricalNavPrices;
import com.fintex.ce.model.redis.ReturnsI;
import com.fintex.ce.util.MapUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static com.fintex.ce.config.enumeration.ExceptionCode.ERR_FDS_MC_002;
import static com.fintex.ce.config.enumeration.ExceptionCode.ERR_NAV_PRICES_002;
import static com.fintex.ce.config.enumeration.ExceptionCode.ERR_RRC_MMR_001;
import static com.fintex.ce.config.enumeration.ExceptionCode.ERR_RRC_MR_002;
import static com.fintex.ce.util.CollectorUtils.toMap;
import static com.fintex.ce.util.CollectorUtils.toTreeMap;
import static com.fintex.ce.util.DateTimeUtils.PATTERN_1;
import static com.fintex.ce.util.DateTimeUtils.toLastDayOfMonth;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@EqualsAndHashCode
public class Returns<T extends ReturnsI> {
    Notification notification = new Notification();
    Map<Holding, Currency> holdingCurrencyMap;
    Map<Holding, TreeMap<LocalDate, BigDecimal>> returnsMap;
    @Getter
    LocalDate ped;
    @Getter
    LocalDate psd;

    private ReturnsCutComponent monthlyReturnsCutComponent = new ReturnsCutComponent();
    private FxRatesConversionComponent fxRatesConversionComponent;
    private WeightedAverageComponent weightedAverageComponent;
    private CpedDataValidation cpedDataValidation;
    private CpsdDataValidation cpsdDataValidation;

    public Returns(Map<Holding, T> originalMonthlyReturns) {
        inCaseOfAnyErrorInRMonthlyReturnThrowAnException(originalMonthlyReturns); // see responseMapper method in MonthlyReturnsFundCanadaEndpoint etc
        this.holdingCurrencyMap = retrieveHoldingCurrencies(originalMonthlyReturns);
        this.returnsMap = retrieveReturns(originalMonthlyReturns);
        findPedAndPsd();
        validateReturns();
    }

    public static Returns<RHistoricalNavPrices> initForNavPrices(Map<Holding, RHistoricalNavPrices> returns) {
        var result = new Returns<RHistoricalNavPrices>();
        result.returnsMap = result.retrieveReturns(returns);
        result.findPedAndPsd();
        result.validateReturns();
        result.ifAnyErrorsThrowException();

        result.setMonthlyReturnsCutComponent(new ReturnsCutComponent());
        result.setCpsdDataValidation(new PortfolioCpsdDataValidation());
        result.setCpedDataValidation(new PortfolioCpedDataValidation());
        return result;
    }

    public static <T extends ReturnsI> Returns<T> initOnlyWithReturnsDataValidation(Map<Holding, T> originalMonthlyReturns) {
        var result = new Returns<T>();
        inCaseOfMonthlyReturnsErrorsInRMonthlyReturnsThrowAnException(originalMonthlyReturns);
        result.returnsMap = result.retrieveReturns(originalMonthlyReturns);
        result.findPedAndPsd();
        result.validateReturns();
        result.ifAnyErrorsThrowException();
        return result;
    }

    private static <T extends ReturnsI> void inCaseOfMonthlyReturnsErrorsInRMonthlyReturnsThrowAnException(Map<Holding, T> originalMonthlyReturns) {
        var notification = new Notification();
        originalMonthlyReturns.values()
                .stream()
                .filter(ReturnsI::hasMonthlyReturnsErrors)
                .forEach(rMonthlyReturns -> notification.addErrors(rMonthlyReturns.getOnlyMonthlyReturnsErrors()));
        notification.ifAnyNonAllowedErrorThrowException(List.of(ERR_RRC_MR_002, ERR_RRC_MMR_001, ERR_FDS_MC_002));
    }

    public Returns() {
    }

    private void inCaseOfAnyErrorInRMonthlyReturnThrowAnException(Map<Holding, T> originalMonthlyReturns) {
        originalMonthlyReturns.values()
                .forEach(rMonthlyReturn -> notification.addErrors(rMonthlyReturn.getErrors()));
        ifAnyErrorsThrowException();
    }

    public Returns<T> findPedAndPsd() {
        this.ped = findPedAmongMonthlyReturns();
        this.psd = findPsdAmongMonthlyReturns();
        return this;
    }

    public Returns<T> cutArgumentToTheSameEndDate(Returns<T> arg) {
        if (this.ped.isBefore(arg.getPed())) {
            arg.returnsMap = monthlyReturnsCutComponent.cutReturnsByEndDate(arg.returnsMap, this.ped);
            return arg.findPedAndPsd();
        }
        return arg;
    }

    public Returns<T> setFxRatesConversionComponent(FxRatesConversionComponent fxRatesConversionComponent) {
        this.fxRatesConversionComponent = fxRatesConversionComponent;
        return this;
    }

    public Returns<T> setMonthlyReturnsCutComponent(ReturnsCutComponent monthlyReturnsCutComponent) {
        this.monthlyReturnsCutComponent = monthlyReturnsCutComponent;
        return this;
    }

    public Returns<T> setWeightedAverageComponent(WeightedAverageComponent weightedAverageComponent) {
        this.weightedAverageComponent = weightedAverageComponent;
        return this;
    }

    public Returns<T> setCpedDataValidation(CpedDataValidation cpedDataValidation) {
        this.cpedDataValidation = cpedDataValidation;
        return this;
    }

    public Returns<T> setCpsdDataValidation(CpsdDataValidation cpsdDataValidation) {
        this.cpsdDataValidation = cpsdDataValidation;
        return this;
    }

    public Returns<T> fxRatesApplied() {
        ifAnyErrorsThrowException();
        returnsMap = fxRatesConversionComponent.convert(returnsMap, holdingCurrencyMap);
        return this;
    }

    public Returns<T> cutByCpedIfCpedEmptyCutByPed(LocalDate cped) {
        returnsMap = monthlyReturnsCutComponent.cutReturnsByEndDate(returnsMap, ped);
        if (Objects.nonNull(cped)) {
            returnsMap = monthlyReturnsCutComponent.cutReturnsByEndDate(returnsMap, cped);
        }
        return this;
    }

    public Returns<T> cutByPed() {
        returnsMap = monthlyReturnsCutComponent.cutReturnsByEndDate(returnsMap, ped);
        return this;
    }

    public Returns<T> cutByPsd() {
        returnsMap = monthlyReturnsCutComponent.cutReturnsByStartDate(returnsMap, psd);
        return this;
    }

    public Returns<T> cutByCpsdIfCpsdEmptyCutByPsd(LocalDate cpsd) {
        LocalDate startDate = Objects.isNull(cpsd) ? psd : cpsd;
        returnsMap = monthlyReturnsCutComponent.cutReturnsByStartDate(returnsMap, startDate);
        return this;
    }

    public NavigableMap<LocalDate, BigDecimal> getWeightedAverage() {
        ifAnyErrorsThrowException();
        return weightedAverageComponent.calculateWeightedAverage(returnsMap);
    }

    public Returns<T> validateCped(LocalDate cped) {
        cpedDataValidation.validate(cped, psd, ped, notification);
        return this;
    }

    public Returns<T> validateCpsd(LocalDate cpsd) {
        cpsdDataValidation.validate(cpsd, psd, ped, notification);
        return this;
    }

    public Returns<T> validateAndUpdateCpsdAndCped(final Map<Holding, RHistoricalNavPrices> navData,
                                                final DailyPerformanceReqDTO reqDTO) {
        validateEarliestAndLatestAvailableDate(navData, reqDTO);
        return this;
    }

    public void validateEarliestAndLatestAvailableDate(final Map<Holding, RHistoricalNavPrices> navData, final DailyPerformanceReqDTO reqDTO) {
        final LocalDate startDate = getStartDate(reqDTO);
        final LocalDate endDate = getEndDate(reqDTO);
        ifAnyErrorsThrowException();

        returnsMap.forEach((key, value) -> {
            final RHistoricalNavPrices navPrices = navData.get(key);
            final LocalDate earliestAvailableDate = getEarliestAvailableDate(startDate, navPrices, value);
            if (earliestAvailableDate.isAfter(reqDTO.getStartDate())) {
                reqDTO.setStartDate(earliestAvailableDate);
            }
            final LocalDate latestAvailableDate = getLatestAvailableDate(endDate, navPrices, value);
            if (latestAvailableDate.isBefore(reqDTO.getEndDate())) {
                reqDTO.setEndDate(latestAvailableDate);
            }
        });
    }

    private LocalDate getStartDate(final DailyPerformanceReqDTO reqDTO) {
        final LocalDate startDate = Optional.ofNullable(reqDTO.getStartDate()).orElse(psd);
        LocalDate newStartDate = LocalDate.of(startDate.getYear(), startDate.getMonth(), 1);
        if (newStartDate.isBefore(psd)) {
            newStartDate = newStartDate.plusMonths(1);
        }
        validateCpsd(newStartDate);
        reqDTO.setStartDate(newStartDate);
        return newStartDate;
    }

    private LocalDate getEndDate(final DailyPerformanceReqDTO reqDTO) {
        LocalDate newEndDate = toLastDayOfMonth(Optional.ofNullable(reqDTO.getEndDate()).orElse(ped));
        if (newEndDate.isAfter(ped)) {
            newEndDate = toLastDayOfMonth(newEndDate.minusMonths(1));
        }
        validateCped(newEndDate);
        reqDTO.setEndDate(newEndDate);
        return newEndDate;
    }

    private LocalDate getEarliestAvailableDate(final LocalDate startDate, final RHistoricalNavPrices navPrices, final TreeMap<LocalDate, BigDecimal> value) {
        if (Objects.isNull(startDate) || Objects.isNull(value)) {
            throw new SystemException(
                    "Can't obtain Earliest Available Date. Missed one of the parameters. Start Date: %s. Value: %s".formatted(startDate, value),
                    ErrorCode.INTERNAL_SERVER_ERROR);
        }

        for (LocalDate date = startDate; date.isBefore(value.lastKey()); date = date.plusDays(1)) {
            if (!navPrices.getMissedDates().contains(date)) {
                return date;
            }
        }
        return startDate;
    }

    private LocalDate getLatestAvailableDate(final LocalDate endDate, final RHistoricalNavPrices navPrices, final TreeMap<LocalDate, BigDecimal> value) {
        if (Objects.isNull(endDate) || Objects.isNull(value)) {
            throw new SystemException(
                    "Can't obtain Latest Available Date. Missed one of the parameters. End Date: %s. Value: %s".formatted(endDate, value),
                    ErrorCode.INTERNAL_SERVER_ERROR);
        }

        for (LocalDate date = endDate; date.isAfter(value.firstKey()); date = date.minusDays(1)) {
            if (!navPrices.getMissedDates().contains(date)) {
                return date;
            }
        }
        return endDate;
    }

    public Map<Holding, TreeMap<LocalDate, BigDecimal>> getReturnsMap() {
        return returnsMap.entrySet().stream().collect(toMap(Map.Entry::getKey, entry -> MapUtils.copyTreeMap(entry.getValue(), TreeMap::new)));
    }

    public Map<Holding, TreeMap<LocalDate, BigDecimal>> getOriginalReturns() {
        return returnsMap;
    }

    /**
     * Find the earliest common start date (PSD) for monthlyReturns
     *
     * @return earliest common start date (PSD)
     */
    LocalDate findPsdAmongMonthlyReturns() {
        return this.returnsMap.values()
                .stream()
                .map(e -> e.keySet().stream().min(LocalDate::compareTo))
                .filter(Optional::isPresent).map(Optional::get)
                .max(LocalDate::compareTo).orElse(null);
    }

    /**
     * Find last common date (PED) for monthlyReturns
     *
     * @return last common end date (PED)
     */
    public LocalDate findPed(Map<Holding, TreeMap<LocalDate, BigDecimal>> monthlyReturns) {
        return monthlyReturns.values()
                .stream()
                .map(e -> e.keySet().stream().max(LocalDate::compareTo))
                .filter(Optional::isPresent).map(Optional::get)
                .min(LocalDate::compareTo).orElse(null);
    }

    public LocalDate findPedAmongMonthlyReturns() {
        return findPed(this.returnsMap);
    }

    Map<Holding, Currency> retrieveHoldingCurrencies(Map<Holding, T> originalMReturns) {
        Map<Boolean, Map<Holding, T>> partitionedHoldings = originalMReturns.entrySet()
                .stream()
                .collect(Collectors.partitioningBy(e -> Objects.nonNull(getCurrency(e)), Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                )));
        partitionedHoldings.get(false).forEach((key, value) -> notification.addError(ERR_FDS_MC_002.error(key)));
        return partitionedHoldings.get(true).entrySet()
                .stream()
                .collect(HashMap::new, (m, entry) -> m.put(entry.getKey(), getCurrency(entry)), HashMap::putAll);
    }

    private Currency getCurrency(Map.Entry<Holding, T> e) {
        String currency = e.getValue().getCurrency();
        return Currency.get(currency);
    }

    Map<Holding, TreeMap<LocalDate, BigDecimal>> retrieveReturns(Map<Holding, T> originalMReturns) {
        return originalMReturns.entrySet()
                .stream()
                .filter(k -> Objects.nonNull(k.getValue().getReturns()))
                .collect(toMap(Map.Entry::getKey, e -> e.getValue().getReturns().entrySet().stream().collect(toTreeMap(Map.Entry::getKey, Map.Entry::getValue))));
    }

    public Returns<T> ifAnyErrorsThrowException() {
        notification.ifAnyNonAllowedErrorThrowException(List.of(ERR_RRC_MR_002, ERR_RRC_MMR_001, ERR_FDS_MC_002));
        return this;
    }

    public Returns<T> validateReturns() {
        Map<LocalDate, List<Map.Entry<Holding, TreeMap<LocalDate, BigDecimal>>>> startDateEntriesMap =
                returnsMap.entrySet()
                        .stream()
                        .collect(groupingBy(e -> e.getValue().firstKey(), toList()));
        if (Objects.nonNull(psd) && Objects.nonNull(ped)) {
            while (psd.isAfter(ped)) {
                var entries = startDateEntriesMap.get(psd);
                for (Map.Entry<Holding, TreeMap<LocalDate, BigDecimal>> entry : entries) {
                    notification.addError(ERR_RRC_MR_002.error(entry.getKey()));
                    returnsMap.remove(entry.getKey());
                }
                findPedAndPsd();
            }
        }
        ifAnyErrorsThrowException();
        return this;
    }

    public Returns<T> validateMonthlyDataMissing(final Map<Holding, RHistoricalNavPrices> navData, final DailyPerformanceReqDTO reqDTO) {
        final long holdingsWithPacOrWithdrawals = reqDTO.getDailyHoldings().stream()
                .filter(dh -> (!dh.getWithdrawal().equals(BigDecimal.ZERO) && !dh.getPac().equals(BigDecimal.ZERO)))
                .count();
        if (holdingsWithPacOrWithdrawals != 0) {
            navData.forEach((key, value) ->
                    value.getMissedMonthData().stream()
                            .filter(m -> m.isAfter(reqDTO.getStartDate()) && m.isBefore(reqDTO.getEndDate()))
                            .forEach(m -> notification.addError(ERR_NAV_PRICES_002.error(key, m.format(PATTERN_1)))));
        }
        return this;
    }

    public List<DataErrorException> getErrors() {
        return notification.getErrors();
    }

    public List<Warning> getErrorsAsWarnings() {
        return notification.getErrors().stream().map(error -> new Warning(error.getId(), error.getMessage(), error.getCode().name())).toList();
    }
}
