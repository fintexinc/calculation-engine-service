package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.model.domain.calculation.holding.CommonTopHoldings;
import com.fintex.ce.model.domain.calculation.holding.CommonTopHoldings.CommonTopHolding;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.enumeration.LanguageCode;
import com.fintex.wm.commons.domain.holding.TopHolding;
import com.fintex.wm.commons.domain.holding.TopHoldings;
import com.fintex.wm.commons.domain.id.ExternalIdentifiers;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.IdentifierTypeValue;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;
import com.fintex.wm.commons.domain.value.MultilingualString;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TopHoldingsMapperTest {

  private final TopHoldingsMapper mapper = new TopHoldingsMapper();

  @Test
  void shouldMapHoldingsAndProvider() {
    var sh = new TopHolding();
    sh.setName(List.of(new MultilingualString(LanguageCode.EN, "Royal Bank of Canada")));
    sh.setCompanyName("RBC");
    sh.setType("E");
    sh.setWeighting(BigDecimal.valueOf(0.05));
    sh.setMarketValue(BigDecimal.valueOf(50000));

    var smsResponse = new TopHoldings();
    smsResponse.setAllocation(List.of(sh));
    smsResponse.setDataProvider(DataProvider.MORNINGSTAR);

    CommonTopHoldings result = mapper.map(smsResponse, createHolding("SEC-001"));

    assertThat(result.getHoldingId()).isEqualTo("SEC-001");
    assertThat(result.getProviders()).containsExactly(DataProvider.MORNINGSTAR);
    assertThat(result.getHoldings()).hasSize(1);

    CommonTopHolding ch = result.getHoldings().get(0);
    assertThat(ch.getName()).isEqualTo("Royal Bank of Canada");
    assertThat(ch.getCompanyName()).isEqualTo("RBC");
    assertThat(ch.getType()).isEqualTo("E");
    assertThat(ch.getWeight()).isEqualByComparingTo("0.05");
    assertThat(ch.getValue()).isEqualByComparingTo("50000");
  }

  @Test
  void shouldExtractTickerFromExternalIdentifiers() {
    var ticker = new IdentifierTypeValue();
    ticker.setType(FiIdentifierType.TICKER);
    ticker.setValue("RY.TO");
    var exchangeId = new IdentifierTypeValue();
    exchangeId.setType(FiIdentifierType.EXCHANGE_ID);
    exchangeId.setValue("TSX");

    var externalIds = new ExternalIdentifiers();
    externalIds.setCodes(List.of(ticker, exchangeId));

    var sh = new TopHolding();
    sh.setName(List.of(new MultilingualString(LanguageCode.EN, "Royal Bank")));
    sh.setExternalIdentifiers(externalIds);

    var smsResponse = new TopHoldings();
    smsResponse.setAllocation(List.of(sh));

    CommonTopHoldings result = mapper.map(smsResponse, createHolding("SEC-002"));

    assertThat(result.getHoldings().get(0).getIdentifiers()).containsExactly(ticker, exchangeId);
  }

  @Test
  void shouldReturnEmptyHoldings_whenResponseIsNull() {
    CommonTopHoldings result = mapper.map(null, createHolding("SEC-003"));

    assertThat(result.getHoldingId()).isEqualTo("SEC-003");
    assertThat(result.getProviders()).isEmpty();
    assertThat(result.getHoldings()).isEmpty();
  }

  @Test
  void shouldReturnEmptyHoldings_whenAllocationIsNull() {
    var smsResponse = new TopHoldings();
    smsResponse.setAllocation(null);

    CommonTopHoldings result = mapper.map(smsResponse, createHolding("SEC-004"));

    assertThat(result.getHoldings()).isEmpty();
  }

  @Test
  void shouldNotSetProvider_whenDataProviderIsNull() {
    var smsResponse = new TopHoldings();
    smsResponse.setAllocation(List.of());
    smsResponse.setDataProvider(null);

    CommonTopHoldings result = mapper.map(smsResponse, createHolding("SEC-005"));

    assertThat(result.getProviders()).isEmpty();
  }

  @Test
  void shouldMapUnderlyingHoldings_recursively() {
    var underlying = new TopHolding();
    underlying.setName(List.of(new MultilingualString(LanguageCode.EN, "Sub PortfolioHolding")));
    underlying.setType("B");
    underlying.setWeighting(BigDecimal.valueOf(0.02));

    var parent = new TopHolding();
    parent.setName(List.of(new MultilingualString(LanguageCode.EN, "Parent Fund")));
    parent.setType("FO");
    parent.setWeighting(BigDecimal.valueOf(0.10));
    parent.setUnderlyingHoldings(List.of(underlying));

    var smsResponse = new TopHoldings();
    smsResponse.setAllocation(List.of(parent));

    CommonTopHoldings result = mapper.map(smsResponse, createHolding("SEC-006"));

    assertThat(result.getHoldings()).hasSize(1);
    CommonTopHolding parentHolding = result.getHoldings().get(0);
    assertThat(parentHolding.getName()).isEqualTo("Parent Fund");
    assertThat(parentHolding.getUnderlyingHoldings()).hasSize(1);
    assertThat(parentHolding.getUnderlyingHoldings().get(0).getName()).isEqualTo("Sub PortfolioHolding");
  }

  private PortfolioHolding createHolding(String securityId) {
    return new PortfolioHolding(null, FinancialInstrumentType.MUTUAL_FUND_CANADA, new SecurityIdentifier(securityId,
        null));
  }
}
