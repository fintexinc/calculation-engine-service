package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.model.domain.calculation.fee.SalesCharge;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;
import com.fintex.wm.commons.domain.sales.SalesChargeData;
import com.fintex.wm.commons.domain.sales.SalesChargeType;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SalesChargeTypeMapperTest {

  private final SalesChargeMapper mapper = new SalesChargeMapper();

  @Test
  void shouldMapValueAndProvider_whenResponseHasValues() {
    var smsResponse = mock(SalesChargeData.class);
    var salesCharge = mock(com.fintex.wm.commons.domain.sales.SalesCharge.class);
    when(smsResponse.getSalesCharge()).thenReturn(salesCharge);
    when(salesCharge.getValue()).thenReturn(SalesChargeType.DEFERRED_SALES_CHARGE);
    when(salesCharge.getDataProviders()).thenReturn(List.of(DataProvider.MORNINGSTAR));

    SalesCharge result = mapper.map(smsResponse, createHolding("SEC-001"));

    assertThat(result.getType()).isEqualTo(SalesChargeType.DEFERRED_SALES_CHARGE);
    assertThat(result.getProviders()).containsExactly(DataProvider.MORNINGSTAR);
  }

  @Test
  void shouldReturnEmptySalesCharge_whenResponseIsNull() {
    SalesCharge result = mapper.map(null, createHolding("SEC-002"));

    assertThat(result.getType()).isNull();
    assertThat(result.getProviders()).isEmpty();
  }

  @Test
  void shouldReturnEmptySalesCharge_whenSalesChargeDatapointIsNull() {
    var smsResponse = mock(SalesChargeData.class);
    when(smsResponse.getSalesCharge()).thenReturn(null);

    SalesCharge result = mapper.map(smsResponse, createHolding("SEC-003"));

    assertThat(result.getType()).isNull();
    assertThat(result.getProviders()).isEmpty();
  }

  @Test
  void shouldMapDifferentSalesChargeTypes() {
    var smsResponse = mock(SalesChargeData.class);
    var salesCharge = mock(com.fintex.wm.commons.domain.sales.SalesCharge.class);
    when(smsResponse.getSalesCharge()).thenReturn(salesCharge);
    when(salesCharge.getValue()).thenReturn(SalesChargeType.FRONT_END_CHARGE);
    when(salesCharge.getDataProviders()).thenReturn(List.of(DataProvider.MORNINGSTAR));

    SalesCharge result = mapper.map(smsResponse, createHolding("SEC-004"));

    assertThat(result.getType()).isEqualTo(SalesChargeType.FRONT_END_CHARGE);
    assertThat(result.getProviders()).containsExactly(DataProvider.MORNINGSTAR);
  }

  private PortfolioHolding createHolding(String securityId) {
    var identifier = new SecurityIdentifier();
    identifier.setId(securityId);
    return PortfolioHolding.builder()
        .holdingType(FinancialInstrumentType.MUTUAL_FUND_CANADA)
        .securityIdentifier(identifier)
        .build();
  }
}
