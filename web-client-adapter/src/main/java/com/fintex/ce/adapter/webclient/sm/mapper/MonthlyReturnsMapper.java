package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.model.domain.calculation.returns.HoldingMonthlyReturns;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.Country;
import com.fintex.wm.commons.domain.performance.MonthlyReturns;
import com.fintex.wm.commons.domain.value.DateBigDecimalValue;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static com.fintex.ce.util.DateTimeUtils.toLastDayOfMonth;
import static java.util.stream.Collectors.joining;

@Component
public class MonthlyReturnsMapper
    implements
      SecurityMasterResponseMapper<HoldingMonthlyReturns, MonthlyReturns> {

  private static final Map<Country, Currency> COUNTRY_CURRENCY_MAP = Map.of(
      Country.CANADA, Currency.CAD,
      Country.USA, Currency.USD);

  @Override
  public HoldingMonthlyReturns map(MonthlyReturns smsResponse, PortfolioHolding holding) {
    List<DateBigDecimalValue> monthlyReturns = Optional.ofNullable(smsResponse)
        .map(MonthlyReturns::getReturns)
        .orElse(List.of());
    validateMonthlyReturnValues(monthlyReturns, holding);

    TreeMap<LocalDate, BigDecimal> returnsMap = monthlyReturns
        .stream()
        .filter(Objects::nonNull)
        .filter(entry -> entry.getDate() != null && entry.getValue() != null)
        .collect(Collectors.toMap(
            entry -> toLastDayOfMonth(LocalDate.parse(entry.getDate())),
            DateBigDecimalValue::getValue,
            (existing, replacement) -> existing,
            TreeMap::new));

    final List<DataProvider> providers = Optional.ofNullable(smsResponse)
        .map(MonthlyReturns::getDataProviders)
        .orElseGet(List::of);

    return HoldingMonthlyReturns.builder()
        .returns(returnsMap)
        .currency(resolveCurrency(holding))
        .holdingType(holding.getHoldingType())
        .providers(providers)
        .build();
  }

  private void validateMonthlyReturnValues(List<DateBigDecimalValue> monthlyReturns, PortfolioHolding holding) {
    String missingDates = monthlyReturns.stream()
        .filter(Objects::nonNull)
        .filter(entry -> entry.getDate() != null && entry.getValue() == null)
        .map(entry -> toLastDayOfMonth(LocalDate.parse(entry.getDate())))
        .distinct()
        .sorted()
        .map(LocalDate::toString)
        .collect(joining(", "));
    if (!missingDates.isEmpty()) {
      throw ErrorCode.MISSING_MONTHLY_RETURN_FOR_DATE.toExceptionForHolding(holding, missingDates);
    }
  }

  private String resolveCurrency(PortfolioHolding holding) {
    Country country = holding.getCountry();
    if (country == null) {
      return null;
    }
    Currency currency = COUNTRY_CURRENCY_MAP.get(country);
    if (currency == null) {
      throw ErrorCode.COUNTRY_NOT_SUPPORTED.toExceptionForHolding(holding, country.name());
    }
    return currency.name();
  }
}
