package com.fintex.ce.adapter.cache;

import com.fintex.ce.adapter.cache.BusinessCountryCacheStorage;
import com.fintex.ce.adapter.cache.repository.businesscountry.BusinessCountryRepository;
import com.fintex.ce.adapter.cache.statistic.CacheStatisticService;
import com.fintex.ce.port.output.sm.SecurityDataPort;
import com.fintex.ce.domain.enumeration.Country;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.BusinessCountry;
import com.fintex.ce.domain.model.ParamHolderDTO;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.holding.StockHolding;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.util.FilterUtils;
import com.fintex.ce.util.validation.DataProviderRequestHandlingValidator;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static com.fintex.ce.domain.enumeration.Country.CAN;
import static com.fintex.ce.domain.enumeration.Country.OTHER;
import static com.fintex.ce.domain.enumeration.Country.USA;
import static com.fintex.ce.domain.enumeration.ExceptionCode.WRN_BCC_001;
import static com.fintex.ce.util.FilterUtils.STOCK_PREDICATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class BusinessCountryCacheStorageTest {

  @Test
  void loadPublic_verifyLoad() {
    // SETUP
    final var sut = mock(BusinessCountryCacheStorage.class);

    final List<Holding> holdings = List.of(new Holding());
    final List<DataProvider> providers = mock(List.class);

    doCallRealMethod().when(sut).load(anyList(), anyList(), anyList(), any(ParamHolderDTO.class));
    // ACT
    sut.load(holdings, providers, List.of(), new ParamHolderDTO());

    // VERIFY
    verify(sut).load(holdings, providers, false);
  }

  @Test
  void load_verifyDataProviderCheckValidation() {
    try (var mockedDataProviderRequestHandlingValidator = Mockito.mockStatic(
        DataProviderRequestHandlingValidator.class)) {
      // SETUP
      final var sut = mock(BusinessCountryCacheStorage.class);

      final List<Holding> holdings = List.of(new Holding());
      final List<DataProvider> providers = mock(List.class);
      final var needToCheckDataProviders = true;
      final var response = mock(Map.class);
      final var responseValues = mock(Collection.class);

      when(response.values()).thenReturn(responseValues);
      when(sut.loadForBenchOfStock(anyList(), anyList())).thenReturn(response);

      doCallRealMethod().when(sut).load(anyList(), anyList(), anyBoolean());
      // ACT
      sut.load(holdings, providers, needToCheckDataProviders);

      // VERIFY
      mockedDataProviderRequestHandlingValidator.verify(
          () -> DataProviderRequestHandlingValidator.dataProviderCheckValidation(providers, responseValues, sut
              .getBusinessCountrySetValueFunction()));
    }
  }

  @Test
  void load_verifyLoadForBenchOfStock() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final var sut = mock(BusinessCountryCacheStorage.class);

      final List<Holding> holdings = List.of(new Holding());
      final List<DataProvider> providers = mock(List.class);
      final var needToCheckDataProviders = false;
      final List<StockHolding> filtered = mock(List.class);

      mockedFilterUtils.when(() -> FilterUtils.filterHoldings(holdings, STOCK_PREDICATE)).thenReturn(filtered);

      doCallRealMethod().when(sut).load(anyList(), anyList(), anyBoolean());
      // ACT
      sut.load(holdings, providers, needToCheckDataProviders);

      // VERIFY
      verify(sut).loadForBenchOfStock(filtered, List.of());
    }
  }

  @Test
  void load_verifyFilters() {
    try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
      // SETUP
      final var sut = mock(BusinessCountryCacheStorage.class);

      final List<Holding> holdings = List.of(new Holding());
      final List<DataProvider> providers = mock(List.class);
      final var needToCheckDataProviders = false;

      doCallRealMethod().when(sut).load(anyList(), anyList(), anyBoolean());
      // ACT
      sut.load(holdings, providers, needToCheckDataProviders);

      // VERIFY
      mockedFilterUtils.verify(() -> FilterUtils.filterHoldings(eq(holdings), eq(STOCK_PREDICATE)));
    }
  }

  @Test
  void getCountryForHolding_checkResult() {
    // SETUP
    final BusinessCountryCacheStorage b = mock(BusinessCountryCacheStorage.class);

    final BusinessCountry rb = mock(BusinessCountry.class);
    when(rb.getValue()).thenReturn(CAN.name());

    doCallRealMethod().when(b).getCountryForHolding(any(), any(), anyList());
    // ACT
    final Country country = b.getCountryForHolding(new StockHolding(), rb, mock(List.class));

    // VERIFY
    assertEquals(CAN, country);
  }

  @Test
  void getCountryForHolding_checkResult2() {
    // SETUP
    final BusinessCountryCacheStorage b = mock(BusinessCountryCacheStorage.class);

    final BusinessCountry rb = mock(BusinessCountry.class);
    when(rb.getValue()).thenReturn(USA.name());

    doCallRealMethod().when(b).getCountryForHolding(any(), any(), anyList());
    // ACT
    final Country country = b.getCountryForHolding(new StockHolding(), rb, mock(List.class));

    // VERIFY
    assertEquals(USA, country);
  }

  @Test
  void getCountryForHolding_checkResult3() {
    // SETUP
    final BusinessCountryCacheStorage b = mock(BusinessCountryCacheStorage.class);

    final BusinessCountry rb = mock(BusinessCountry.class);
    when(rb.getValue()).thenReturn("UKR");

    doCallRealMethod().when(b).getCountryForHolding(any(), any(), anyList());
    // ACT
    final Country e = b.getCountryForHolding(new StockHolding(), rb, mock(List.class));

    assertEquals(OTHER, e);
  }

  @Test
  void getCountryForHolding_checkResult4() {
    // SETUP
    final var sut = mock(BusinessCountryCacheStorage.class);
    final var warnings = new ArrayList<Warning>();

    final var businessCountry = mock(BusinessCountry.class);
    when(businessCountry.getValue()).thenReturn("");

    doCallRealMethod().when(sut).getCountryForHolding(any(), any(), anyList());
    // ACT
    sut.getCountryForHolding(new StockHolding(), businessCountry, warnings);

    assertEquals(1, warnings.size());
    assertEquals(WRN_BCC_001.name(), warnings.get(0).getCode());
    assertEquals(WRN_BCC_001.getMessage(), warnings.get(0).getMessage());
  }

  @Test
  void loadBusinessCountries_verifyLoad() {
    // SETUP
    final BusinessCountryCacheStorage b = mock(BusinessCountryCacheStorage.class);

    final List<Holding> holdings = List.of(new Holding());
    final List<DataProvider> providers = mock(List.class);
    final var needToCheckDataProviders = false;

    when(b.load(anyList(), anyList(), anyList(), any(ParamHolderDTO.class))).thenReturn(Map.of());

    doCallRealMethod().when(b).loadBusinessCountries(any(), any(), anyBoolean(), anyList());
    // ACT
    b.loadBusinessCountries(holdings, providers, needToCheckDataProviders, mock(List.class));

    // VERIFY
    verify(b).load(holdings, providers, needToCheckDataProviders);
  }

  @Test
  void loadBusinessCountries_checkResult() {
    // SETUP
    final var sut = mock(BusinessCountryCacheStorage.class);

    final BusinessCountry rb = mock(BusinessCountry.class);
    when(rb.getValue()).thenReturn(USA.name());
    final List<DataProvider> providers = mock(List.class);
    final var needToCheckDataProviders = false;
    final Holding h = new Holding();
    final List<Holding> holdings = List.of(h);

    when(sut.load(anyList(), anyList(), anyBoolean())).thenReturn(Map.of(h, rb));

    doCallRealMethod().when(sut).getCountryForHolding(any(), any(), anyList());
    doCallRealMethod().when(sut).loadBusinessCountries(anyList(), anyList(), anyBoolean(), anyList());
    // ACT
    final Map<Holding, Country> actual = sut.loadBusinessCountries(holdings, providers, needToCheckDataProviders, mock(
        List.class));

    // VERIFY
    assertEquals(Map.of(h, USA), actual);
  }

  @Test
  void getBusinessCountrySetValueFunction_checkResult() {
    // SETUP
    final var queryRepository = mock(SecurityDataPort.class);
    final var businessCountryRepository = mock(BusinessCountryRepository.class);
    final var cacheStatisticService = mock(CacheStatisticService.class);

    final var sut = mock(BusinessCountryCacheStorage.class, withSettings()
        .useConstructor(queryRepository, null, businessCountryRepository, cacheStatisticService));

    final var businessCountry = mock(BusinessCountry.class);
    final var expectedValue = "value";

    doCallRealMethod().when(sut).getBusinessCountrySetValueFunction();

    // ACT
    final BiFunction<BusinessCountry, String, BusinessCountry> actualFunction = sut
        .getBusinessCountrySetValueFunction();
    actualFunction.apply(businessCountry, expectedValue);

    // VERIFY
    verify(sut).getBusinessCountrySetValueFunction();
    verify(businessCountry).setValue(expectedValue);
  }

}