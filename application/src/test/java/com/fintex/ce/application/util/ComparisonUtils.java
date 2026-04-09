package com.fintex.ce.application.util;

import org.junit.jupiter.api.Assertions;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class ComparisonUtils {
  private static final double DIFF_8x = 0.000_000_01;

  private ComparisonUtils() {
  }

  public static <K, V> void compareMaps(final Map<K, V> expected, final Map<K, V> actual) {
    if (expected == null && actual == null) {
      return;
    }
    Assertions.assertNotNull(actual);
    Assertions.assertNotNull(expected);
    Assertions.assertEquals(expected.size(), actual.size(),
        "Number of elements in actual/expected maps are different");

    actual.forEach((actualKey, actualValue) -> {
      Assertions.assertTrue(expected.containsKey(actualKey),
          String.format("Key '%s' should not be present in the actual response", actualKey));
      final V expectedValue = expected.get(actualKey);
      if (expectedValue instanceof BigDecimal && actualValue instanceof BigDecimal) {
        compareWithin8xRange((BigDecimal) expectedValue, (BigDecimal) actualValue, String.format(
            "Values are different: expected value is %s, but actual is %s", expectedValue, actualValue));
      } else {
        Assertions.assertEquals(expectedValue, actualValue);
      }
    });
  }

  public static void compareWithin8xRange(BigDecimal expected, BigDecimal actual, String message) {
    if (expected == null && actual == null) {
      return;
    }
    Assertions.assertNotNull(expected);
    Assertions.assertNotNull(actual);
    Assertions.assertTrue(Math.abs(actual.doubleValue() - expected.doubleValue()) <= DIFF_8x, message);
  }

  public static <K> void compareCollections(final Collection<K> expectedData, final Collection<K> actualData) {
    if (expectedData == null && actualData == null) {
      return;
    }

    Assertions.assertNotNull(actualData);
    Assertions.assertNotNull(expectedData);
    Assertions.assertEquals(expectedData.size(), actualData.size(),
        "Number of elements in actualData/expectedData collections are different");

    for (K actual : actualData) {
      final Optional<K> expected = expectedData.stream().filter(k -> {
        if (actual instanceof BigDecimal && k instanceof BigDecimal) {
          return ((BigDecimal) k).compareTo((BigDecimal) actual) == 0;
        }
        return Objects.equals(k, actual);
      }).findFirst();
      Assertions.assertTrue(expected.isPresent());
      if (expected.get() instanceof BigDecimal && actual instanceof BigDecimal) {
        compareWithin8xRange((BigDecimal) expected.get(), (BigDecimal) actual, String.format(
            "Values are different: expected value is %s, but actual is %s", expected.get(), actual));
      } else {
        Assertions.assertEquals(expected.get(), actual);
      }
    }
  }

}