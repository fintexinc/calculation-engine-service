package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.model.domain.calculation.fee.FeeData;
import com.fintex.ce.model.domain.holding.Holding;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.datapoint.FloatDatapoint;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.financial.Fees;
import com.fintex.wm.commons.domain.financial.ManagementFeeDatapoint;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class FeesMapperTest {

  private final FeesMapper sut = new FeesMapper();

  @Test
  void shouldMapAllFieldsAndProviders_whenResponseHasValues() {
    var managementFee = new ManagementFeeDatapoint();
    managementFee.setValue(BigDecimal.valueOf(0.0125));
    managementFee.setDataProvider(DataProvider.MORNINGSTAR);

    var mer = new FloatDatapoint();
    mer.setValue(BigDecimal.valueOf(0.0225));
    mer.setDataProvider(DataProvider.MORNINGSTAR);

    var netExpenseRatio = new FloatDatapoint();
    netExpenseRatio.setValue(BigDecimal.valueOf(0.021));
    netExpenseRatio.setDataProvider(DataProvider.MORNINGSTAR);

    var grossExpenseRatio = new FloatDatapoint();
    grossExpenseRatio.setValue(BigDecimal.valueOf(0.025));
    grossExpenseRatio.setDataProvider(DataProvider.MORNINGSTAR);

    var actual12B1Fee = new FloatDatapoint();
    actual12B1Fee.setValue(BigDecimal.valueOf(0.0025));
    actual12B1Fee.setDataProvider(DataProvider.MORNINGSTAR);

    var smsResponse = new Fees();
    smsResponse.setManagementFee(managementFee);
    smsResponse.setManagementExpenseRatio(mer);
    smsResponse.setNetExpenseRatio(netExpenseRatio);
    smsResponse.setGrossExpenseRatio(grossExpenseRatio);
    smsResponse.setActual12B1Fee(actual12B1Fee);

    FeeData result = sut.map(smsResponse, createHolding("SEC-001"));

    assertThat(result.getHoldingId()).isEqualTo("SEC-001");
    assertThat(result.getManagementFee()).isEqualByComparingTo("0.0125");
    assertThat(result.getManagementFeeProvider()).isEqualTo(DataProvider.MORNINGSTAR);
    assertThat(result.getManagementExpenseRatio()).isEqualByComparingTo("0.0225");
    assertThat(result.getManagementExpenseRatioProvider()).isEqualTo(DataProvider.MORNINGSTAR);
    assertThat(result.getNetExpenseRatio()).isEqualByComparingTo("0.021");
    assertThat(result.getNetExpenseRatioProvider()).isEqualTo(DataProvider.MORNINGSTAR);
    assertThat(result.getGrossExpenseRatio()).isEqualByComparingTo("0.025");
    assertThat(result.getGrossExpenseRatioProvider()).isEqualTo(DataProvider.MORNINGSTAR);
    assertThat(result.getActual12B1Fee()).isEqualByComparingTo("0.0025");
    assertThat(result.getActual12B1FeeProvider()).isEqualTo(DataProvider.MORNINGSTAR);
  }

  @Test
  void shouldReturnEmptyFeeData_whenResponseIsNull() {
    FeeData result = sut.map(null, createHolding("SEC-002"));

    assertThat(result.getHoldingId()).isEqualTo("SEC-002");
    assertThat(result.getManagementFee()).isNull();
    assertThat(result.getManagementExpenseRatio()).isNull();
    assertThat(result.getNetExpenseRatio()).isNull();
    assertThat(result.getGrossExpenseRatio()).isNull();
    assertThat(result.getActual12B1Fee()).isNull();
  }

  @Test
  void shouldMapOnlyHoldingId_whenResponseHasNullDatapoints() {
    var smsResponse = new Fees();

    FeeData result = sut.map(smsResponse, createHolding("SEC-003"));

    assertThat(result.getHoldingId()).isEqualTo("SEC-003");
    assertThat(result.getManagementFee()).isNull();
    assertThat(result.getManagementExpenseRatio()).isNull();
  }

  @Test
  void shouldMapPartialFields_whenSomeValuesArePresent() {
    var mer = new FloatDatapoint();
    mer.setValue(BigDecimal.valueOf(0.019));
    mer.setDataProvider(DataProvider.MORNINGSTAR);

    var smsResponse = new Fees();
    smsResponse.setManagementExpenseRatio(mer);

    FeeData result = sut.map(smsResponse, createHolding("SEC-004"));

    assertThat(result.getManagementExpenseRatio()).isEqualByComparingTo("0.019");
    assertThat(result.getManagementExpenseRatioProvider()).isEqualTo(DataProvider.MORNINGSTAR);
    assertThat(result.getManagementFee()).isNull();
    assertThat(result.getNetExpenseRatio()).isNull();
    assertThat(result.getGrossExpenseRatio()).isNull();
  }

  private Holding createHolding(String securityId) {
    var identifier = new SecurityIdentifier();
    identifier.setId(securityId);
    return new Holding(null, FinancialInstrumentType.MUTUAL_FUND_CANADA, identifier);
  }
}
