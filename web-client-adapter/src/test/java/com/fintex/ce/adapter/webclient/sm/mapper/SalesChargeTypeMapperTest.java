package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.domain.model.SalesCharge;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.sm.model.DataProvider;
import com.fintex.sm.model.domain.SecurityIdentifier;
import com.fintex.sm.model.domain.datapoint.SalesChargeData;
import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;
import com.fintex.sm.model.domain.enumeration.SalesChargeType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SalesChargeTypeMapperTest {

  private final SalesChargeMapper sut = new SalesChargeMapper();

  @Test
  void shouldMapValueAndProvider_whenResponseHasValues() {
    var smsResponse = mock(SalesChargeData.class);
    var salesCharge = mock(com.fintex.sm.model.domain.datapoint.SalesCharge.class);
    when(smsResponse.getSalesCharge()).thenReturn(salesCharge);
    when(salesCharge.getValue()).thenReturn(SalesChargeType.DEFERRED_SALES_CHARGE);
    when(salesCharge.getDataProvider()).thenReturn(DataProvider.MORNINGSTAR);

    SalesCharge result = sut.map(smsResponse, createHolding("SEC-001"));

    assertThat(result.getHoldingId()).isEqualTo("SEC-001");
    assertThat(result.getType()).isEqualTo(SalesChargeType.DEFERRED_SALES_CHARGE);
    assertThat(result.getProvider()).isEqualTo("MORNINGSTAR");
  }

  @Test
  void shouldReturnEmptySalesCharge_whenResponseIsNull() {
    SalesCharge result = sut.map(null, createHolding("SEC-002"));

    assertThat(result.getHoldingId()).isEqualTo("SEC-002");
    assertThat(result.getType()).isNull();
    assertThat(result.getProvider()).isNull();
  }

  @Test
  void shouldMapOnlyHoldingId_whenSalesChargeDatapointIsNull() {
    var smsResponse = mock(SalesChargeData.class);
    when(smsResponse.getSalesCharge()).thenReturn(null);

    SalesCharge result = sut.map(smsResponse, createHolding("SEC-003"));

    assertThat(result.getHoldingId()).isEqualTo("SEC-003");
    assertThat(result.getType()).isNull();
    assertThat(result.getProvider()).isNull();
  }

  @Test
  void shouldMapDifferentSalesChargeTypes() {
    var smsResponse = mock(SalesChargeData.class);
    var salesCharge = mock(com.fintex.sm.model.domain.datapoint.SalesCharge.class);
    when(smsResponse.getSalesCharge()).thenReturn(salesCharge);
    when(salesCharge.getValue()).thenReturn(SalesChargeType.FRONT_END_CHARGE);
    when(salesCharge.getDataProvider()).thenReturn(DataProvider.MORNINGSTAR);

    SalesCharge result = sut.map(smsResponse, createHolding("SEC-004"));

    assertThat(result.getType()).isEqualTo(SalesChargeType.FRONT_END_CHARGE);
    assertThat(result.getProvider()).isEqualTo("MORNINGSTAR");
  }

  private Holding createHolding(String securityId) {
    var identifier = new SecurityIdentifier();
    identifier.setId(securityId);
    return new Holding()
            .setHoldingType(FinancialInstrumentType.MUTUAL_FUND_CANADA)
            .setSecurityIdentifier(identifier);
  }
}
