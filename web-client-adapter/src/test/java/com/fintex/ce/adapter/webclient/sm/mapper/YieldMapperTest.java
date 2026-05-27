package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.model.domain.calculation.yield.Yield;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.datapoint.FloatDatapoint;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.financial.Income;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class YieldMapperTest {

  private final YieldMapper mapper = new YieldMapper();

  @Test
  void shouldMapDividendYieldAndProvider_whenResponseHasValues() {
    var dividendYield = new FloatDatapoint();
    dividendYield.setValue(BigDecimal.valueOf(0.035));
    dividendYield.setDataProviders(List.of(DataProvider.MORNINGSTAR));

    var smsResponse = new Income();
    smsResponse.setDividendYield(dividendYield);

    Yield result = mapper.map(smsResponse, createHolding("SEC-001"));

    assertThat(result.getDividendYield()).isEqualByComparingTo("0.035");
    assertThat(result.getProviders()).containsExactly(DataProvider.MORNINGSTAR);
  }

  @Test
  void shouldReturnEmptyYield_whenResponseIsNull() {
    Yield result = mapper.map(null, createHolding("SEC-002"));

    assertThat(result.getDividendYield()).isNull();
    assertThat(result.getProviders()).isEmpty();
  }

  @Test
  void shouldReturnEmptyYield_whenDividendYieldIsNull() {
    var smsResponse = new Income();
    smsResponse.setDividendYield(null);

    Yield result = mapper.map(smsResponse, createHolding("SEC-003"));

    assertThat(result.getDividendYield()).isNull();
    assertThat(result.getProviders()).isEmpty();
  }

  @Test
  void shouldMapDifferentProviders() {
    var dividendYield = new FloatDatapoint();
    dividendYield.setValue(BigDecimal.valueOf(0.025));
    dividendYield.setDataProviders(List.of(DataProvider.MORNINGSTAR));

    var smsResponse = new Income();
    smsResponse.setDividendYield(dividendYield);

    Yield result = mapper.map(smsResponse, createHolding("SEC-004"));

    assertThat(result.getDividendYield()).isEqualByComparingTo("0.025");
    assertThat(result.getProviders()).containsExactly(DataProvider.MORNINGSTAR);
  }

  private PortfolioHolding createHolding(String securityId) {
    var identifier = new SecurityIdentifier();
    identifier.setId(securityId);
    return new PortfolioHolding(null, FinancialInstrumentType.ETF_CANADA, identifier);
  }
}
