package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.domain.model.Yield;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.sm.model.DataProvider;
import com.fintex.sm.model.domain.SecurityIdentifier;
import com.fintex.sm.model.domain.datapoint.FloatDatapoint;
import com.fintex.sm.model.domain.datapoint.Income;
import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class YieldMapperTest {

  private final YieldMapper sut = new YieldMapper();

  @Test
  void shouldMapDividendYieldAndProvider_whenResponseHasValues() {
    var dividendYield = new FloatDatapoint();
    dividendYield.setValue(BigDecimal.valueOf(0.035));
    dividendYield.setDataProvider(DataProvider.MORNINGSTAR);

    var smsResponse = new Income();
    smsResponse.setDividendYield(dividendYield);

    Yield result = sut.map(smsResponse, createHolding("SEC-001"));

    assertThat(result.getHoldingId()).isEqualTo("SEC-001");
    assertThat(result.getDividendYield()).isEqualByComparingTo("0.035");
    assertThat(result.getProvider()).isEqualTo("MORNINGSTAR");
  }

  @Test
  void shouldReturnEmptyYield_whenResponseIsNull() {
    Yield result = sut.map(null, createHolding("SEC-002"));

    assertThat(result.getHoldingId()).isEqualTo("SEC-002");
    assertThat(result.getDividendYield()).isNull();
    assertThat(result.getProvider()).isNull();
  }

  @Test
  void shouldMapOnlyHoldingId_whenDividendYieldIsNull() {
    var smsResponse = new Income();
    smsResponse.setDividendYield(null);

    Yield result = sut.map(smsResponse, createHolding("SEC-003"));

    assertThat(result.getHoldingId()).isEqualTo("SEC-003");
    assertThat(result.getDividendYield()).isNull();
    assertThat(result.getProvider()).isNull();
  }

  @Test
  void shouldMapDifferentProviders() {
    var dividendYield = new FloatDatapoint();
    dividendYield.setValue(BigDecimal.valueOf(0.025));
    dividendYield.setDataProvider(DataProvider.MORNINGSTAR);

    var smsResponse = new Income();
    smsResponse.setDividendYield(dividendYield);

    Yield result = sut.map(smsResponse, createHolding("SEC-004"));

    assertThat(result.getDividendYield()).isEqualByComparingTo("0.025");
    assertThat(result.getProvider()).isEqualTo("MORNINGSTAR");
  }

  private Holding createHolding(String securityId) {
    var identifier = new SecurityIdentifier();
    identifier.setId(securityId);
    return new Holding()
            .setHoldingType(FinancialInstrumentType.ETF_CANADA)
            .setSecurityIdentifier(identifier);
  }
}
