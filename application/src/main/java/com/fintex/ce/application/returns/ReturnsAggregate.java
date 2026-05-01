package com.fintex.ce.application.returns;

import com.fintex.ce.application.calculation.service.FxRateService;
import com.fintex.ce.application.util.MapUtils;
import com.fintex.ce.application.validation.CpedDataValidation;
import com.fintex.ce.application.validation.CpsdDataValidation;
import com.fintex.ce.application.validation.PortfolioCpedDataValidation;
import com.fintex.ce.application.validation.PortfolioCpsdDataValidation;
import com.fintex.ce.model.domain.CurrencyExchangePair;
import com.fintex.ce.model.domain.calculation.returns.HistoricalNavPrices;
import com.fintex.ce.model.domain.calculation.returns.ReturnsData;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.dto.command.DailyPerformanceCommand;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.PceExceptionCollector;
import com.fintex.ce.model.error.Warning;
import com.fintex.ce.model.error.exceptions.BasePceException;
import com.fintex.ce.model.error.exceptions.CalculationException;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.error.Notification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import static com.fintex.ce.application.util.CollectorUtils.toMap;
import static com.fintex.ce.application.util.CollectorUtils.toTreeMap;
import static com.fintex.ce.model.error.ErrorCode.FX_RATES_UNAVAILABLE;
import static com.fintex.ce.model.error.ErrorCode.HOLDING_MISSING_CURRENCY_FROM_FDS;
import static com.fintex.ce.model.error.ErrorCode.HOLDING_PSD_OUT_OF_RANGE;
import static com.fintex.ce.model.error.ErrorCode.MISSING_HISTORICAL_NAV_PRICES_FOR_MONTH;
import static com.fintex.ce.model.error.ErrorCode.MISSING_MONTHLY_RETURNS;
import static com.fintex.ce.model.error.ErrorCode.NAV_PARAM_MISSING;
import static com.fintex.ce.util.DateTimeUtils.PATTERN_1;
import static com.fintex.ce.util.DateTimeUtils.toLastDayOfMonth;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@EqualsAndHashCode
public class ReturnsAggregate<T extends ReturnsData> {
  public PceExceptionCollector notification = new PceExceptionCollector();
  public Map<PortfolioHolding, Currency> holdingCurrencyMap;
  public Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> returnsMap;
  @Getter
  public LocalDate performanceEndDate;
  @Getter
  public LocalDate performanceStartDate;

  private ReturnsCutComponent monthlyReturnsCutComponent = new ReturnsCutComponent();
  private FxRateService fxRateService;
  private Map<CurrencyExchangePair, NavigableMap<LocalDate, BigDecimal>> fxRates;
  private Currency fxTargetCurrency;
  private WeightedAverageComponent weightedAverageComponent;
  private CpedDataValidation cpedDataValidation;
  private CpsdDataValidation cpsdDataValidation;

  public ReturnsAggregate() {
  }

  public ReturnsAggregate(Map<PortfolioHolding, T> originalMonthlyReturns) {
    inCaseOfAnyErrorInReturnsThrowAnException(originalMonthlyReturns);
    this.holdingCurrencyMap = retrieveHoldingCurrencies(originalMonthlyReturns);
    this.returnsMap = retrieveReturns(originalMonthlyReturns);
    findPedAndPsd();
    validateReturns();
  }

  public static ReturnsAggregate<HistoricalNavPrices> initForNavPrices(
      Map<PortfolioHolding, HistoricalNavPrices> returns) {
    var result = new ReturnsAggregate<HistoricalNavPrices>();
    result.returnsMap = result.retrieveReturns(returns);
    result.findPedAndPsd();
    result.validateReturns();
    result.ifAnyErrorsThrowException();

    result.setMonthlyReturnsCutComponent(new ReturnsCutComponent());
    result.setCpsdDataValidation(new PortfolioCpsdDataValidation());
    result.setCpedDataValidation(new PortfolioCpedDataValidation());
    return result;
  }

  public static <T extends ReturnsData> ReturnsAggregate<T> initOnlyWithReturnsDataValidation(
      Map<PortfolioHolding, T> originalMonthlyReturns) {
    var result = new ReturnsAggregate<T>();
    inCaseOfMonthlyReturnsErrorsThrowAnException(originalMonthlyReturns);
    result.returnsMap = result.retrieveReturns(originalMonthlyReturns);
    result.findPedAndPsd();
    result.validateReturns();
    result.ifAnyErrorsThrowException();
    return result;
  }

  private static <T extends ReturnsData> void inCaseOfMonthlyReturnsErrorsThrowAnException(
      Map<PortfolioHolding, T> originalMonthlyReturns) {
    var notification = new PceExceptionCollector();
    originalMonthlyReturns.values()
        .stream()
        .filter(ReturnsData::hasMonthlyReturnsErrors)
        .forEach(returns -> notification.addAll(convertToCalculationExceptions(returns
            .getOnlyMonthlyReturnsErrors())));
    notification.throwIfAnyNonAllowed(List.of(HOLDING_PSD_OUT_OF_RANGE, MISSING_MONTHLY_RETURNS,
        HOLDING_MISSING_CURRENCY_FROM_FDS));
  }

  private void inCaseOfAnyErrorInReturnsThrowAnException(Map<PortfolioHolding, T> originalMonthlyReturns) {
    originalMonthlyReturns.values()
        .forEach(returns -> notification.addAll(convertToCalculationExceptions(returns.getErrors())));
    ifAnyErrorsThrowException();
  }

  private static List<CalculationException> convertToCalculationExceptions(List<Notification> errors) {
    if (errors == null) {
      return List.of();
    }
    return errors.stream()
        .map(n -> ErrorCode.fromCode(n.getCode()).toExceptionForId(n.getUuid()))
        .toList();
  }

  public ReturnsAggregate<T> findPedAndPsd() {
    this.performanceEndDate = findPedAmongMonthlyReturns();
    this.performanceStartDate = findPsdAmongMonthlyReturns();
    return this;
  }

  public ReturnsAggregate<T> cutArgumentToTheSameEndDate(ReturnsAggregate<T> arg) {
    if (this.performanceEndDate.isBefore(arg.getPerformanceEndDate())) {
      arg.returnsMap = monthlyReturnsCutComponent.cutReturnsByEndDate(arg.returnsMap, this.performanceEndDate);
      return arg.findPedAndPsd();
    }
    return arg;
  }

  public ReturnsAggregate<T> setFxRateService(FxRateService fxRateService) {
    this.fxRateService = fxRateService;
    return this;
  }

  public ReturnsAggregate<T> setFxRates(Map<CurrencyExchangePair, NavigableMap<LocalDate, BigDecimal>> fxRates,
      Currency toCurrency) {
    this.fxRates = fxRates;
    this.fxTargetCurrency = toCurrency;
    return this;
  }

  public ReturnsAggregate<T> setMonthlyReturnsCutComponent(ReturnsCutComponent monthlyReturnsCutComponent) {
    this.monthlyReturnsCutComponent = monthlyReturnsCutComponent;
    return this;
  }

  public ReturnsAggregate<T> setWeightedAverageComponent(WeightedAverageComponent weightedAverageComponent) {
    this.weightedAverageComponent = weightedAverageComponent;
    return this;
  }

  public ReturnsAggregate<T> setCpedDataValidation(CpedDataValidation cpedDataValidation) {
    this.cpedDataValidation = cpedDataValidation;
    return this;
  }

  public ReturnsAggregate<T> setCpsdDataValidation(CpsdDataValidation cpsdDataValidation) {
    this.cpsdDataValidation = cpsdDataValidation;
    return this;
  }

  public ReturnsAggregate<T> fxRatesApplied() {
    ifAnyErrorsThrowException();
    returnsMap = fxRateService.convertReturns(returnsMap, holdingCurrencyMap, fxRates, fxTargetCurrency, notification);
    updateCurrenciesAfterConversion();
    return this;
  }

  private void updateCurrenciesAfterConversion() {
    if (fxTargetCurrency == null) {
      return;
    }
    Set<String> failedHoldingIds = notification.getExceptions().stream()
        .filter(e -> e.getErrorCode() == ErrorCode.FX_RATES_UNAVAILABLE)
        .map(BasePceException::getId)
        .collect(Collectors.toSet());
    holdingCurrencyMap.replaceAll((holding, current) -> {
      if (current == null || current.equals(fxTargetCurrency)) {
        return current;
      }
      return failedHoldingIds.contains(holding.getIdsString()) ? current : fxTargetCurrency;
    });
  }

  public ReturnsAggregate<T> cutByCpedIfCpedEmptyCutByPed(LocalDate cped) {
    returnsMap = monthlyReturnsCutComponent.cutReturnsByEndDate(returnsMap, performanceEndDate);
    if (Objects.nonNull(cped)) {
      returnsMap = monthlyReturnsCutComponent.cutReturnsByEndDate(returnsMap, cped);
    }
    return this;
  }

  public ReturnsAggregate<T> cutByPed() {
    returnsMap = monthlyReturnsCutComponent.cutReturnsByEndDate(returnsMap, performanceEndDate);
    return this;
  }

  public ReturnsAggregate<T> cutByPsd() {
    returnsMap = monthlyReturnsCutComponent.cutReturnsByStartDate(returnsMap, performanceStartDate);
    return this;
  }

  public ReturnsAggregate<T> cutByCpsdIfCpsdEmptyCutByPsd(LocalDate cpsd) {
    LocalDate startDate = Objects.isNull(cpsd) ? performanceStartDate : cpsd;
    returnsMap = monthlyReturnsCutComponent.cutReturnsByStartDate(returnsMap, startDate);
    return this;
  }

  public NavigableMap<LocalDate, BigDecimal> getWeightedAverage() {
    ifAnyErrorsThrowException();
    return weightedAverageComponent.calculateWeightedAverage(returnsMap);
  }

  public ReturnsAggregate<T> validateCped(LocalDate cped) {
    cpedDataValidation.validate(cped, performanceStartDate, performanceEndDate, notification);
    return this;
  }

  public ReturnsAggregate<T> validateCpsd(LocalDate cpsd) {
    cpsdDataValidation.validate(cpsd, performanceStartDate, performanceEndDate, notification);
    return this;
  }

  public ReturnsAggregate<T> validateAndUpdateCpsdAndCped(final Map<PortfolioHolding, HistoricalNavPrices> navData,
      final DailyPerformanceCommand command) {
    validateEarliestAndLatestAvailableDate(navData, command);
    return this;
  }

  public void validateEarliestAndLatestAvailableDate(final Map<PortfolioHolding, HistoricalNavPrices> navData,
      final DailyPerformanceCommand command) {
    final LocalDate startDate = getStartDate(command);
    final LocalDate endDate = getEndDate(command);
    ifAnyErrorsThrowException();

    returnsMap.forEach((key, value) -> {
      final HistoricalNavPrices navPrices = navData.get(key);
      final LocalDate earliestAvailableDate = getEarliestAvailableDate(startDate, navPrices, value);
      if (earliestAvailableDate.isAfter(command.getStartDate())) {
        command.setStartDate(earliestAvailableDate);
      }
      final LocalDate latestAvailableDate = getLatestAvailableDate(endDate, navPrices, value);
      if (latestAvailableDate.isBefore(command.getEndDate())) {
        command.setEndDate(latestAvailableDate);
      }
    });
  }

  private LocalDate getStartDate(final DailyPerformanceCommand command) {
    final LocalDate startDate = Optional.ofNullable(command.getStartDate()).orElse(performanceStartDate);
    LocalDate newStartDate = LocalDate.of(startDate.getYear(), startDate.getMonth(), 1);
    if (newStartDate.isBefore(performanceStartDate)) {
      newStartDate = newStartDate.plusMonths(1);
    }
    validateCpsd(newStartDate);
    command.setStartDate(newStartDate);
    return newStartDate;
  }

  private LocalDate getEndDate(final DailyPerformanceCommand command) {
    LocalDate newEndDate = toLastDayOfMonth(Optional.ofNullable(command.getEndDate()).orElse(performanceEndDate));
    if (newEndDate.isAfter(performanceEndDate)) {
      newEndDate = toLastDayOfMonth(newEndDate.minusMonths(1));
    }
    validateCped(newEndDate);
    command.setEndDate(newEndDate);
    return newEndDate;
  }

  private LocalDate getEarliestAvailableDate(final LocalDate startDate, final HistoricalNavPrices navPrices,
      final TreeMap<LocalDate, BigDecimal> value) {
    if (Objects.isNull(startDate) || Objects.isNull(value)) {
      throw NAV_PARAM_MISSING.toException(
          Objects.isNull(startDate) ? "startDate" : "value",
          Objects.isNull(startDate) ? startDate : value);
    }

    for (LocalDate date = startDate; date.isBefore(value.lastKey()); date = date.plusDays(1)) {
      if (!navPrices.getMissedDates().contains(date)) {
        return date;
      }
    }
    return startDate;
  }

  private LocalDate getLatestAvailableDate(final LocalDate endDate, final HistoricalNavPrices navPrices,
      final TreeMap<LocalDate, BigDecimal> value) {
    if (Objects.isNull(endDate) || Objects.isNull(value)) {
      throw NAV_PARAM_MISSING.toException(
          Objects.isNull(endDate) ? "endDate" : "value",
          Objects.isNull(endDate) ? endDate : value);
    }

    for (LocalDate date = endDate; date.isAfter(value.firstKey()); date = date.minusDays(1)) {
      if (!navPrices.getMissedDates().contains(date)) {
        return date;
      }
    }
    return endDate;
  }

  public Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> getReturnsMap() {
    return returnsMap.entrySet().stream().collect(toMap(Map.Entry::getKey, entry -> MapUtils.copyTreeMap(entry
        .getValue(), TreeMap::new)));
  }

  public Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> getOriginalReturns() {
    return returnsMap;
  }

  public LocalDate findPsdAmongMonthlyReturns() {
    return this.returnsMap.values()
        .stream()
        .map(e -> e.keySet().stream().min(LocalDate::compareTo))
        .filter(Optional::isPresent).map(Optional::get)
        .max(LocalDate::compareTo).orElse(null);
  }

  public LocalDate findPed(Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> monthlyReturns) {
    return monthlyReturns.values()
        .stream()
        .map(e -> e.keySet().stream().max(LocalDate::compareTo))
        .filter(Optional::isPresent).map(Optional::get)
        .min(LocalDate::compareTo).orElse(null);
  }

  public LocalDate findPedAmongMonthlyReturns() {
    return findPed(this.returnsMap);
  }

  public Map<PortfolioHolding, Currency> retrieveHoldingCurrencies(Map<PortfolioHolding, T> originalMReturns) {
    Map<Boolean, Map<PortfolioHolding, T>> partitionedHoldings = originalMReturns.entrySet()
        .stream()
        .collect(Collectors.partitioningBy(e -> Objects.nonNull(getCurrency(e)), Collectors.toMap(
            Map.Entry::getKey,
            Map.Entry::getValue)));
    partitionedHoldings.get(false).forEach((key, value) -> notification.add(HOLDING_MISSING_CURRENCY_FROM_FDS
        .toExceptionForHolding(key)));
    return partitionedHoldings.get(true).entrySet()
        .stream()
        .collect(HashMap::new, (m, entry) -> m.put(entry.getKey(), getCurrency(entry)), HashMap::putAll);
  }

  private Currency getCurrency(Map.Entry<PortfolioHolding, T> e) {
    String currency = e.getValue().getCurrency();
    return Currency.fromValueOrNull(currency);
  }

  public Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> retrieveReturns(
      Map<PortfolioHolding, T> originalMReturns) {
    return originalMReturns.entrySet()
        .stream()
        .filter(k -> Objects.nonNull(k.getValue().getReturns()))
        .collect(toMap(Map.Entry::getKey, e -> e.getValue().getReturns().entrySet().stream().collect(toTreeMap(
            Map.Entry::getKey, Map.Entry::getValue))));
  }

  public ReturnsAggregate<T> ifAnyErrorsThrowException() {
    notification.throwIfAnyNonAllowed(List.of(HOLDING_PSD_OUT_OF_RANGE, MISSING_MONTHLY_RETURNS,
        HOLDING_MISSING_CURRENCY_FROM_FDS, FX_RATES_UNAVAILABLE));
    return this;
  }

  public ReturnsAggregate<T> validateReturns() {
    Map<LocalDate, List<Map.Entry<PortfolioHolding, TreeMap<LocalDate, BigDecimal>>>> startDateEntriesMap = returnsMap
        .entrySet()
        .stream()
        .collect(groupingBy(e -> e.getValue().firstKey(), toList()));
    if (Objects.nonNull(performanceStartDate) && Objects.nonNull(performanceEndDate)) {
      while (performanceStartDate.isAfter(performanceEndDate)) {
        var entries = startDateEntriesMap.get(performanceStartDate);
        for (Map.Entry<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> entry : entries) {
          notification.add(HOLDING_PSD_OUT_OF_RANGE.toExceptionForHolding(entry.getKey()));
          returnsMap.remove(entry.getKey());
        }
        findPedAndPsd();
      }
    }
    ifAnyErrorsThrowException();
    return this;
  }

  public ReturnsAggregate<T> validateMonthlyDataMissing(final Map<PortfolioHolding, HistoricalNavPrices> navData,
      final DailyPerformanceCommand command) {
    final long holdingsWithPacOrWithdrawals = command.getDailyHoldings().stream()
        .filter(dh -> (!dh.getWithdrawal().equals(BigDecimal.ZERO) && !dh.getPac().equals(BigDecimal.ZERO)))
        .count();
    if (holdingsWithPacOrWithdrawals != 0) {
      navData.forEach((key, value) -> value.getMissedMonthData().stream()
          .filter(m -> m.isAfter(command.getStartDate()) && m.isBefore(command.getEndDate()))
          .forEach(m -> notification.add(MISSING_HISTORICAL_NAV_PRICES_FOR_MONTH.toExceptionForHolding(key, m.format(
              PATTERN_1)))));
    }
    return this;
  }

  public List<BasePceException> getErrors() {
    return notification.getExceptions();
  }

  public List<Warning> getErrorsAsWarnings() {
    return notification.getExceptions().stream()
        .map(error -> new Warning(error.getId(), error.getMessage(), error.getErrorCode().getCode()))
        .toList();
  }
}
