package com.fintex.ce.e2e;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.dto.command.ReturnCommand;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.id.EquitySecurityIdentifier;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;
import com.fintex.wm.commons.domain.performance.MonthlyReturns;
import com.fintex.wm.commons.domain.value.DateBigDecimalValue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

final class ReturnCommandFixtures {

  static final SecurityIdentifier XBAL = new SecurityIdentifier("XBAL", FiIdentifierType.TICKER);
  static final SecurityIdentifier VCNS = new SecurityIdentifier("VCNS", FiIdentifierType.TICKER);
  static final SecurityIdentifier VTI = new SecurityIdentifier("VTI", FiIdentifierType.TICKER);
  static final SecurityIdentifier SPY = new SecurityIdentifier("SPY", FiIdentifierType.TICKER);
  static final SecurityIdentifier F0CAN999 = new SecurityIdentifier("F0CAN999", FiIdentifierType.MORNINGSTAR_ID);
  static final SecurityIdentifier CCM4752 = new SecurityIdentifier("CCM4752", FiIdentifierType.FUNDSERV);
  static final EquitySecurityIdentifier RY_TO = EquitySecurityIdentifier.builder()
      .id("RY.TO")
      .idType(FiIdentifierType.TICKER_MIC)
      .exchangeId("TSX")
      .build();

  static ReturnCommand commandFor(
      CalculationMetric metric,
      Currency currency,
      List<PortfolioHolding> holdings) {
    ReturnCommand command = new ReturnCommand();
    command.setMetric(metric);
    command.setCurrency(currency);
    command.setHoldings(holdings);
    return command;
  }

  static MonthlyReturns monthlyReturns(
      List<DateBigDecimalValue> returns,
      DataProvider provider,
      String asOf) {
    MonthlyReturns monthlyReturns = new MonthlyReturns();
    monthlyReturns.setReturns(returns);
    monthlyReturns.setDataProviders(provider == null ? null : List.of(provider));
    monthlyReturns.setAsOfDate(LocalDateTime.parse(asOf));
    return monthlyReturns;
  }

  static List<DateBigDecimalValue> returns(String... dateValuePairs) {
    if (dateValuePairs.length % 2 != 0) {
      throw new IllegalArgumentException("expected even number of strings: date,value pairs");
    }

    var list = new ArrayList<DateBigDecimalValue>(dateValuePairs.length / 2);
    for (int i = 0; i < dateValuePairs.length; i += 2) {
      list.add(new DateBigDecimalValue(
          dateValuePairs[i],
          new BigDecimal(dateValuePairs[i + 1])));
    }
    return list;
  }

  private ReturnCommandFixtures() {
  }
}