package com.fintex.ce.adapter.graphqlclient.util;

import com.google.common.base.Strings;
import com.fintex.smclient.graphql.AssetAllocation;
import com.fintex.smclient.graphql.CountryAllocation;
import com.fintex.smclient.graphql.CountryValue;
import com.fintex.smclient.graphql.CreditQualityRatingTypeValue;
import com.fintex.smclient.graphql.CreditQualityRatings;
import com.fintex.smclient.graphql.DateValue;
import com.fintex.smclient.graphql.DateValuesDatapoint;
import com.fintex.smclient.graphql.EquityMarketCapitalization;
import com.fintex.smclient.graphql.EquitySectorAllocation;
import com.fintex.smclient.graphql.ExternalIdentifierType;
import com.fintex.smclient.graphql.ExternalIdentifierTypeValue;
import com.fintex.smclient.graphql.FixedIncomeSecuritiesAllocation;
import com.fintex.smclient.graphql.FloatDatapoint;
import com.fintex.smclient.graphql.HistoricalDistributions;
import com.fintex.smclient.graphql.Holding;
import com.fintex.smclient.graphql.HoldingValue;
import com.fintex.smclient.graphql.Holdings;
import com.fintex.smclient.graphql.MonthlyReturns;
import com.fintex.smclient.graphql.NameValue;
import com.fintex.ce.domain.enumeration.Currency;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.enumeration.ExceptionCode;
import com.fintex.ce.domain.enumeration.HoldingIdentifierType;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.model.*;
import com.fintex.ce.domain.model.CommonHoldingsDTO;
import com.fintex.ce.domain.model.ExternalIdentifiersDTO;
import com.fintex.ce.domain.exception.DataErrorException;
import com.fintex.ce.util.JacksonUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.fintex.smclient.graphql.LanguageCode.EN;
import static com.fintex.ce.util.CollectorUtils.toMap;
import static com.fintex.ce.util.CollectorUtils.toTreeMap;
import static com.fintex.ce.util.DateTimeUtils.PATTERN_1;
import static com.fintex.ce.util.DateTimeUtils.toLastDayOfMonth;
import static java.util.Objects.nonNull;

public class GraphQlMapperUtils {

  private static final long DAYS_TO_VERIFY = 5;

  private GraphQlMapperUtils() {
  }

  public static com.fintex.ce.domain.model.MonthlyReturns monthlyReturns(MonthlyReturns monthlyReturns,
      String currency,
      com.fintex.ce.domain.model.holding.Holding holding) {
    com.fintex.ce.domain.model.MonthlyReturns result = new com.fintex.ce.domain.model.MonthlyReturns()
        .setCurrency(currency)
        .setHoldingType(toDomainHoldingType(holding.getType()));

    validateCurrency(currency, holding, result);

    if (Objects.isNull(monthlyReturns)
        || Objects.isNull(monthlyReturns.getReturns())
        || monthlyReturns.getReturns().isEmpty()) {
      result.addError(toValidationError(ExceptionCode.ERR_RRC_MMR_001.error(holding)));
      return result;
    }

    var returns = monthlyReturns.getReturns()
        .stream()
        .collect(toTreeMap(GraphQlMapperUtils::parseDate, DateValue::getValue));

    validateEveryMonthInReturns(holding, returns, result);

    return result.setReturns(returns);
  }

  public static HistoricalNavPrices historicalNavPrices(DateValuesDatapoint navPrices,
      com.fintex.ce.domain.model.holding.Holding holding) {
    HistoricalNavPrices result = new HistoricalNavPrices()
        .setHoldingType(toDomainHoldingType(holding.getType()));

    if (Objects.isNull(navPrices)
        || Objects.isNull(navPrices.getValues())
        || navPrices.getValues().isEmpty()) {
      result.addError(toValidationError(ExceptionCode.ERR_NAV_PRICES_001.error(holding)));
      return result;
    }

    var returns = navPrices.getValues()
        .stream()
        .collect(toTreeMap(GraphQlMapperUtils::parseDateAsIs, DateValue::getValue));

    validateNavPrice(holding, returns, result);

    return result.setReturns(returns);
  }

  public static com.fintex.ce.domain.model.HistoricalDistributions historicalDistributions(
      HistoricalDistributions fdsResponse,
      com.fintex.ce.domain.model.holding.Holding holding) {
    com.fintex.ce.domain.model.HistoricalDistributions result = new com.fintex.ce.domain.model.HistoricalDistributions()
        .setHoldingType(toDomainHoldingType(holding.getType()));

    TreeMap<LocalDate, com.fintex.ce.domain.model.HistoricalDistributions.DistributionsDto> distributionsDtoTreeMap = null;
    TreeMap<LocalDate, com.fintex.ce.domain.model.HistoricalDistributions.CapitalGainsDto> capitalGainsDtoTreeMap = null;

    if (nonNull(fdsResponse) && nonNull(fdsResponse.getDistributions())) {
      distributionsDtoTreeMap = fdsResponse.getDistributions()
          .stream()
          .collect(toTreeMap(e -> LocalDate.parse(e.getDate()),
              e -> new com.fintex.ce.domain.model.HistoricalDistributions.DistributionsDto(
                  e.getDomesticDividend(), e.getForeignDividend(), e.getInterestIncome())));
    }

    if (nonNull(fdsResponse) && nonNull(fdsResponse.getCapitalGains())) {
      capitalGainsDtoTreeMap = fdsResponse.getCapitalGains()
          .stream()
          .collect(toTreeMap(e -> LocalDate.parse(e.getDate()),
              e -> new com.fintex.ce.domain.model.HistoricalDistributions.CapitalGainsDto(
                  e.getCapitalGain(), e.getReturnOfCapital())));
    }

    result.setDistributions(distributionsDtoTreeMap);
    result.setCapitalGains(capitalGainsDtoTreeMap);
    return result;
  }

  private static void validateCurrency(String currency, com.fintex.ce.domain.model.holding.Holding holding,
      com.fintex.ce.domain.model.MonthlyReturns result) {
    if (Objects.isNull(currency)) {
      result.addError(toValidationError(ExceptionCode.ERR_FDS_MC_002.error(holding)));
    } else if (Currency.get(currency) == null) {
      result.addError(toValidationError(ExceptionCode.ERR_FDS_MC_003.error(holding, currency)));
    }
  }

  private static void validateEveryMonthInReturns(com.fintex.ce.domain.model.holding.Holding holding,
      TreeMap<LocalDate, BigDecimal> collected,
      com.fintex.ce.domain.model.MonthlyReturns result) {
    final LocalDate endDate = toLastDayOfMonth(collected.lastKey());
    final LocalDate startDate = toLastDayOfMonth(collected.firstKey());
    for (LocalDate date = startDate; !date.isAfter(endDate); date = toLastDayOfMonth(date.plusMonths(1))) {
      if (!collected.containsKey(date)) {
        result.addError(toValidationError(ExceptionCode.ERR_RRC_MMR_002.error(holding, date)));
      }
    }
  }

  private static void validateNavPrice(com.fintex.ce.domain.model.holding.Holding holding,
      TreeMap<LocalDate, BigDecimal> collected,
      HistoricalNavPrices result) {
    final LocalDate startDate = collected.firstKey();
    final LocalDate endDate = collected.lastKey();
    validateMonthlyData(holding, collected, result, startDate, endDate);
    populateNavPricesWithMissedValues(collected, startDate, endDate, result);
  }

  private static void validateMonthlyData(com.fintex.ce.domain.model.holding.Holding holding,
      TreeMap<LocalDate, BigDecimal> collected,
      HistoricalNavPrices result,
      LocalDate startDate,
      LocalDate endDate) {
    for (LocalDate date = startDate; !(date.getYear() == endDate.getYear() && date.getMonth() == endDate
        .getMonth()); date = date.plusMonths(1)) {
      final LocalDate lastDayOfMonth = toLastDayOfMonth(date);
      boolean monthContainsData = false;
      for (LocalDate day = lastDayOfMonth; day.isAfter(lastDayOfMonth.minusDays(DAYS_TO_VERIFY)); day = day.minusDays(
          1)) {
        if (collected.containsKey(day)) {
          monthContainsData = true;
          break;
        }
      }
      if (!monthContainsData) {
        result.addError(toValidationError(ExceptionCode.ERR_NAV_PRICES_002.error(holding, lastDayOfMonth.format(
            PATTERN_1))));
        result.getMissedMonthData().add(lastDayOfMonth);
      }
    }
  }

  private static void populateNavPricesWithMissedValues(final Map<LocalDate, BigDecimal> returns,
      final LocalDate startDate,
      final LocalDate endDate,
      final HistoricalNavPrices result) {
    final List<LocalDate> datesWithoutData = new ArrayList<>();
    for (LocalDate date = startDate; date.isBefore(endDate); date = date.plusDays(1)) {
      final LocalDate nextDay = date.plusDays(1);
      if (Objects.isNull(returns.get(nextDay))) {
        datesWithoutData.add(nextDay);
      }
      returns.putIfAbsent(nextDay, returns.get(date));
    }
    result.setMissedDates(datesWithoutData);
  }

  public static com.fintex.ce.domain.model.AssetAllocation assetAllocation(final AssetAllocation assetAllocation,
      final HoldingType holdingType) {
    Map<String, BigDecimal> assetAllocationMap = Optional.ofNullable(assetAllocation)
        .map(AssetAllocation::getAllocation)
        .orElse(List.of())
        .stream()
        .collect(Collectors.toMap(NameValue::getName, NameValue::getValue));
    com.fintex.ce.domain.model.AssetAllocation allocation = new com.fintex.ce.domain.model.AssetAllocation(
        toDomainHoldingType(holdingType), assetAllocationMap);
    Optional.ofNullable(assetAllocation).ifPresent(result -> allocation.setProvider(DataProvider.of(result
        .getDataProvider().name()).name()));
    return allocation;
  }

  public static FixedIncomeBondSecurities fixedIncomeBondSectorMapper(
      final FixedIncomeSecuritiesAllocation fixedIncomeSectorAllocation,
      final HoldingType holdingType) {
    final Map<String, BigDecimal> fixedIncomeSectorAllocationMap = Optional.ofNullable(fixedIncomeSectorAllocation)
        .map(FixedIncomeSecuritiesAllocation::getAllocation)
        .orElse(List.of())
        .stream()
        .collect(Collectors.toMap(NameValue::getName, NameValue::getValue));
    final FixedIncomeBondSecurities result = new FixedIncomeBondSecurities(
        toDomainHoldingType(holdingType), fixedIncomeSectorAllocationMap);
    Optional.ofNullable(fixedIncomeSectorAllocation).ifPresent(r -> result.setProvider(DataProvider.of(r
        .getDataProvider().name()).name()));
    return result;
  }

  public static LocalDate parseDate(final DateValue m) {
    final String dateStr = m.getDate();
    final String[] split = dateStr.split("-");
    final LocalDate date = LocalDate.of(Integer.parseInt(split[0]), Integer.parseInt(split[1]), 1);
    return toLastDayOfMonth(date);
  }

  public static LocalDate parseDateAsIs(final DateValue m) {
    final String dateStr = m.getDate();
    final String[] split = dateStr.split("-");
    return LocalDate.of(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
  }

  public static Map<String, BigDecimal> equityCountryAllocationMapper(final CountryAllocation countryAllocation) {
    if (countryAllocation == null || countryAllocation.getAllocation() == null) {
      return new HashMap<>();
    }
    return countryAllocation.getAllocation().stream()
        .filter(e -> e != null && e.getName() != null && e.getName().stream().anyMatch(lang -> EN.equals(lang
            .getLanguageCode())))
        .collect(
            toMap(
                e -> e.getName().stream().filter(lang -> EN.equals(lang.getLanguageCode())).findFirst().orElseThrow()
                    .getValue(),
                CountryValue::getValue));
  }

  public static Map<String, BigDecimal> creditQualityMapper(final CreditQualityRatings creditQualityRatings) {
    if (creditQualityRatings == null || creditQualityRatings.getRatings() == null) {
      return new HashMap<>();
    }
    return creditQualityRatings.getRatings().stream()
        .filter(e -> e != null && !StringUtils.isBlank(e.getRating()))
        .collect(
            toMap(
                CreditQualityRatingTypeValue::getRating,
                e -> e.getValue() == null ? BigDecimal.ZERO : e.getValue()));
  }

  public static EquitySector equitySectorMapper(final EquitySectorAllocation sectorAllocation) {
    if (sectorAllocation == null || CollectionUtils.isEmpty(sectorAllocation.getAllocation())) {
      EquitySector equitySector = new EquitySector();
      equitySector.setAllocations(new HashMap<>());
      return equitySector;
    }
    final Map<String, BigDecimal> sectors = sectorAllocation.getAllocation().stream()
        .filter(e -> e != null && e.getNames() != null && e.getNames().stream().anyMatch(lang -> EN.equals(lang
            .getLanguageCode())))
        .collect(
            toMap(
                e -> e.getNames().stream().filter(lang -> EN.equals(lang.getLanguageCode())).findFirst().orElseThrow()
                    .getValue(),
                e -> e.getValue() == null ? BigDecimal.ZERO : e.getValue()));
    final EquitySector equitySector = new EquitySector(sectors);
    equitySector.setProvider(Objects.requireNonNull(sectorAllocation.getDataProvider()).name());
    return equitySector;
  }

  public static CommonHoldings topCommonHoldingsMapper(final Holdings topHoldings) {
    if (topHoldings == null || CollectionUtils.isEmpty(topHoldings.getAllocation())) {
      return new CommonHoldings();
    }
    final List<CommonHoldingsDTO> commonHoldings = mapCommonHoldings(topHoldings.getAllocation());
    final CommonHoldings result = new CommonHoldings(JacksonUtil.serialize(commonHoldings));
    result.setProvider(Objects.requireNonNull(topHoldings.getDataProvider()).name());
    return result;
  }

  public static List<CommonHoldingsDTO> mapCommonHoldings(final List<HoldingValue> holdings) {
    final List<CommonHoldingsDTO> commonHoldings = new ArrayList<>();

    for (final HoldingValue h : holdings) {
      final Holding holding = h.getHolding();
      final BigDecimal value = h.getValue();
      if (!isValid(holding)) {
        continue;
      }
      final CommonHoldingsDTO dto = initializeCommonHolding(holding, value);
      if (holding.getUnderlyingHoldings() != null) {
        final List<CommonHoldingsDTO> underlyingHoldings = mapCommonHoldings(holding.getUnderlyingHoldings());
        dto.setUnderlyingHoldings(underlyingHoldings);
      } else {
        dto.setUnderlyingHoldings(null);
      }
      commonHoldings.add(dto);
    }
    return commonHoldings;
  }

  public static CommonHoldingsDTO initializeCommonHolding(final Holding holding, final BigDecimal value) {
    final CommonHoldingsDTO dto = new CommonHoldingsDTO();

    final String ticker = getTickerFromExternalIdentifiers(holding);
    final String exchangeCode = getExchangeCodeFromExternalIdentifiers(holding);

    final String name = holding.getName().stream().filter(lang -> EN.equals(lang.getLanguageCode())).findFirst()
        .orElseThrow().getValue();
    return dto
        .setName(name)
        .setCompanyName(holding.getCompanyName())
        .setType(holding.getType())
        .setValue(value)
        .setTicker(ticker)
        .setExchangeCode(exchangeCode);
  }

  private static String getExchangeCodeFromExternalIdentifiers(final Holding holding) {
    if (holding.getExternalIdentifiers() != null) {
      return Optional.of(holding.getExternalIdentifiers().getCodes().stream()
          .filter(e -> ExternalIdentifierType.EXCHANGE_ID.equals(e.getType())).map(
              ExternalIdentifierTypeValue::getValue).findFirst()).get().orElse(null);
    }
    return null;
  }

  private static String getTickerFromExternalIdentifiers(final Holding holding) {
    if (holding.getExternalIdentifiers() != null) {
      return Optional.of(holding.getExternalIdentifiers().getCodes().stream()
          .filter(e -> HoldingIdentifierType.TICKER.name().equalsIgnoreCase(e.getType().name())).map(
              ExternalIdentifierTypeValue::getValue).findFirst()).get().orElse(null);
    }
    return null;
  }

  public static Function<ExternalIdentifierTypeValue, ExternalIdentifiersDTO> mapExternalIdentifiers() {
    return e -> new ExternalIdentifiersDTO(
        HoldingIdentifierType.of(e.getType().name()),
        e.getValue());
  }

  public static boolean isValid(final Holding holding) {
    return !Strings.isNullOrEmpty(holding.getCompanyName())
        || holding.getName().stream().noneMatch(e -> e.getValue() == null);
  }

  public static com.fintex.ce.domain.model.EquityMarketCapitalization equityMarketCapitalizationMapper(
      final EquityMarketCapitalization res) {
    if (res == null || CollectionUtils.isEmpty(res.getValues())) {
      return new com.fintex.ce.domain.model.EquityMarketCapitalization();
    }
    final Map<String, BigDecimal> result = res.getValues().stream()
        .filter(e -> e.getEquityMarketCapitalization() != null)
        .collect(
            toMap(
                e -> e.getEquityMarketCapitalization().name(),
                e -> e.getValue() == null ? BigDecimal.ZERO : e.getValue()));
    final com.fintex.ce.domain.model.EquityMarketCapitalization equityMarketCap = new com.fintex.ce.domain.model.EquityMarketCapitalization(
        result);
    equityMarketCap.setProvider(Objects.requireNonNull(res.getDataProvider()).name());
    return equityMarketCap;
  }

  public static Map<String, BigDecimal> countryExposureMapper(final CountryAllocation countryAllocation) {
    if (countryAllocation == null || countryAllocation.getAllocation() == null) {
      return new HashMap<>();
    }
    return countryAllocation.getAllocation().stream()
        .filter(e -> !StringUtils.isBlank(e.getIsoCode()))
        .collect(
            toMap(
                CountryValue::getIsoCode,
                e -> e.getValue() == null ? BigDecimal.ZERO : e.getValue()));
  }

  public static <T> Yield mapYield(final T entity,
      final Function<T, FloatDatapoint> function) {
    final var result = new Yield();
    Optional.ofNullable(entity)
        .map(function)
        .map(FloatDatapoint::getValue)
        .ifPresent(result::setDividendYield);
    return result;
  }

  // Utility methods
  public static com.fintex.ce.domain.enumeration.HoldingType toDomainHoldingType(HoldingType apiType) {
    if (apiType == null) {
      return null;
    }
    return com.fintex.ce.domain.enumeration.HoldingType.valueOf(apiType.name());
  }

  public static ValidationError toValidationError(DataErrorException e) {
    return new ValidationError(e.getId(), e.getCode().name(), e.getMessage());
  }

}
