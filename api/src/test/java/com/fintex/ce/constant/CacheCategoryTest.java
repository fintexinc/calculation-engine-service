package com.fintex.ce.constant;

import com.fintex.ce.constant.CacheCategory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CacheCategoryTest {

  @Test
  void isMutualFund_checkResult() {
    // SETUP
    assertTrue(CacheCategory.CANADA_MUTUAL_FUNDS.isMutualFund());
  }

  @Test
  void isEtf_checkResult() {
    // SETUP
    assertTrue(CacheCategory.ETF.isEtf());
    assertTrue(CacheCategory.US_ETF.isEtf());
    assertTrue(CacheCategory.CANADA_ETF.isEtf());
  }

  @Test
  void isStock_checkResult() {
    // SETUP
    assertTrue(CacheCategory.STOCKS.isStock());
    assertTrue(CacheCategory.CANADA_STOCKS.isStock());
    assertTrue(CacheCategory.US_STOCKS.isStock());
  }

  @Test
  void isBenchmark_checkResult() {
    // SETUP
    assertTrue(CacheCategory.BENCHMARK_INDEXES.isBenchmark());
  }

}