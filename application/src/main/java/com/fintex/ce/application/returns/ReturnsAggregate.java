package com.fintex.ce.application.returns;

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
import com.fintex.ce.model.error.HttpCode;
import com.fintex.ce.model.error.Notification;
import com.fintex.ce.model.error.ValidationError;
import com.fintex.ce.model.error.Warning;
import com.fintex.ce.model.error.exceptions.DataErrorException;
import com.fintex.ce.model.error.exceptions.SystemException;
import com.fintex.ce.util.MapUtils;
import com.fintex.wm.commons.domain.currency.Currency;

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
import lombok.EqualsAndHashCode;
import lombok.Getter;

import static com.fintex.ce.model.error.ErrorCode.ERR_FDS_MC_002;
import static com.fintex.ce.model.error.ErrorCode.ERR_NAV_PRICES_002;
import static com.fintex.ce.model.error.ErrorCode.ERR_RRC_MMR_001;
import static com.fintex.ce.model.error.ErrorCode.ERR_RRC_MR_002;
import static com.fintex.ce.util.CollectorUtils.toMap;
import static com.fintex.ce.util.CollectorUtils.toTreeMap;
import static com.fintex.ce.util.DateTimeUtils.PATTERN_1;
import static com.fintex.ce.util.DateTimeUtils.toLastDayOfMonth;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@EqualsAndHashCode
public class ReturnsAggregate<T extends ReturnsData> {
  public Notification notification = new Notification();
  public Map<PortfolioHolding, Currency> holdingCurrencyMap;
  public Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> returnsMap;
  @Getter
  public LocalDate ped;
  @Getter
  public LocalDate psd;

  private ReturnsCutComponent monthlyReturnsCutComponent = new ReturnsCutComponent();
  private FxRatesConversionComponent fxRatesConversionComponent;
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
    var notification = new Notification();
    originalMonthlyReturns.values()
        .stream()
        .filter(ReturnsData::hasMonthlyReturnsErrors)
        .forEach(returns -> notification.addErrors(convertToDataErrorExceptions(returns
            .getOnlyMonthlyReturnsErrors())));
    notification.ifAnyNonAllowedErrorThrowException(List.of(ERR_RRC_MR_002, ERR_RRC_MMR_001, ERR_FDS_MC_002));
  }

  private void inCaseOfAnyErrorInReturnsThrowAnException(Map<PortfolioHolding, T> originalMonthlyReturns) {
    originalMonthlyReturns.values()
        .forEach(returns -> notification.addErrors(convertToDataErrorExceptions(returns.getErrors())));
    ifAnyErrorsThrowException();
  }

  private static List<DataErrorException> convertToDataErrorExceptions(List<ValidationError> errors) {
    if (errors == null) {
      return List.of();
    }
    return errors.stream()
        .map(e -> new DataErrorException(e.getId(), e.getMessage(),
            ErrorCode.valueOf(e.getCode())))
        .toList();
  }

  public ReturnsAggregate<T> findPedAndPsd() {
    this.ped = findPedAmongMonthlyReturns();
    this.psd = findPsdAmongMonthlyReturns();
    return this;
  }

  public ReturnsAggregate<T> cutArgumentToTheSameEndDate(ReturnsAggregate<T> arg) {
    if (this.ped.isBefore(arg.getPed())) {
      arg.returnsMap = monthlyReturnsCutComponent.cutReturnsByEndDate(arg.returnsMap, this.ped);
      return arg.findPedAndPsd();
    }
    return arg;
  }

  public ReturnsAggregate<T> setFxRatesConversionComponent(FxRatesConversionComponent fxRatesConversionComponent) {
    this.fxRatesConversionComponent = fxRatesConversionComponent;
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
    returnsMap = fxRatesConversionComponent.convert(returnsMap, holdingCurrencyMap, fxRates, fxTargetCurrency);
    return this;
  }

  public ReturnsAggregate<T> cutByCpedIfCpedEmptyCutByPed(LocalDate cped) {
    returnsMap = monthlyReturnsCutComponent.cutReturnsByEndDate(returnsMap, ped);
    if (Objects.nonNull(cped)) {
      returnsMap = monthlyReturnsCutComponent.cutReturnsByEndDate(returnsMap, cped);
    }
    return this;
  }

  public ReturnsAggregate<T> cutByPed() {
    returnsMap = monthlyReturnsCutComponent.cutReturnsByEndDate(returnsMap, ped);
    return this;
  }

  public ReturnsAggregate<T> cutByPsd() {
    returnsMap = monthlyReturnsCutComponent.cutReturnsByStartDate(returnsMap, psd);
    return this;
  }

  public ReturnsAggregate<T> cutByCpsdIfCpsdEmptyCutByPsd(LocalDate cpsd) {
    LocalDate startDate = Objects.isNull(cpsd) ? psd : cpsd;
    returnsMap = monthlyReturnsCutComponent.cutReturnsByStartDate(returnsMap, startDate);
    return this;
  }

  public NavigableMap<LocalDate, BigDecimal> getWeightedAverage() {
    ifAnyErrorsThrowException();
    return weightedAverageComponent.calculateWeightedAverage(returnsMap);
  }

  public ReturnsAggregate<T> validateCped(LocalDate cped) {
    cpedDataValidation.validate(cped, psd, ped, notification);
    return this;
  }

  public ReturnsAggregate<T> validateCpsd(LocalDate cpsd) {
    cpsdDataValidation.validate(cpsd, psd, ped, notification);
    return this;
  }

  public ReturnsAggregate<T> validateAndUpdateCpsdAndCped(final Map<PortfolioHolding, HistoricalNavPrices> navData,
      final DailyPerformanceCommand reqDTO) {
    validateEarliestAndLatestAvailableDate(navData, reqDTO);
    return this;
  }

  public void validateEarliestAndLatestAvailableDate(final Map<PortfolioHolding, HistoricalNavPrices> navData,
      final DailyPerformanceCommand reqDTO) {
    final LocalDate startDate = getStartDate(reqDTO);
    final LocalDate endDate = getEndDate(reqDTO);
    ifAnyErrorsThrowException();

    returnsMap.forEach((key, value) -> {
      final HistoricalNavPrices navPrices = navData.get(key);
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

  private LocalDate getStartDate(final DailyPerformanceCommand reqDTO) {
    final LocalDate startDate = Optional.ofNullable(reqDTO.getStartDate()).orElse(psd);
    LocalDate newStartDate = LocalDate.of(startDate.getYear(), startDate.getMonth(), 1);
    if (newStartDate.isBefore(psd)) {
      newStartDate = newStartDate.plusMonths(1);
    }
    validateCpsd(newStartDate);
    reqDTO.setStartDate(newStartDate);
    return newStartDate;
  }

  private LocalDate getEndDate(final DailyPerformanceCommand reqDTO) {
    LocalDate newEndDate = toLastDayOfMonth(Optional.ofNullable(reqDTO.getEndDate()).orElse(ped));
    if (newEndDate.isAfter(ped)) {
      newEndDate = toLastDayOfMonth(newEndDate.minusMonths(1));
    }
    validateCped(newEndDate);
    reqDTO.setEndDate(newEndDate);
    return newEndDate;
  }

  private LocalDate getEarliestAvailableDate(final LocalDate startDate, final HistoricalNavPrices navPrices,
      final TreeMap<LocalDate, BigDecimal> value) {
    if (Objects.isNull(startDate) || Objects.isNull(value)) {
      throw new SystemException(
          "Can't obtain Earliest Available Date. Missed one of the parameters. Start Date: %s. Value: %s".formatted(
              startDate, value),
          HttpCode.INTERNAL_SERVER_ERROR);
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
      throw new SystemException(
          "Can't obtain Latest Available Date. Missed one of the parameters. End Date: %s. Value: %s".formatted(endDate,
              value),
          HttpCode.INTERNAL_SERVER_ERROR);
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
    partitionedHoldings.get(false).forEach((key, value) -> notification.addError(ERR_FDS_MC_002.error(key)));
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
    notification.ifAnyNonAllowedErrorThrowException(List.of(ERR_RRC_MR_002, ERR_RRC_MMR_001, ERR_FDS_MC_002));
    return this;
  }

  public ReturnsAggregate<T> validateReturns() {
    Map<LocalDate, List<Map.Entry<PortfolioHolding, TreeMap<LocalDate, BigDecimal>>>> startDateEntriesMap = returnsMap
        .entrySet()
        .stream()
        .collect(groupingBy(e -> e.getValue().firstKey(), toList()));
    if (Objects.nonNull(psd) && Objects.nonNull(ped)) {
      while (psd.isAfter(ped)) {
        var entries = startDateEntriesMap.get(psd);
        for (Map.Entry<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> entry : entries) {
          notification.addError(ERR_RRC_MR_002.error(entry.getKey()));
          returnsMap.remove(entry.getKey());
        }
        findPedAndPsd();
      }
    }
    ifAnyErrorsThrowException();
    return this;
  }

  public ReturnsAggregate<T> validateMonthlyDataMissing(final Map<PortfolioHolding, HistoricalNavPrices> navData,
      final DailyPerformanceCommand reqDTO) {
    final long holdingsWithPacOrWithdrawals = reqDTO.getDailyHoldings().stream()
        .filter(dh -> (!dh.getWithdrawal().equals(BigDecimal.ZERO) && !dh.getPac().equals(BigDecimal.ZERO)))
        .count();
    if (holdingsWithPacOrWithdrawals != 0) {
      navData.forEach((key, value) -> value.getMissedMonthData().stream()
          .filter(m -> m.isAfter(reqDTO.getStartDate()) && m.isBefore(reqDTO.getEndDate()))
          .forEach(m -> notification.addError(ERR_NAV_PRICES_002.error(key, m.format(PATTERN_1)))));
    }
    return this;
  }

  public List<DataErrorException> getErrors() {
    return notification.getErrors();
  }

  public List<Warning> getErrorsAsWarnings() {
    return notification.getErrors().stream().map(error -> new Warning(error.getId(), error.getMessage(), error.getCode()
        .name())).toList();
  }
}
