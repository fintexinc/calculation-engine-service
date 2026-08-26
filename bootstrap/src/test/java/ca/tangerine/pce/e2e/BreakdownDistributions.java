package ca.tangerine.pce.e2e;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import lombok.experimental.UtilityClass;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Assertions shared by the breakdown metrics' e2e tests. Every one of those metrics answers with the same shape — one
 * value per bucket of a taxonomy enum — so what a scenario has to state is the whole distribution, not the handful of
 * buckets it happens to populate.
 */
@UtilityClass
final class BreakdownDistributions {

  /**
   * Asserts the complete payload: every bucket of the taxonomy is present, the ones named in {@code expected} carry
   * exactly that value, every other one is exactly zero.
   *
   * <p>
   * Exact rather than within a tolerance, and zeros asserted rather than ignored. A tolerance would accept a rounding
   * regression at the fourth decimal while the client renders two, and, applied to an expected zero, would accept a
   * bucket that quietly picked up a fraction of a percent of the portfolio — which is exactly the class of defect these
   * metrics have produced before. Scenarios whose weights do not terminate in base 10 cannot use this and say so.
   */
  static <T extends Enum<T>> void assertDistribution(Map<T, BigDecimal> actual, Class<T> bucketType,
      Map<T, String> expected) {
    assertThat(actual).containsOnlyKeys(bucketType.getEnumConstants());
    for (T bucket : bucketType.getEnumConstants()) {
      assertThat(actual.get(bucket)).as("bucket %s", bucket)
          .isEqualByComparingTo(new BigDecimal(expected.getOrDefault(bucket, "0")));
    }
  }

  /**
   * The reported distribution has to describe the whole of whatever the metric measures, so the buckets add up to one.
   * Asserted separately from {@link #assertDistribution} because it is a different claim: the values can each be right
   * and still fail to account for a holding that dropped out of the denominator.
   */
  static <T extends Enum<T>> void assertTotalsToOne(Map<T, BigDecimal> actual) {
    assertThat(total(actual)).as("the buckets must account for the whole of what the metric measures")
        .isEqualByComparingTo(BigDecimal.ONE);
  }

  static <T extends Enum<T>> BigDecimal total(Map<T, BigDecimal> actual) {
    return actual.values().stream()
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}
