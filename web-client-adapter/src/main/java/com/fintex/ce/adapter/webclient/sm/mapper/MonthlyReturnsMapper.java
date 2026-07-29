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
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static com.fintex.ce.util.DateTimeUtils.toLastDayOfMonth;

@Component
public class MonthlyReturnsMapper
    implements
      SecurityMasterResponseMapper<HoldingMonthlyReturns, MonthlyReturns> {

  private static final Map<Country, Currency> COUNTRY_CURRENCY_MAP = Map.of(
      Country.CANADA, Currency.CAD,
      Country.USA, Currency.USD);

  @Override
  public HoldingMonthlyReturns map(MonthlyReturns smsResponse, PortfolioHolding holding) {
    TreeMap<LocalDate, BigDecimal> returnsMap = Optional.ofNullable(smsResponse)
        .map(MonthlyReturns::getReturns)
        .orElse(List.of())
        .stream()
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
