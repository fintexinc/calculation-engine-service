package com.fintex.ce.adapter.cache;

import com.fintex.ce.adapter.cache.TBillsCacheStorage;
import com.fintex.ce.adapter.cache.entity.RTBills;
import com.fintex.ce.adapter.cache.repository.TBillsRepository;
import com.fintex.ce.domain.enumeration.Currency;
import com.fintex.smclient.service.CommonEndpointsComponent;
import com.fintex.ce.util.TestConstants;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class TBillsCacheStorageTest {

  @Test
  void mapToFdsCurrency_checkResult_whenCurrencyCad() {
    // SETUP
    final var sut = new TBillsCacheStorage(null, null);

    // ACT
    final com.fintex.smclient.enumeration.Currency actual = sut.mapToFdsCurrency(Currency.CAD);

    // VERIFY
    assertEquals(com.fintex.smclient.enumeration.Currency.CAD, actual);
  }

  @Test
  void mapToFdsCurrency_checkResult_whenCurrencyUsd() {
    // SETUP
    final var sut = new TBillsCacheStorage(null, null);

    // ACT
    final com.fintex.smclient.enumeration.Currency actual = sut.mapToFdsCurrency(Currency.USD);

    // VERIFY
    assertEquals(com.fintex.smclient.enumeration.Currency.USD, actual);
  }

  @Test
  void loadTBillsFor_verifyFindAllByCurrency() {
    // SETUP
    final TBillsRepository tBillsRepository = mock(TBillsRepository.class);
    final var sut = mock(TBillsCacheStorage.class, withSettings()
        .useConstructor(mock(CommonEndpointsComponent.class), tBillsRepository));

    final Collection<RTBills> tBills = List.of(mock(RTBills.class));
    when(tBillsRepository.findAllByCurrency(any())).thenReturn(tBills);

    doCallRealMethod().when(sut).loadTBillsFor(any());

    // ACT
    final TreeMap<LocalDate, BigDecimal> actual = sut.loadTBillsFor(Currency.USD);

    // VERIFY
    verify(tBillsRepository).findAllByCurrency(Currency.USD);
  }

  @Test
  void loadTBillsFor_verifyMapToFdsCurrency_whenRepositoryTBillsIsEmpty() {
    // SETUP
    final TBillsRepository tBillsRepository = mock(TBillsRepository.class);
    final CommonEndpointsComponent commonEndpointsComponent = mock(CommonEndpointsComponent.class);
    final var sut = mock(TBillsCacheStorage.class, withSettings()
        .useConstructor(commonEndpointsComponent, tBillsRepository));

    when(tBillsRepository.findAllByCurrency(any())).thenReturn(List.of());
    when(commonEndpointsComponent.loadTreasuryBillsBy(any())).thenReturn(mock(TreeMap.class));

    doCallRealMethod().when(sut).loadTBillsFor(any());

    // ACT
    final TreeMap<LocalDate, BigDecimal> actual = sut.loadTBillsFor(Currency.USD);

    // VERIFY
    verify(sut).mapToFdsCurrency(Currency.USD);
  }

  @Test
  void loadTBillsFor_verifyLoadTBillsFor_whenRepositoryTBillsIsEmpty() {
    // SETUP
    final TBillsRepository tBillsRepository = mock(TBillsRepository.class);
    final CommonEndpointsComponent commonEndpointsComponent = mock(CommonEndpointsComponent.class);
    final var sut = mock(TBillsCacheStorage.class, withSettings()
        .useConstructor(commonEndpointsComponent, tBillsRepository));

    when(tBillsRepository.findAllByCurrency(any())).thenReturn(List.of());
    when(sut.mapToFdsCurrency(any())).thenReturn(com.fintex.smclient.enumeration.Currency.USD);
    when(commonEndpointsComponent.loadTreasuryBillsBy(any())).thenReturn(mock(TreeMap.class));

    doCallRealMethod().when(sut).loadTBillsFor(any());

    // ACT
    final TreeMap<LocalDate, BigDecimal> actual = sut.loadTBillsFor(Currency.USD);

    // VERIFY
    verify(commonEndpointsComponent).loadTreasuryBillsBy(com.fintex.smclient.enumeration.Currency.USD);
  }

  @Test
  void loadTBillsFor_verifySave_whenRepositoryTBillsIsEmpty() {
    // SETUP
    final TBillsRepository tBillsRepository = mock(TBillsRepository.class);
    final CommonEndpointsComponent commonEndpointsComponent = mock(CommonEndpointsComponent.class);
    final var sut = mock(TBillsCacheStorage.class, withSettings()
        .useConstructor(commonEndpointsComponent, tBillsRepository));

    when(tBillsRepository.findAllByCurrency(any())).thenReturn(List.of());
    when(sut.mapToFdsCurrency(any())).thenReturn(com.fintex.smclient.enumeration.Currency.USD);

    final TreeMap tBillsMonthlyReturns = mock(TreeMap.class);
    when(commonEndpointsComponent.loadTreasuryBillsBy(any())).thenReturn(tBillsMonthlyReturns);

    doCallRealMethod().when(sut).loadTBillsFor(any());

    // ACT
    final TreeMap<LocalDate, BigDecimal> actual = sut.loadTBillsFor(Currency.USD);

    // VERIFY
    final RTBills expectedRTBills = new RTBills(Currency.USD, tBillsMonthlyReturns);
    verify(tBillsRepository).save(expectedRTBills);
  }

  @Test
  void loadTBillsFor_checkResult_whenRepositoryTBillsIsEmpty() {
    // SETUP
    final TBillsRepository tBillsRepository = mock(TBillsRepository.class);
    final CommonEndpointsComponent commonEndpointsComponent = mock(CommonEndpointsComponent.class);
    final var sut = mock(TBillsCacheStorage.class, withSettings()
        .useConstructor(commonEndpointsComponent, tBillsRepository));

    when(tBillsRepository.findAllByCurrency(any())).thenReturn(List.of());
    when(sut.mapToFdsCurrency(any())).thenReturn(com.fintex.smclient.enumeration.Currency.USD);

    final TreeMap tBillsMonthlyReturns = mock(TreeMap.class);
    when(commonEndpointsComponent.loadTreasuryBillsBy(any())).thenReturn(tBillsMonthlyReturns);

    doCallRealMethod().when(sut).loadTBillsFor(any());

    // ACT
    final TreeMap<LocalDate, BigDecimal> actual = sut.loadTBillsFor(Currency.USD);

    // VERIFY
    assertSame(tBillsMonthlyReturns, actual);
  }

  @Test
  void loadTBillsFor_checkResult_whenRepositoryTBillsIsNotEmpty() {
    // SETUP
    final TBillsRepository tBillsRepository = mock(TBillsRepository.class);
    final CommonEndpointsComponent commonEndpointsComponent = mock(CommonEndpointsComponent.class);
    final var sut = mock(TBillsCacheStorage.class, withSettings()
        .useConstructor(commonEndpointsComponent, tBillsRepository));

    final var tBillsMonthlyReturns = new TreeMap<>(Map.of(TestConstants.LOCAL_DATE_NOW, BigDecimal.TEN));
    final var tBills = new RTBills(Currency.USD, tBillsMonthlyReturns);

    final var tBillsList = List.of(tBills);
    when(tBillsRepository.findAllByCurrency(any())).thenReturn(tBillsList);

    doCallRealMethod().when(sut).loadTBillsFor(any());

    // ACT
    final TreeMap<LocalDate, BigDecimal> actual = sut.loadTBillsFor(Currency.USD);

    // VERIFY
    assertEquals(tBillsMonthlyReturns, actual);
  }

  @Test
  void loadTBillsFor_verifyNoSave_whenLoadTBillsForThrowsError() {
    // SETUP
    final TBillsRepository tBillsRepository = mock(TBillsRepository.class);
    final CommonEndpointsComponent commonEndpointsComponent = mock(CommonEndpointsComponent.class);
    final var sut = mock(TBillsCacheStorage.class, withSettings()
        .useConstructor(commonEndpointsComponent, tBillsRepository));

    when(tBillsRepository.findAllByCurrency(any())).thenReturn(List.of());
    when(commonEndpointsComponent.loadTreasuryBillsBy(any())).thenThrow(RuntimeException.class);

    doCallRealMethod().when(sut).loadTBillsFor(any());

    // ACT
    assertThrows(RuntimeException.class, () -> sut.loadTBillsFor(Currency.USD));

    // VERIFY
    verify(tBillsRepository).findAllByCurrency(Currency.USD);
    verifyNoMoreInteractions(tBillsRepository);
  }

}