package com.fintex.ce.adapter.observability.external;

/**
 * How an outbound call to an external data provider ended, as reported on the {@code outcome} tag. {@code EMPTY} is
 * separated from {@code SUCCESS} because a response that carries no usable data is a failure mode these providers
 * actually exhibit.
 *
 * <p>
 * The vocabulary is shared with the Market Investment Catalogue Service, which adds {@code rate_limited} and
 * {@code cancelled} for the two outcomes only a rate-limited reactive client can reach. A value is added here when a
 * client can actually report it, so that no outcome exists in the tag space without a path that produces it.
 */
enum ExternalCallOutcome {

  SUCCESS("success"),
  EMPTY("empty"),
  HTTP_ERROR("http_error"),
  ERROR("error");

  private final String id;

  ExternalCallOutcome(String id) {
    this.id = id;
  }

  String id() {
    return id;
  }
}
