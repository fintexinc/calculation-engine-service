package com.fintex.ce.adapter.webclient.observability;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

class RollingCallStatisticsTest {

  private static final long ONE_MINUTE_NANOS = Duration.ofMinutes(1).toNanos();
  private static final long ONE_MILLI_NANOS = Duration.ofMillis(1).toNanos();

  private final AtomicLong clock = new AtomicLong();
  private final RollingCallStatistics statistics = new RollingCallStatistics(clock::get);

  @Test
  void shouldReportZero_whenNoCallsRecorded() {
    assertThat(statistics.failureRatio()).isZero();
    assertThat(statistics.meanDurationMillis()).isZero();
  }

  @Test
  void shouldReportRatioAndMean_whenCallsRecordedInSameBucket() {
    statistics.record(10 * ONE_MILLI_NANOS, false);
    statistics.record(20 * ONE_MILLI_NANOS, false);
    statistics.record(30 * ONE_MILLI_NANOS, true);
    statistics.record(40 * ONE_MILLI_NANOS, true);

    assertThat(statistics.failureRatio()).isEqualTo(0.5);
    assertThat(statistics.meanDurationMillis()).isEqualTo(25.0);
  }

  @Test
  void shouldKeepCallsWithinWindow_whenWindowPartiallyElapsed() {
    statistics.record(10 * ONE_MILLI_NANOS, true);
    advanceMinutes(2);
    statistics.record(30 * ONE_MILLI_NANOS, false);

    assertThat(statistics.failureRatio()).isEqualTo(0.5);
    assertThat(statistics.meanDurationMillis()).isEqualTo(20.0);
  }

  @Test
  void shouldDropOldestCalls_whenTheyFallOutOfWindow() {
    statistics.record(100 * ONE_MILLI_NANOS, true);
    advanceMinutes(RollingCallStatistics.BUCKETS - 1L);
    statistics.record(20 * ONE_MILLI_NANOS, false);

    assertThat(statistics.failureRatio()).isEqualTo(0.5);

    advanceMinutes(1);

    assertThat(statistics.failureRatio()).isZero();
    assertThat(statistics.meanDurationMillis()).isEqualTo(20.0);
  }

  @Test
  void shouldForgetEverything_whenWholeWindowElapsed() {
    statistics.record(10 * ONE_MILLI_NANOS, true);
    statistics.record(10 * ONE_MILLI_NANOS, true);
    advanceMinutes(RollingCallStatistics.BUCKETS);

    assertThat(statistics.failureRatio()).isZero();
    assertThat(statistics.meanDurationMillis()).isZero();
  }

  @Test
  void shouldForgetEverything_whenIdleFarLongerThanWindow() {
    statistics.record(10 * ONE_MILLI_NANOS, true);
    advanceMinutes(RollingCallStatistics.BUCKETS * 20L);

    assertThat(statistics.failureRatio()).isZero();

    statistics.record(50 * ONE_MILLI_NANOS, false);

    assertThat(statistics.failureRatio()).isZero();
    assertThat(statistics.meanDurationMillis()).isEqualTo(50.0);
  }

  private void advanceMinutes(long minutes) {
    clock.addAndGet(minutes * ONE_MINUTE_NANOS);
  }
}
