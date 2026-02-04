package com.fintex.ce.adapter.graphqlclient.util;

import com.fintex.ce.adapter.graphqlclient.util.GraphQlMapperUtils;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.model.AssetAllocation;
import com.fintex.ce.domain.model.CommonHoldings;
import com.fintex.ce.domain.model.CommonHoldingsDTO;
import com.fintex.ce.domain.model.EquitySector;
import com.fintex.ce.domain.model.ExternalIdentifiersDTO;
import com.fintex.ce.domain.model.FixedIncomeBondSecurities;
import com.fintex.smclient.graphql.CountryAllocation;
import com.fintex.smclient.graphql.CountryValue;
import com.fintex.smclient.graphql.CreditQualityRatingTypeValue;
import com.fintex.smclient.graphql.CreditQualityRatings;
import com.fintex.smclient.graphql.DataProvider;
import com.fintex.smclient.graphql.DateValue;
import com.fintex.smclient.graphql.EquityMarketCapitalization;
import com.fintex.smclient.graphql.EquityMarketCapitalizationType;
import com.fintex.smclient.graphql.EquityMarketCapitalizationTypeValue;
import com.fintex.smclient.graphql.EquitySectorAllocation;
import com.fintex.smclient.graphql.EquitySectorAllocationType;
import com.fintex.smclient.graphql.EquitySectorAllocationTypeNameValue;
import com.fintex.smclient.graphql.ExternalIdentifierType;
import com.fintex.smclient.graphql.ExternalIdentifierTypeValue;
import com.fintex.smclient.graphql.ExternalIdentifiers;
import com.fintex.smclient.graphql.FixedIncomeSecuritiesAllocation;
import com.fintex.smclient.graphql.Holding;
import com.fintex.smclient.graphql.HoldingValue;
import com.fintex.smclient.graphql.Holdings;
import com.fintex.smclient.graphql.LanguageCode;
import com.fintex.smclient.graphql.MonthlyReturns;
import com.fintex.smclient.graphql.MultilingualString;
import com.fintex.smclient.graphql.NameValue;
import com.fintex.ce.util.JacksonUtil;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.domain.enumeration.calculation.FixedIncomeSectorType.ST_INVESTMENTS;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GraphQlMapperUtilsTest {

  @Test
  void equityCountryAllocationMapper_checkResult() {
    // SETUP
    final CountryAllocation c = mock(CountryAllocation.class);

    // ACT
    final Map<String, BigDecimal> actual = GraphQlMapperUtils.equityCountryAllocationMapper(c);

    // VERIFY
    assertTrue(actual.isEmpty());
  }

  @Test
  void equityCountryAllocationMapper_checkResult2() {
    // SETUP
    final CountryAllocation c = mock(CountryAllocation.class);

    final CountryValue cValue = mock(CountryValue.class);
    when(c.getAllocation()).thenReturn(List.of(cValue));

    // ACT
    final Map<String, BigDecimal> actual = GraphQlMapperUtils.equityCountryAllocationMapper(c);

    // VERIFY
    assertTrue(actual.isEmpty());
  }

  @Test
  void equityCountryAllocationMapper_checkResult3() {
    // SETUP
    final CountryAllocation c = mock(CountryAllocation.class);

    final CountryValue cValue = mock(CountryValue.class);
    when(cValue.getValue()).thenReturn(BigDecimal.TEN);

    final MultilingualString mLang = mock(MultilingualString.class);
    when(mLang.getLanguageCode()).thenReturn(LanguageCode.EN);
    when(mLang.getValue()).thenReturn("TEST");

    when(cValue.getName()).thenReturn(List.of(mLang));
    when(c.getAllocation()).thenReturn(List.of(cValue));

    // ACT
    final Map<String, BigDecimal> actual = GraphQlMapperUtils.equityCountryAllocationMapper(c);

    // VERIFY
    assertEquals(Map.of("TEST", BigDecimal.TEN), actual);
  }

  @Test
  void equitySectorMapper_checkResult() {
    // SETUP
    final EquitySectorAllocation e = mock(EquitySectorAllocation.class);
    when(e.getAllocation()).thenReturn(List.of());

    // ACT
    final EquitySector actual = GraphQlMapperUtils.equitySectorMapper(e);

    // VERIFY
    final EquitySector expected = new EquitySector(Map.of());
    assertEquals(expected, actual);
  }

  @Test
  void equitySectorMapper_checkResult2() {
    // SETUP

    final EquitySectorAllocationTypeNameValue eS = mock(EquitySectorAllocationTypeNameValue.class);
    final MultilingualString lang = mock(MultilingualString.class);
    when(lang.getLanguageCode()).thenReturn(LanguageCode.EN);
    when(lang.getValue()).thenReturn(EquitySectorAllocationType.UTILITIES + "");
    when(eS.getNames()).thenReturn(List.of(lang));
    when(eS.getValue()).thenReturn(TEN);

    final EquitySectorAllocation e = mock(EquitySectorAllocation.class);
    when(e.getAllocation()).thenReturn(List.of(eS));
    when(e.getDataProvider()).thenReturn(DataProvider.MORNINGSTAR);

    // ACT
    final EquitySector actual = GraphQlMapperUtils.equitySectorMapper(e);

    // VERIFY
    final EquitySector expected = new EquitySector(Map.of(EquitySectorAllocationType.UTILITIES.name(), TEN));
    expected.setProvider(DataProvider.MORNINGSTAR.name());
    assertEquals(expected, actual);
  }

  @Test
  void topCommonHoldingsMapper_checkResult() {
    // SETUP
    final Holdings topHoldings = mock(Holdings.class);
    final CommonHoldings expected = new CommonHoldings();
    when(topHoldings.getAllocation()).thenReturn(List.of());

    // ACT
    final CommonHoldings actual = GraphQlMapperUtils.topCommonHoldingsMapper(topHoldings);

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void topCommonHoldingsMapper_checkResult2() {
    try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class);
        var mockedCollectionUtils = Mockito.mockStatic(CollectionUtils.class);
        var mockedJacksonUtil = Mockito.mockStatic(JacksonUtil.class)) {
      // SETUP
      final Holdings topHoldings = mock(Holdings.class);
      final CommonHoldingsDTO mock = new CommonHoldingsDTO();
      final List<CommonHoldingsDTO> commonHoldings = List.of(mock);
      final String holdings = "test";
      final List<HoldingValue> holdingValue = List.of(mock(HoldingValue.class));
      final CommonHoldings expected = new CommonHoldings(holdings);
      expected.setProvider(DataProvider.MORNINGSTAR.name());

      when(topHoldings.getAllocation()).thenReturn(holdingValue);
      when(topHoldings.getDataProvider()).thenReturn(DataProvider.MORNINGSTAR);
      mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.mapCommonHoldings(anyList())).thenReturn(commonHoldings);
      mockedCollectionUtils.when(() -> CollectionUtils.isEmpty(anyList())).thenReturn(false);
      mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.topCommonHoldingsMapper(any())).thenCallRealMethod();
      mockedJacksonUtil.when(() -> JacksonUtil.serialize(any())).thenReturn(holdings);

      // ACT
      final CommonHoldings actual = GraphQlMapperUtils.topCommonHoldingsMapper(topHoldings);

      // VERIFY
      assertEquals(expected, actual);
    }
  }

  @Test
  void isValid_checkResult() {
    // SETUP
    final Holding holding = mock(Holding.class);
    when(holding.getCompanyName()).thenReturn("Tesla");

    // ACT
    boolean expected = GraphQlMapperUtils.isValid(holding);

    // VERIFY
    assertTrue(expected);
  }

  @Test
  void isValid_checkResult2() {
    // SETUP
    final Holding holding = mock(Holding.class);
    when(holding.getCompanyName()).thenReturn(null);
    when(holding.getName()).thenReturn(List.of());

    // ACT
    boolean expected = GraphQlMapperUtils.isValid(holding);

    // VERIFY
    assertTrue(expected);
  }

  @Test
  void initializeCommonHolding_checkResult() {
    try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
      // SETUP
      final Holding holding = mock(Holding.class);
      final CommonHoldingsDTO dto = new CommonHoldingsDTO("testName", null, null, null, null, null, null, null, null,
          null);
      final MultilingualString multilingualString = mock(MultilingualString.class);

      when(multilingualString.getLanguageCode()).thenReturn(LanguageCode.EN);
      when(multilingualString.getValue()).thenReturn("testName");
      when(holding.getExternalIdentifiers()).thenReturn(mock(ExternalIdentifiers.class));
      when(holding.getName()).thenReturn(List.of(multilingualString));
      mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.initializeCommonHolding(any(), any()))
          .thenCallRealMethod();
      // ACT
      final CommonHoldingsDTO actual = GraphQlMapperUtils.initializeCommonHolding(holding, TEN);

      // VERIFY
      assertEquals("testName", actual.getName());
    }
  }

  @Test
  void mapCommonHoldings_checkResult() {
    try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
      // SETUP
      final var holdingValue = mock(HoldingValue.class);
      final var holdings = List.of(holdingValue);

      final Holding holding = mock(Holding.class);
      final CommonHoldingsDTO dto = new CommonHoldingsDTO();

      when(holdingValue.getHolding()).thenReturn(holding);
      when(holdingValue.getValue()).thenReturn(TEN);
      when(holding.getUnderlyingHoldings()).thenReturn(null);
      mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.isValid(any())).thenReturn(true);
      mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.initializeCommonHolding(any(), any())).thenReturn(dto);
      mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.mapCommonHoldings(any())).thenCallRealMethod();

      // ACT
      final List<CommonHoldingsDTO> actual = GraphQlMapperUtils.mapCommonHoldings(holdings);

      // VERIFY
      assertNull(actual.get(0).getUnderlyingHoldings());
    }
  }

  @Test
  void mapCommonHoldings_verifyMethodCall_whenContainsUnderlyingHoldings() {
    try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
      // SETUP
      final var holdingValue = mock(HoldingValue.class);
      final var holdings = List.of(holdingValue);

      final Holding holding = mock(Holding.class);
      final CommonHoldingsDTO dto = new CommonHoldingsDTO();

      when(holdingValue.getHolding()).thenReturn(holding);
      when(holdingValue.getValue()).thenReturn(TEN);
      when(holding.getUnderlyingHoldings()).thenReturn(List.of());

      mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.isValid(any())).thenReturn(true);
      mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.initializeCommonHolding(any(), any())).thenReturn(dto);
      mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.mapCommonHoldings(any())).thenCallRealMethod().thenReturn(
          List.of());

      // ACT
      final List<CommonHoldingsDTO> actual = GraphQlMapperUtils.mapCommonHoldings(holdings);

      // VERIFY
      assertNotNull(actual.get(0).getUnderlyingHoldings());
      assertTrue(actual.get(0).getUnderlyingHoldings().isEmpty());
      mockedGraphQlMapperUtils.verify(() -> GraphQlMapperUtils.mapCommonHoldings(any()), Mockito.times(2));
    }
  }

  @Test
  void equityMarketCapitalizationMapper_checkResult2() {
    // SETUP

    final EquityMarketCapitalizationTypeValue eS = mock(EquityMarketCapitalizationTypeValue.class);
    when(eS.getEquityMarketCapitalization()).thenReturn(EquityMarketCapitalizationType.GIANT);
    when(eS.getValue()).thenReturn(TEN);

    final EquityMarketCapitalization e = mock(EquityMarketCapitalization.class);
    when(e.getValues()).thenReturn(List.of(eS));
    when(e.getDataProvider()).thenReturn(DataProvider.MORNINGSTAR);

    // ACT
    final com.fintex.ce.domain.model.EquityMarketCapitalization actual = GraphQlMapperUtils
        .equityMarketCapitalizationMapper(e);

    // VERIFY
    final com.fintex.ce.domain.model.EquityMarketCapitalization expected = new com.fintex.ce.domain.model.EquityMarketCapitalization(
        Map.of(EquityMarketCapitalizationType.GIANT.name(), TEN));
    expected.setProvider(DataProvider.MORNINGSTAR.name());
    assertEquals(expected, actual);
  }

  @Test
  void equityMarketCapitalizationMapper_checkResult_whenEquityMarketCapitalizationIsNull() {
    // SETUP
    final EquityMarketCapitalization e = null;

    // ACT
    final com.fintex.ce.domain.model.EquityMarketCapitalization actual = GraphQlMapperUtils
        .equityMarketCapitalizationMapper(e);

    // VERIFY
    assertNull(actual.getHoldingType());
    assertNull(actual.getRatings());
  }

  @Test
  void equityMarketCapitalizationMapper_checkResult_whenEquityMarketCapitalizationValuesIsEmpty() {
    // SETUP
    final EquityMarketCapitalization e = mock(EquityMarketCapitalization.class);
    when(e.getValues()).thenReturn(List.of());

    // ACT
    final com.fintex.ce.domain.model.EquityMarketCapitalization actual = GraphQlMapperUtils
        .equityMarketCapitalizationMapper(e);

    // VERIFY
    assertNull(actual.getHoldingType());
    assertNull(actual.getRatings());
  }

  @Test
  void assetAllocation_checkResult() {
    // SETUP
    final HoldingType holdingType = HoldingType.CANADA_ETF;
    final AssetAllocation expected = new AssetAllocation(holdingType, Map.of());
    final AssetAllocation actual = GraphQlMapperUtils.assetAllocation(null, holdingType);
    // ACT
    assertEquals(expected, actual);

    // VERIFY
  }

  @Test
  void fixedIncomeBondSectorMapper_checkResult() {
    // SETUP
    final HoldingType holdingType = HoldingType.CANADA_ETF;
    final FixedIncomeBondSecurities expected = new FixedIncomeBondSecurities(holdingType, Map.of(ST_INVESTMENTS.name(),
        ONE));
    expected.setProvider("EAGLE");
    final FixedIncomeSecuritiesAllocation fixedIncomeSectorAllocation = mock(FixedIncomeSecuritiesAllocation.class);
    final NameValue fixedIncomeSectorAllocationTypeNameValue = mock(NameValue.class);

    when(fixedIncomeSectorAllocationTypeNameValue.getName()).thenReturn("ST_INVESTMENTS");
    when(fixedIncomeSectorAllocationTypeNameValue.getValue()).thenReturn(ONE);
    when(fixedIncomeSectorAllocation.getAllocation()).thenReturn(List.of(fixedIncomeSectorAllocationTypeNameValue));
    when(fixedIncomeSectorAllocation.getDataProvider()).thenReturn(DataProvider.EAGLE);
    final FixedIncomeBondSecurities actual = GraphQlMapperUtils.fixedIncomeBondSectorMapper(fixedIncomeSectorAllocation,
        holdingType);
    // ACT
    assertEquals(expected, actual);

    // VERIFY
  }

  @Test
  void countryExposureMapper_checkResult() {
    final Map<String, BigDecimal> actual = GraphQlMapperUtils.countryExposureMapper(null);
    // ACT
    assertEquals(0, actual.size());
    // VERIFY
  }

  @Test
  void countryExposureMapper_checkResult2() {
    final CountryAllocation countryAllocation = mock(CountryAllocation.class);
    when(countryAllocation.getAllocation()).thenReturn(null);
    final Map<String, BigDecimal> actual = GraphQlMapperUtils.countryExposureMapper(countryAllocation);
    // ACT
    assertEquals(0, actual.size());
    // VERIFY
  }

  @Test
  void countryExposureMapper_checkResult3() {
    // SETUP
    final CountryAllocation c = mock(CountryAllocation.class);
    final CountryValue cValue = mock(CountryValue.class);
    when(cValue.getValue()).thenReturn(BigDecimal.TEN);
    when(cValue.getIsoCode()).thenReturn("TEST");
    when(c.getAllocation()).thenReturn(List.of(cValue));

    // ACT
    final Map<String, BigDecimal> actual = GraphQlMapperUtils.countryExposureMapper(c);

    // VERIFY
    assertEquals(Map.of("TEST", BigDecimal.TEN), actual);
  }

  @Test
  void countryExposureMapper_checkResult4() {
    // SETUP
    final CountryAllocation m = mock(CountryAllocation.class);
    final CountryValue cValue = mock(CountryValue.class);
    when(cValue.getValue()).thenReturn(null);
    when(cValue.getIsoCode()).thenReturn("TEST");
    when(m.getAllocation()).thenReturn(List.of(cValue));

    // ACT
    final Map<String, BigDecimal> actual = GraphQlMapperUtils.countryExposureMapper(m);

    // VERIFY
    assertEquals(Map.of("TEST", BigDecimal.ZERO), actual);
  }

  @Test
  void creditQualityMapper_checkResult() {
    // SETUP

    final CreditQualityRatings c = mock(CreditQualityRatings.class);

    final Map<String, BigDecimal> expected = GraphQlMapperUtils.creditQualityMapper(c);
    // ACT

    // VERIFY
    assertEquals(expected, Map.of());
  }

  @Test
  void creditQualityMapper_checkResult3() {
    // SETUP

    final CreditQualityRatings c = mock(CreditQualityRatings.class);

    final CreditQualityRatingTypeValue creditE = mock(CreditQualityRatingTypeValue.class);
    when(creditE.getRating()).thenReturn("");

    when(c.getRatings()).thenReturn(List.of(creditE));

    final Map<String, BigDecimal> expected = GraphQlMapperUtils.creditQualityMapper(c);
    // ACT

    // VERIFY
    assertEquals(expected, Map.of());
  }

  @Test
  void creditQualityMapper_checkResult4() {
    // SETUP

    final CreditQualityRatings c = mock(CreditQualityRatings.class);

    final CreditQualityRatingTypeValue creditE = mock(CreditQualityRatingTypeValue.class);
    when(creditE.getRating()).thenReturn("F");

    when(c.getRatings()).thenReturn(List.of(creditE));

    final Map<String, BigDecimal> expected = GraphQlMapperUtils.creditQualityMapper(c);
    // ACT

    // VERIFY
    assertEquals(expected, Map.of("F", BigDecimal.ZERO));
  }

  @Test
  void creditQualityMapper_checkResult5() {
    // SETUP

    final CreditQualityRatings c = mock(CreditQualityRatings.class);

    final CreditQualityRatingTypeValue creditE = mock(CreditQualityRatingTypeValue.class);
    when(creditE.getRating()).thenReturn("F");
    when(creditE.getValue()).thenReturn(BigDecimal.TEN);

    when(c.getRatings()).thenReturn(List.of(creditE));

    final Map<String, BigDecimal> expected = GraphQlMapperUtils.creditQualityMapper(c);
    // ACT

    // VERIFY
    assertEquals(expected, Map.of("F", BigDecimal.TEN));
  }

  @Test
  void parseDate_checkResult() {
    // SETUP
    final var date = mock(DateValue.class);
    final var expectedDate = LocalDate.of(2020, 1, 31);

    when(date.getDate()).thenReturn("2020-01");

    // ACT
    final LocalDate actual = GraphQlMapperUtils.parseDate(date);

    // VERIFY
    assertEquals(expectedDate, actual);
  }

  @Test
  void monthlyReturns_checkResult() {
    try (var mockedGraphQlMapperUtils = Mockito.mockStatic(GraphQlMapperUtils.class)) {
      // SETUP
      final var monthlyReturns = mock(MonthlyReturns.class);
      final var currency = "CAD";
      final var holding = mock(com.fintex.ce.domain.model.holding.Holding.class);
      final var holdingType = HoldingType.CASH;
      final var expectedDate = LocalDate.of(2020, 1, 1);
      final var expectedBigDecimal = mock(BigDecimal.class);
      final var dateValue = mock(DateValue.class);

      when(holding.getType()).thenReturn(holdingType);
      when(monthlyReturns.getReturns()).thenReturn(List.of(dateValue));
      when(dateValue.getValue()).thenReturn(expectedBigDecimal);

      mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.parseDate(any())).thenReturn(expectedDate);
      mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.monthlyReturns(any(), any(), any())).thenCallRealMethod();
      mockedGraphQlMapperUtils.when(() -> GraphQlMapperUtils.toDomainHoldingType(any())).thenReturn(HoldingType.CASH);

      // ACT
      final com.fintex.ce.domain.model.MonthlyReturns response = GraphQlMapperUtils.monthlyReturns(monthlyReturns,
          currency, holding);

      // VERIFY
      assertEquals(currency, response.getCurrency());
      assertEquals(holdingType, response.getHoldingType());
      assertEquals(Map.of(expectedDate, expectedBigDecimal), response.getReturns());
    }
  }

  @Test
  void mapExternalIdentifiers_checkResult() {
    // SETUP
    final var externalIdentifier = mock(ExternalIdentifierTypeValue.class);
    final var expectedType = ExternalIdentifierType.TICKER;
    final var expectedValue = "value";

    when(externalIdentifier.getType()).thenReturn(expectedType);
    when(externalIdentifier.getValue()).thenReturn(expectedValue);

    // ACT
    final ExternalIdentifiersDTO externalIdentifiersDTO = GraphQlMapperUtils.mapExternalIdentifiers().apply(
        externalIdentifier);

    // VERIFY
    assertEquals(expectedType.name(), externalIdentifiersDTO.getIdentifierType().name());
    assertEquals(expectedValue, externalIdentifiersDTO.getValue());
  }

}
