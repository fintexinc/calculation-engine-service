package ca.tangerine.pce.util;

import org.apache.commons.math3.util.Precision;
import org.junit.jupiter.api.Assertions;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * Assertions over maps and collections whose values may be {@link BigDecimal}, compared within a fixed tolerance so a
 * difference in scale or a rounding residue does not fail a test that {@code equals} would.
 */
@Slf4j
@UtilityClass
public class ComparisonUtils {

  private static final double DIFF_8X = 0.000_000_01;

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
      log.info("key: {}, expectedValue: {}, actualValue: {}", actualKey, expectedValue, actualValue);
      compareValues(expectedValue, actualValue);
    });
  }

  public static void compareWithin8xRange(final BigDecimal expected, final BigDecimal actual, final String message) {
    if (expected == null && actual == null) {
      return;
    }
    Assertions.assertNotNull(expected);
    Assertions.assertNotNull(actual);
    Assertions.assertTrue(Precision.equals(actual.doubleValue(), expected.doubleValue(), DIFF_8X), message);
  }

  public static <K> void compareCollections(final Collection<K> expectedData, final Collection<K> actualData) {
    if (expectedData == null && actualData == null) {
      return;
    }

    Assertions.assertNotNull(actualData);
    Assertions.assertNotNull(expectedData);
    Assertions.assertEquals(expectedData.size(), actualData.size(),
        "Number of elements in actualData/expectedData collections are different");

    for (final K actual : actualData) {
      final K expected = expectedData.stream()
          .filter(candidate -> matches(candidate, actual))
          .findFirst()
          .orElseGet(() -> Assertions.fail(String.format("No expected element matches the actual one: %s", actual)));
      compareValues(expected, actual);
    }
  }

  private static <K> boolean matches(final K candidate, final K actual) {
    if (candidate instanceof BigDecimal candidateValue && actual instanceof BigDecimal actualValue) {
      return candidateValue.compareTo(actualValue) == 0;
    }
    return Objects.equals(candidate, actual);
  }

  private static <V> void compareValues(final V expected, final V actual) {
    if (expected instanceof BigDecimal expectedValue && actual instanceof BigDecimal actualValue) {
      compareWithin8xRange(expectedValue, actualValue,
          String.format("Values are different: expected value is %s, but actual is %s", expected, actual));
    } else {
      Assertions.assertEquals(expected, actual);
    }
  }
}
