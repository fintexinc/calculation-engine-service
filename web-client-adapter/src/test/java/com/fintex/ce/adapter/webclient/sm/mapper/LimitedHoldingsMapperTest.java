package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.model.domain.calculation.holding.CommonHolding;
import com.fintex.ce.model.domain.calculation.holding.CommonTopHoldings;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.Country;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.enumeration.LanguageCode;
import com.fintex.wm.commons.domain.holding.HoldingType;
import com.fintex.wm.commons.domain.holding.Holdings;
import com.fintex.wm.commons.domain.holding.SecurityHolding;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.IdentifiersDatapoint;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;
import com.fintex.wm.commons.domain.value.MultilingualString;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static com.fintex.ce.test.PortfolioHoldingBuildHelper.holding;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Covers the {@code LIMITED_HOLDINGS} payload and, through it, the conversion shared by every holdings mapper — field
 * mapping, ratio conversion and primary-identifier selection.
 */
class LimitedHoldingsMapperTest {

  private final LimitedHoldingsMapper mapper = new LimitedHoldingsMapper();

  @Test
  void shouldMapAllocationCurrencyAndProviders_whenHoldingsArePopulated() {
    var response = holdings(Currency.CAD,
        securityHolding("NVIDIA Corp", "8.87516", FiIdentifierType.MORNINGSTAR_ID, "0P000003RE"),
        securityHolding("Microsoft Corp", "7.54319", FiIdentifierType.MORNINGSTAR_ID, "0P00000203"));

    CommonTopHoldings result = mapper.map(response, holding(new SecurityIdentifier("F00001S8IG",
        FiIdentifierType.MORNINGSTAR_ID), FinancialInstrumentType.ETF, Country.CANADA, (BigDecimal) null));

    assertThat(result.getCurrency()).isEqualTo(Currency.CAD);
    assertThat(result.getProviders()).containsExactly(DataProvider.MORNINGSTAR);
    assertThat(result.getHoldings()).hasSize(2);

    CommonHolding nvidia = result.getHoldings().get(0);
    assertThat(nvidia.getName()).isEqualTo("NVIDIA Corp");
    assertThat(nvidia.getCompanyName()).isEqualTo("NVIDIA Corp");
    assertThat(nvidia.getType()).isEqualTo(HoldingType.E);
    // SM sends weighting on a percent (0-100) scale; the calculation expects a ratio (0-1).
    assertThat(nvidia.getWeight()).isEqualByComparingTo("0.0887516");
    assertThat(nvidia.getPrimaryIdentifier().getIdType()).isEqualTo(FiIdentifierType.MORNINGSTAR_ID);
    assertThat(nvidia.getPrimaryIdentifier().getId()).isEqualTo("0P000003RE");
  }

  @Test
  void shouldMapMarketValue_whenTheHoldingCarriesOne() {
    var holding = securityHolding("Royal Bank of Canada", "5", null, null);
    holding.setMarketValue(BigDecimal.valueOf(50000));

    CommonHolding mapped = mapper.map(holdings(Currency.CAD, holding), holding(new SecurityIdentifier("SEC-001",
        FiIdentifierType.MORNINGSTAR_ID), FinancialInstrumentType.ETF, Country.CANADA, (BigDecimal) null)).getHoldings()
        .get(0);

    assertThat(mapped.getValue()).isEqualByComparingTo("50000");
    assertThat(mapped.getWeight()).isEqualByComparingTo("0.05");
  }

  /**
   * The resolver aggregates nested funds into their leaves before responding, so every row arrives flat and the
   * calculation's tree expansion has nothing to descend into.
   */
  @Test
  void shouldLeaveUnderlyingHoldingsEmpty_whenAllocationIsFlat() {
    var response = holdings(Currency.USD, securityHolding("Amazon.com Inc", "4.49273", null, null));

    CommonTopHoldings result = mapper.map(response, holding(new SecurityIdentifier("F00001S8IH",
        FiIdentifierType.MORNINGSTAR_ID), FinancialInstrumentType.ETF, Country.CANADA, (BigDecimal) null));

    assertThat(result.getHoldings()).singleElement()
        .satisfies(holding -> assertThat(holding.getUnderlyingHoldings()).isEmpty());
  }

  @Test
  void shouldMapUnderlyingHoldings_recursively() {
    var underlying = securityHolding("Sub Holding", "0.02", null, null);
    var parent = securityHolding("Parent Fund", "0.10", null, null);
    parent.setUnderlyingHoldings(List.of(underlying));

    CommonTopHoldings result = mapper.map(holdings(Currency.CAD, parent), holding(new SecurityIdentifier("SEC-006",
        FiIdentifierType.MORNINGSTAR_ID), FinancialInstrumentType.ETF, Country.CANADA, (BigDecimal) null));

    assertThat(result.getHoldings()).hasSize(1);
    CommonHolding parentHolding = result.getHoldings().get(0);
    assertThat(parentHolding.getName()).isEqualTo("Parent Fund");
    assertThat(parentHolding.getUnderlyingHoldings()).hasSize(1);
    assertThat(parentHolding.getUnderlyingHoldings().get(0).getName()).isEqualTo("Sub Holding");
  }

  @Test
  void shouldSelectMorningstarOverEveryOtherIdentifier_whenAllArePresent() {
    var holding = holdingWithIdentifiers(
        identifier(FiIdentifierType.FUNDSERV, "RBF1234"),
        identifier(FiIdentifierType.TICKER, "RY.TO"),
        identifier(FiIdentifierType.MORNINGSTAR_ID, "F0CAN05NHL"));

    CommonHolding mapped = mapper.map(holdings(Currency.CAD, holding), holding(new SecurityIdentifier("SEC-007",
        FiIdentifierType.MORNINGSTAR_ID), FinancialInstrumentType.ETF, Country.CANADA, (BigDecimal) null)).getHoldings()
        .get(0);

    assertThat(mapped.getPrimaryIdentifier().getIdType()).isEqualTo(FiIdentifierType.MORNINGSTAR_ID);
    assertThat(mapped.getPrimaryIdentifier().getId()).isEqualTo("F0CAN05NHL");
  }

  /** Ticker is last in the priority: the same symbol is reused across exchanges, so it is the least unique of them. */
  @Test
  void shouldSelectFundservOverTicker_whenBothPresent() {
    var holding = holdingWithIdentifiers(
        identifier(FiIdentifierType.TICKER, "RY.TO"),
        identifier(FiIdentifierType.FUNDSERV, "RBF1234"));

    CommonHolding mapped = mapper.map(holdings(Currency.CAD, holding), holding(new SecurityIdentifier("SEC-010",
        FiIdentifierType.MORNINGSTAR_ID), FinancialInstrumentType.ETF, Country.CANADA, (BigDecimal) null)).getHoldings()
        .get(0);

    assertThat(mapped.getPrimaryIdentifier().getIdType()).isEqualTo(FiIdentifierType.FUNDSERV);
    assertThat(mapped.getPrimaryIdentifier().getId()).isEqualTo("RBF1234");
  }

  @Test
  void shouldSelectTicker_whenItIsTheOnlyRankedIdentifierPresent() {
    var holding = holdingWithIdentifiers(
        identifier(FiIdentifierType.TICKER, "RY.TO"),
        identifier(FiIdentifierType.EXCHANGE_ID, "TSX"));

    CommonHolding mapped = mapper.map(holdings(Currency.CAD, holding), holding(new SecurityIdentifier("SEC-002",
        FiIdentifierType.MORNINGSTAR_ID), FinancialInstrumentType.ETF, Country.CANADA, (BigDecimal) null)).getHoldings()
        .get(0);

    assertThat(mapped.getPrimaryIdentifier().getIdType()).isEqualTo(FiIdentifierType.TICKER);
    assertThat(mapped.getPrimaryIdentifier().getId()).isEqualTo("RY.TO");
  }

  /** A missing optional holding type must remain null without dropping the row. */
  @Test
  void shouldLeaveTypeNull_whenSmsHoldingTypeIsNull() {
    var holding = securityHolding("Missing Type", "1.00", null, null);
    holding.setType(null);

    CommonHolding mapped = mapper.map(holdings(Currency.CAD, holding), holding(new SecurityIdentifier("SEC-011",
        FiIdentifierType.MORNINGSTAR_ID), FinancialInstrumentType.ETF, Country.CANADA, (BigDecimal) null)).getHoldings()
        .get(0);

    assertThat(mapped.getName()).isEqualTo("Missing Type");
    assertThat(mapped.getType()).isNull();
    assertThat(mapped.getWeight()).isEqualByComparingTo("0.01");
  }

  @Test
  void shouldReturnNullPrimaryIdentifier_whenTheHoldingHasNoIdentifiers() {
    var holding = securityHolding("Bond Holding", "1.00", null, null);

    CommonHolding mapped = mapper.map(holdings(Currency.CAD, holding), holding(new SecurityIdentifier("SEC-009",
        FiIdentifierType.MORNINGSTAR_ID), FinancialInstrumentType.ETF, Country.CANADA, (BigDecimal) null)).getHoldings()
        .get(0);

    assertThat(mapped.getPrimaryIdentifier()).isNull();
  }

  @Test
  void shouldReturnEmptyHoldings_whenResponseIsNull() {
    CommonTopHoldings result = mapper.map(null, holding(new SecurityIdentifier("F00000MBXT",
        FiIdentifierType.MORNINGSTAR_ID), FinancialInstrumentType.ETF, Country.CANADA, (BigDecimal) null));

    assertThat(result.getCurrency()).isNull();
    assertThat(result.getProviders()).isEmpty();
    assertThat(result.getHoldings()).isEmpty();
  }

  @Test
  void shouldReturnEmptyHoldings_whenAllocationIsNull() {
    var response = new Holdings();
    response.setCurrency(Currency.CAD);

    CommonTopHoldings result = mapper.map(response, holding(new SecurityIdentifier("F00000MBXT",
        FiIdentifierType.MORNINGSTAR_ID), FinancialInstrumentType.ETF, Country.CANADA, (BigDecimal) null));

    assertThat(result.getCurrency()).isEqualTo(Currency.CAD);
    assertThat(result.getHoldings()).isEmpty();
  }

  @Test
  void shouldReturnEmptyProviders_whenDataProvidersAreNull() {
    var response = new Holdings();
    response.setAllocation(List.of());
    response.setDataProviders(null);

    CommonTopHoldings result = mapper.map(response, holding(new SecurityIdentifier("SEC-005",
        FiIdentifierType.MORNINGSTAR_ID), FinancialInstrumentType.ETF, Country.CANADA, (BigDecimal) null));

    assertThat(result.getProviders()).isEmpty();
  }

  private static Holdings holdings(Currency currency, SecurityHolding... allocation) {
    var response = new Holdings();
    response.setAllocation(List.of(allocation));
    response.setCurrency(currency);
    response.setDataProviders(List.of(DataProvider.MORNINGSTAR));
    return response;
  }

  private static SecurityHolding securityHolding(String name, String weighting, FiIdentifierType idType, String id) {
    var holding = new SecurityHolding();
    holding.setName(List.of(new MultilingualString(LanguageCode.EN, name)));
    holding.setCompanyName(name);
    holding.setType(HoldingType.E);
    holding.setWeighting(new BigDecimal(weighting));
    if (idType != null) {
      holding.setIdentifiers(identifiersOf(new SecurityIdentifier(id, idType)));
    }
    return holding;
  }

  private static SecurityHolding holdingWithIdentifiers(SecurityIdentifier... ids) {
    var holding = securityHolding("Test Holding", "1.00", null, null);
    holding.setIdentifiers(identifiersOf(ids));
    return holding;
  }

  private static IdentifiersDatapoint identifiersOf(SecurityIdentifier... ids) {
    var datapoint = new IdentifiersDatapoint();
    datapoint.setIdentifiers(List.of(ids));
    return datapoint;
  }

  private static SecurityIdentifier identifier(FiIdentifierType type, String id) {
    var securityIdentifier = new SecurityIdentifier();
    securityIdentifier.setIdType(type);
    securityIdentifier.setId(id);
    return securityIdentifier;
  }

}
