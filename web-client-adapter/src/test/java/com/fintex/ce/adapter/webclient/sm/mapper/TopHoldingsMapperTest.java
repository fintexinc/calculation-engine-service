package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.model.domain.calculation.holding.CommonHolding;
import com.fintex.ce.model.domain.calculation.holding.CommonTopHoldings;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.enumeration.LanguageCode;
import com.fintex.wm.commons.domain.holding.TopHolding;
import com.fintex.wm.commons.domain.holding.TopHoldings;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.IdentifiersDatapoint;
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
    // SM weighting comes in on a percent (0-100) scale; the mapper normalises to a ratio (0-1) via percentageToRatio.
    var sh = new TopHolding();
    sh.setName(List.of(new MultilingualString(LanguageCode.EN, "Royal Bank of Canada")));
    sh.setCompanyName("RBC");
    sh.setType("E");
    sh.setWeighting(BigDecimal.valueOf(5));
    sh.setMarketValue(BigDecimal.valueOf(50000));

    var smsResponse = new TopHoldings();
    smsResponse.setAllocation(List.of(sh));
    smsResponse.setDataProviders(List.of(DataProvider.MORNINGSTAR));
    smsResponse.setCurrency(Currency.CAD);

    CommonTopHoldings result = mapper.map(smsResponse, createHolding("SEC-001"));

    assertThat(result.getProviders()).containsExactly(DataProvider.MORNINGSTAR);
    assertThat(result.getCurrency()).isEqualTo(Currency.CAD);
    assertThat(result.getHoldings()).hasSize(1);

    CommonHolding ch = result.getHoldings().get(0);
    assertThat(ch.getName()).isEqualTo("Royal Bank of Canada");
    assertThat(ch.getCompanyName()).isEqualTo("RBC");
    assertThat(ch.getType()).isEqualTo("E");
    assertThat(ch.getWeight()).isEqualByComparingTo("0.05");
    assertThat(ch.getValue()).isEqualByComparingTo("50000");
  }

  @Test
  void shouldSelectTickerAsPrimaryIdentifier_whenOnlyTickerPresent() {
    var sh = topHoldingWithIdentifiers(
        identifier(FiIdentifierType.TICKER, "RY.TO"),
        identifier(FiIdentifierType.EXCHANGE_ID, "TSX"));

    CommonHolding ch = mapper.map(allocation(sh), createHolding("SEC-002")).getHoldings().get(0);

    assertThat(ch.getPrimaryIdentifier()).isNotNull();
    assertThat(ch.getPrimaryIdentifier().getIdType()).isEqualTo(FiIdentifierType.TICKER);
    assertThat(ch.getPrimaryIdentifier().getId()).isEqualTo("RY.TO");
  }

  @Test
  void shouldSelectMorningstarOverTickerAndFundserv_whenAllPresent() {
    var sh = topHoldingWithIdentifiers(
        identifier(FiIdentifierType.FUNDSERV, "RBF1234"),
        identifier(FiIdentifierType.TICKER, "RY.TO"),
        identifier(FiIdentifierType.MORNINGSTAR_ID, "F0CAN05NHL"));

    CommonHolding ch = mapper.map(allocation(sh), createHolding("SEC-007")).getHoldings().get(0);

    assertThat(ch.getPrimaryIdentifier().getIdType()).isEqualTo(FiIdentifierType.MORNINGSTAR_ID);
    assertThat(ch.getPrimaryIdentifier().getId()).isEqualTo("F0CAN05NHL");
  }

  @Test
  void shouldFallBackToFundserv_whenNoTickerOrMorningstar() {
    var sh = topHoldingWithIdentifiers(identifier(FiIdentifierType.FUNDSERV, "RBF1234"));

    CommonHolding ch = mapper.map(allocation(sh), createHolding("SEC-008")).getHoldings().get(0);

    assertThat(ch.getPrimaryIdentifier().getIdType()).isEqualTo(FiIdentifierType.FUNDSERV);
    assertThat(ch.getPrimaryIdentifier().getId()).isEqualTo("RBF1234");
  }

  @Test
  void shouldReturnNullPrimaryIdentifier_whenSmHasNoIdentifiers() {
    var sh = new TopHolding();
    sh.setName(List.of(new MultilingualString(LanguageCode.EN, "Bond Holding")));
    sh.setIdentifiers(null);

    CommonHolding ch = mapper.map(allocation(sh), createHolding("SEC-009")).getHoldings().get(0);

    assertThat(ch.getPrimaryIdentifier()).isNull();
  }

  @Test
  void shouldReturnEmptyHoldings_whenResponseIsNull() {
    CommonTopHoldings result = mapper.map(null, createHolding("SEC-003"));

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
    smsResponse.setDataProviders(null);

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
    CommonHolding parentHolding = result.getHoldings().get(0);
    assertThat(parentHolding.getName()).isEqualTo("Parent Fund");
    assertThat(parentHolding.getUnderlyingHoldings()).hasSize(1);
    assertThat(parentHolding.getUnderlyingHoldings().get(0).getName()).isEqualTo("Sub PortfolioHolding");
  }

  private PortfolioHolding createHolding(String securityId) {
    return new PortfolioHolding(null, FinancialInstrumentType.MUTUAL_FUND_CANADA, new SecurityIdentifier(securityId,
        null));
  }

  private static SecurityIdentifier identifier(FiIdentifierType type, String id) {
    var si = new SecurityIdentifier();
    si.setIdType(type);
    si.setId(id);
    return si;
  }

  private static TopHolding topHoldingWithIdentifiers(SecurityIdentifier... ids) {
    var datapoint = new IdentifiersDatapoint();
    datapoint.setIdentifiers(List.of(ids));
    var sh = new TopHolding();
    sh.setName(List.of(new MultilingualString(LanguageCode.EN, "Test Holding")));
    sh.setIdentifiers(datapoint);
    return sh;
  }

  private static TopHoldings allocation(TopHolding... holdings) {
    var response = new TopHoldings();
    response.setAllocation(List.of(holdings));
    return response;
  }
}
