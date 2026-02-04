package com.fintex.ce.application.service;

import com.fintex.ce.adapter.cache.repository.FxRatesRepository;
import com.fintex.ce.adapter.cache.entity.RBusinessCountry;
import com.fintex.ce.adapter.cache.repository.businesscountry.BusinessCountryRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class ExceptionHandlingServiceImplTest {

  @Test
  void removeFxRatesFromRedisCache_verifyFxRateRepositoryDeleteAll() {
    // SETUP
    final FxRatesRepository fxRatesRepository = mock(FxRatesRepository.class);
    final var sut = mock(ExceptionHandlingServiceImpl.class,
        withSettings().useConstructor(mock(List.class), fxRatesRepository));

    doCallRealMethod().when(sut).removeFxRatesFromRedisCache();
    // ACT
    sut.removeFxRatesFromRedisCache();

    // VERIFY
    verify(fxRatesRepository).deleteAll();
  }

  @Test
  void removeDataFromRepositoriesByHoldingId_verifyFindAllByHoldingId() {
    // SETUP
    final String id = "id";
    final BusinessCountryRepository businessCountryRepository = mock(BusinessCountryRepository.class);
    final FxRatesRepository fxRatesRepository = mock(FxRatesRepository.class);
    final ExceptionHandlingServiceImpl sut = mock(ExceptionHandlingServiceImpl.class,
        withSettings().useConstructor(List.of(businessCountryRepository), fxRatesRepository));
    doCallRealMethod().when(sut).removeDataFromRepositoriesByHoldingId(any());

    // ACT
    sut.removeDataFromRepositoriesByHoldingId(id);

    // VERIFY
    verify(businessCountryRepository).findAllByHoldingId(id);
  }

  @Test
  void removeDataFromRepositoriesByHoldingId_verifyDeleteById() {
    // SETUP
    final String id = "id";
    final BusinessCountryRepository businessCountryRepository = mock(BusinessCountryRepository.class);
    final RBusinessCountry rBusinessCountry = mock(RBusinessCountry.class);
    when(rBusinessCountry.getId()).thenReturn(id);
    when(businessCountryRepository.findAllByHoldingId(id)).thenReturn(List.of(rBusinessCountry));
    final FxRatesRepository fxRatesRepository = mock(FxRatesRepository.class);
    final ExceptionHandlingServiceImpl sut = mock(ExceptionHandlingServiceImpl.class,
        withSettings().useConstructor(List.of(businessCountryRepository), fxRatesRepository));
    doCallRealMethod().when(sut).removeDataFromRepositoriesByHoldingId(any());

    // ACT
    sut.removeDataFromRepositoriesByHoldingId(id);

    // VERIFY
    verify(businessCountryRepository).deleteById(id);
  }

}