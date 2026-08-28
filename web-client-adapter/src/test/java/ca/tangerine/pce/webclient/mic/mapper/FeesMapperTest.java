package ca.tangerine.pce.webclient.mic.mapper;

import ca.tangerine.pce.model.domain.calculation.fee.FeeData;
import ca.tangerine.wm.commons.domain.DataProvider;
import ca.tangerine.wm.commons.domain.datapoint.FloatDatapoint;
import ca.tangerine.wm.commons.domain.enumeration.Country;
import ca.tangerine.wm.commons.domain.enumeration.FinancialInstrumentType;
import ca.tangerine.wm.commons.domain.financial.Fees;
import ca.tangerine.wm.commons.domain.financial.ManagementFeeDatapoint;
import ca.tangerine.wm.commons.domain.id.SecurityIdentifier;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static ca.tangerine.pce.util.PortfolioHoldingBuildHelper.holding;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * MIC returns fee fields in percentage form (e.g. {@code 1.51} meaning 1.51%). The mapper must convert to ratio form
 * ({@code 0.0151}) at the adapter boundary so the rest of the engine works in consistent units.
 */
class FeesMapperTest {

  private final FeesMapper mapper = new FeesMapper();

  @Test
  void mapsPercentageValuesToRatioForm() {
    var managementFee = new ManagementFeeDatapoint();
    managementFee.setValue(new BigDecimal("1.25"));
    managementFee.setDataProviders(List.of(DataProvider.MORNINGSTAR));

    var mer = new FloatDatapoint();
    mer.setValue(new BigDecimal("2.25"));
    mer.setDataProviders(List.of(DataProvider.MORNINGSTAR));

    var netExpenseRatio = new FloatDatapoint();
    netExpenseRatio.setValue(new BigDecimal("2.10"));
    netExpenseRatio.setDataProviders(List.of(DataProvider.MORNINGSTAR));

    var grossExpenseRatio = new FloatDatapoint();
    grossExpenseRatio.setValue(new BigDecimal("2.50"));
    grossExpenseRatio.setDataProviders(List.of(DataProvider.MORNINGSTAR));

    var actual12B1Fee = new FloatDatapoint();
    actual12B1Fee.setValue(new BigDecimal("0.25"));
    actual12B1Fee.setDataProviders(List.of(DataProvider.MORNINGSTAR));

    var micResponse = new Fees();
    micResponse.setManagementFee(managementFee);
    micResponse.setManagementExpenseRatio(mer);
    micResponse.setNetExpenseRatio(netExpenseRatio);
    micResponse.setGrossExpenseRatio(grossExpenseRatio);
    micResponse.setActual12B1Fee(actual12B1Fee);

    FeeData result = mapper.map(micResponse, holding(new SecurityIdentifier("SEC-001", null),
        FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, (BigDecimal) null));

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
  void returnsAllNullFeeData_whenResponseIsNull() {
    FeeData result = mapper.map(null, holding(new SecurityIdentifier("SEC-002", null),
        FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, (BigDecimal) null));

    assertThat(result.getManagementFee()).isNull();
    assertThat(result.getManagementExpenseRatio()).isNull();
    assertThat(result.getNetExpenseRatio()).isNull();
    assertThat(result.getGrossExpenseRatio()).isNull();
    assertThat(result.getActual12B1Fee()).isNull();
  }

  @Test
  void returnsAllNullFeeData_whenResponseHasNullDatapoints() {
    var micResponse = new Fees();

    FeeData result = mapper.map(micResponse, holding(new SecurityIdentifier("SEC-003", null),
        FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, (BigDecimal) null));

    assertThat(result.getManagementFee()).isNull();
    assertThat(result.getManagementExpenseRatio()).isNull();
  }

  @Test
  void mapsPartialFields_keepingNullFieldsNull() {
    var mer = new FloatDatapoint();
    mer.setValue(new BigDecimal("1.90"));
    mer.setDataProviders(List.of(DataProvider.MORNINGSTAR));

    var micResponse = new Fees();
    micResponse.setManagementExpenseRatio(mer);

    FeeData result = mapper.map(micResponse, holding(new SecurityIdentifier("SEC-004", null),
        FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, (BigDecimal) null));

    assertThat(result.getManagementExpenseRatio()).isEqualByComparingTo("0.019");
    assertThat(result.getManagementExpenseRatioProvider()).isEqualTo(DataProvider.MORNINGSTAR);
    assertThat(result.getManagementFee()).isNull();
    assertThat(result.getNetExpenseRatio()).isNull();
    assertThat(result.getGrossExpenseRatio()).isNull();
  }

}
