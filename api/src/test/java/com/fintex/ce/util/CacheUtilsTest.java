package com.fintex.ce.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static com.fintex.ce.constant.CacheCategory.CANADA_MUTUAL_FUNDS;
import static com.fintex.ce.constant.CacheNameEntity.MER;
import static com.fintex.ce.domain.enumeration.DataProvider.EAGLE;
import static com.fintex.ce.domain.enumeration.DataProvider.MORNINGSTAR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CacheUtilsTest {

  @Test
  void buildCacheName_checkResult() {
    // SETUP
    final String expected = MER + "_" + CANADA_MUTUAL_FUNDS;

    // ACT
    final String actual = CacheUtils.buildCacheName(MER, CANADA_MUTUAL_FUNDS);

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void buildCacheName_whenPrefixIsNull() {
    assertThrows(NullPointerException.class, () -> CacheUtils.buildCacheName(null, CANADA_MUTUAL_FUNDS));
  }

  @Test
  void buildCacheName_whenCategoryIsNull() {
    assertThrows(NullPointerException.class, () -> CacheUtils.buildCacheName(MER, null));
  }

  @Test
  void buildIdBasedOnProviders_checkResult() {
    // SETUP
    final String expected = EAGLE.ordinal() + "";

    // ACT
    final String actual = CacheUtils.buildIdBasedOnProviders(List.of(EAGLE));

    // VERIFY
    assertEquals(expected, actual);
  }

  @Test
  void buildIdBasedOnProviders_checkResult2() {
    // SETUP
    final String expected = EAGLE.ordinal() + "_" + MORNINGSTAR.ordinal();

    // ACT
    final String actual = CacheUtils.buildIdBasedOnProviders(List.of(EAGLE, MORNINGSTAR));

    // VERIFY
    assertEquals(expected, actual);
  }

}