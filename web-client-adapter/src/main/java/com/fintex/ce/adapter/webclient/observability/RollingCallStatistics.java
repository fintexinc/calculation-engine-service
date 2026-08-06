package com.fintex.ce.adapter.webclient.observability;

import java.time.Duration;
import java.util.function.LongSupplier;

/**
 * Rolling per-provider tally of call count, failures and total duration, used to publish a failure ratio and a mean
 * latency as gauges.
 *
 * <p>
 * A ratio and a mean are derived values, so they need a window to be derived over. A cumulative-since-startup ratio is
 * close to useless — one bad hour never washes out of it and the number only ever drifts — so the tally is kept in
 * {@link #BUCKETS} one-minute buckets and the oldest bucket is dropped as time advances. Reads therefore describe the
 * last few minutes, which is what an alert threshold needs.
 */
final class RollingCallStatistics {

  static final int BUCKETS = 5;
  static final Duration WINDOW = Duration.ofMinutes(BUCKETS);

  private static final long BUCKET_NANOS = Duration.ofMinutes(1).toNanos();

  private final long[] calls = new long[BUCKETS];
  private final long[] failures = new long[BUCKETS];
  private final long[] durationNanos = new long[BUCKETS];
  private final LongSupplier nanoClock;

  private long currentBucketStartNanos;
  private int currentBucket;

  RollingCallStatistics() {
    this(System::nanoTime);
  }

  RollingCallStatistics(LongSupplier nanoClock) {
    this.nanoClock = nanoClock;
    this.currentBucketStartNanos = nanoClock.getAsLong();
  }

  synchronized void record(long callDurationNanos, boolean failure) {
    advance();
    calls[currentBucket]++;
    durationNanos[currentBucket] += callDurationNanos;
    if (failure) {
      failures[currentBucket]++;
    }
  }

  synchronized double failureRatio() {
    advance();
    long totalCalls = sum(calls);
    return totalCalls == 0 ? 0 : (double) sum(failures) / totalCalls;
  }

  synchronized double meanDurationMillis() {
    advance();
    long totalCalls = sum(calls);
    return totalCalls == 0 ? 0 : (double) sum(durationNanos) / totalCalls / 1_000_000;
  }

  private void advance() {
    long now = nanoClock.getAsLong();
    long elapsedBuckets = (now - currentBucketStartNanos) / BUCKET_NANOS;
    if (elapsedBuckets <= 0) {
      return;
    }
    long bucketsToClear = Math.min(elapsedBuckets, BUCKETS);
    for (long i = 0; i < bucketsToClear; i++) {
      currentBucket = (currentBucket + 1) % BUCKETS;
      calls[currentBucket] = 0;
      failures[currentBucket] = 0;
      durationNanos[currentBucket] = 0;
    }
    currentBucketStartNanos += elapsedBuckets * BUCKET_NANOS;
  }

  private static long sum(long[] values) {
    long total = 0;
    for (long value : values) {
      total += value;
    }
    return total;
  }
}
