package ca.tangerine.pce.webclient.mic.mapper;

import ca.tangerine.pce.model.domain.calculation.allocation.EquitySector;
import ca.tangerine.wm.commons.domain.DataProvider;
import ca.tangerine.wm.commons.domain.allocation.EquitySectorAllocationType;
import ca.tangerine.wm.commons.domain.allocation.EquitySectorDatapoint;
import ca.tangerine.wm.commons.domain.allocation.EquitySectorWithCurrency;
import ca.tangerine.wm.commons.domain.currency.Currency;
import ca.tangerine.wm.commons.domain.currency.CurrencyDatapoint;
import ca.tangerine.wm.commons.domain.enumeration.Country;
import ca.tangerine.wm.commons.domain.enumeration.FinancialInstrumentType;
import ca.tangerine.wm.commons.domain.id.FiIdentifierType;
import ca.tangerine.wm.commons.domain.id.SecurityIdentifier;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static ca.tangerine.pce.util.PortfolioHoldingBuildHelper.holding;
import static org.assertj.core.api.Assertions.assertThat;

class EquitySectorMapperTest {

  private final EquitySectorMapper mapper = new EquitySectorMapper();

  @Test
  void shouldWidenTheSectorToOneFullBucket_whenTheSecurityHasOne() {
    EquitySector result = mapper.map(response(EquitySectorAllocationType.ENERGY, Currency.USD), holding(
        new SecurityIdentifier("XOM", FiIdentifierType.TICKER), FinancialInstrumentType.STOCK,
        Country.USA, new BigDecimal("100")));

    assertThat(result.getAllocations()).containsExactly(
        Map.entry(EquitySectorAllocationType.ENERGY, BigDecimal.ONE));
    assertThat(result.getCurrency()).isEqualTo(Currency.USD);
    assertThat(result.getProviders()).containsExactly(DataProvider.FMP);
  }

  /**
   * No bucket rather than an {@code UNKNOWN} one, so the metric reads it as absent data and warns, the way it does for
   * a fund whose allocation is missing — rather than reporting a company as belonging to a sector named Unknown.
   */
  @Test
  void shouldMapToNoBuckets_whenTheSecurityHasNoSector() {
    EquitySector result = mapper.map(response(null, Currency.CAD), holding(new SecurityIdentifier("XOM",
        FiIdentifierType.TICKER), FinancialInstrumentType.STOCK, Country.USA, new BigDecimal("100")));

    assertThat(result.getAllocations()).isEmpty();
    assertThat(result.getCurrency()).isEqualTo(Currency.CAD);
    assertThat(result.getProviders()).isEmpty();
  }

  @Test
  void shouldMapToNoBucketsAndNoCurrency_whenMarketInvestmentCatalogueReturnedNothing() {
    EquitySector result = mapper.map(null, holding(new SecurityIdentifier("XOM", FiIdentifierType.TICKER),
        FinancialInstrumentType.STOCK, Country.USA, new BigDecimal("100")));

    assertThat(result.getAllocations()).isEmpty();
    assertThat(result.getCurrency()).isNull();
    assertThat(result.getProviders()).isEmpty();
  }

  private static EquitySectorWithCurrency response(EquitySectorAllocationType sector, Currency currency) {
    var response = new EquitySectorWithCurrency();
    if (sector != null) {
      var datapoint = new EquitySectorDatapoint();
      datapoint.setEquitySector(sector);
      datapoint.setDataProviders(List.of(DataProvider.FMP));
      response.setSector(datapoint);
    }
    var currencyDatapoint = new CurrencyDatapoint();
    currencyDatapoint.setType(currency);
    response.setCurrency(currencyDatapoint);
    return response;
  }

}
