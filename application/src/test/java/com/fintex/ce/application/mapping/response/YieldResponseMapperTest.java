package com.fintex.ce.application.mapping.response;

import com.fintex.ce.model.domain.calculation.yield.Yield;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.income.YieldResult;
import com.fintex.wm.commons.domain.enumeration.Country;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.error.Notification;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class YieldResponseMapperTest {

  private final YieldResponseMapper mapper = new YieldResponseMapper();

  @Test
  void shouldReturnNullYield_whenDomainIsNull() {
    YieldResult result = mapper.toResponse((Yield) null);

    assertNull(result.getYield());
    assertEquals(0, result.getWarnings().size());
  }

  @Test
  void shouldMapDividendYield_whenDomainIsPresent() {
    Yield domain = new Yield();
    domain.setDividendYield(new BigDecimal("0.12345678901"));

    YieldResult result = mapper.toResponse(domain);

    assertEquals(0, result.getYield().compareTo(new BigDecimal("0.12345678901")));
    assertEquals(0, result.getWarnings().size());
  }

  @Test
  void shouldCalculateWeightedAverageYield_whenMappingPortfolioDomainMap() {
    PortfolioHolding stock = new PortfolioHolding(new BigDecimal("2"), FinancialInstrumentType.STOCK, Country.USA,
        null);
    PortfolioHolding gic = new PortfolioHolding(new BigDecimal("3"), FinancialInstrumentType.GIC, null);
    PortfolioHolding skipped = new PortfolioHolding(new BigDecimal("5"), FinancialInstrumentType.STOCK, Country.CANADA,
        null);

    Map<PortfolioHolding, Yield> domainMap = Map.of(
        stock, yieldOf(new BigDecimal("0.1")),
        gic, yieldOf(new BigDecimal("5")),
        skipped, yieldOf(null));
    List<Notification> warnings = List.of(Notification.builder().uuid("w1").message("warning").build());

    YieldResult result = mapper.toResponse(domainMap, warnings);

    // weighted average: (0.1*2 + 0.05*3) / (2+3) = 0.07
    assertEquals(0, result.getYield().compareTo(new BigDecimal("0.070000000000000")));
    assertEquals(warnings, result.getWarnings());
  }

  @Test
  void shouldReturnZeroYield_whenNoValidEntriesProvided() {
    PortfolioHolding invalid = new PortfolioHolding(null, FinancialInstrumentType.STOCK, Country.USA, null);
    Map<PortfolioHolding, Yield> domainMap = Map.of(invalid, yieldOf(new BigDecimal("0.1")));

    YieldResult result = mapper.toResponse(domainMap, List.of());

    assertEquals(0, result.getYield().compareTo(BigDecimal.ZERO));
    assertEquals(0, result.getWarnings().size());
  }

  private static Yield yieldOf(final BigDecimal dividendYield) {
    final Yield yield = new Yield();
    yield.setDividendYield(dividendYield);
    return yield;
  }
}
