package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.model.domain.calculation.yield.Yield;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.datapoint.FloatDatapoint;
import com.fintex.wm.commons.domain.enumeration.Country;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.financial.Income;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static com.fintex.ce.test.PortfolioHoldingBuildHelper.holding;
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

    Yield result = mapper.map(smsResponse, holding(new SecurityIdentifier("SEC-001", null), FinancialInstrumentType.ETF,
        Country.CANADA, (BigDecimal) null));

    assertThat(result.getDividendYield()).isEqualByComparingTo("0.035");
    assertThat(result.getProviders()).containsExactly(DataProvider.MORNINGSTAR);
  }

  @Test
  void shouldReturnEmptyYield_whenResponseIsNull() {
    Yield result = mapper.map(null, holding(new SecurityIdentifier("SEC-002", null), FinancialInstrumentType.ETF,
        Country.CANADA, (BigDecimal) null));

    assertThat(result.getDividendYield()).isNull();
    assertThat(result.getProviders()).isEmpty();
  }

  @Test
  void shouldReturnEmptyYield_whenDividendYieldIsNull() {
    var smsResponse = new Income();
    smsResponse.setDividendYield(null);

    Yield result = mapper.map(smsResponse, holding(new SecurityIdentifier("SEC-003", null), FinancialInstrumentType.ETF,
        Country.CANADA, (BigDecimal) null));

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

    Yield result = mapper.map(smsResponse, holding(new SecurityIdentifier("SEC-004", null), FinancialInstrumentType.ETF,
        Country.CANADA, (BigDecimal) null));

    assertThat(result.getDividendYield()).isEqualByComparingTo("0.025");
    assertThat(result.getProviders()).containsExactly(DataProvider.MORNINGSTAR);
  }

}
