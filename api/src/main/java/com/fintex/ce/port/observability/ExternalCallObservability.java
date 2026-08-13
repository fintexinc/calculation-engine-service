package com.fintex.ce.port.observability;

import com.fintex.wm.commons.domain.ExternalWebService;

/**
 * How this service reports an outbound call to an external data provider. Clients report what only they can know —
 * whether the response carried usable data, how many items came back and how the call failed — and stay free of any
 * metrics or tracing technology.
 *
 * <p>
 * Transport-level timing and the client span are not reported through this port: they already come from the framework
 * instrumentation on the web client, and reporting them a second time would double count them.
 *
 * <p>
 * The meter names and tag vocabulary behind this port are shared with the Security Master Service, so one dashboard
 * covers both. Only the outcomes this service can actually reach are declared here; Security Master reports rate
 * limiting and cancellation as well, because it has a client-side limiter and a reactive client.
 *
 * <p>
 * {@link #NO_OP} exists for contexts that deliberately publish nothing — a slim test context wiring a client without
 * the telemetry around it. It is an explicit choice at the point of wiring, never a fallback substituted when the real
 * implementation is missing: a deployment that lost its observability must fail at startup rather than run on quietly
 * with no metrics.
 */
public interface ExternalCallObservability {

  ExternalCallObservability NO_OP = (service, httpMethod, endpoint) -> new ExternalCallObservability.ExternalCall() {

    @Override
    public void completed(int itemCount) {
    }

    @Override
    public void failed(Throwable cause) {
    }

    @Override
    public void httpFailed(int statusCode, Throwable cause) {
    }
  };

  /**
   * Begins observing a call. The endpoint must be the templated path, or one whose dynamic segments are bounded by an
   * enum, rather than an expanded one, so that the number of distinct values it can take stays bounded.
   */
  ExternalCall start(ExternalWebService service, String httpMethod, String endpoint);

  /**
   * A call in flight, carrying the provider, method and endpoint settled at {@link #start} so an outcome can never be
   * filed against a different call than the one measured. The first outcome reported wins, so the client's error
   * handler can report the status it saw and let the exception that follows pass through without being counted twice.
   */
  interface ExternalCall {

    /**
     * Reports a call that returned without error, and how many items it carried. A response carrying no items is a
     * failure mode these providers actually exhibit and is reported apart from a successful one.
     */
    void completed(int itemCount);

    /**
     * Reports a call that failed without an HTTP response of its own — a transport, timeout or deserialization failure.
     */
    void failed(Throwable cause);

    /**
     * Reports a call the provider answered with an error status. The status is passed separately from the cause so that
     * a transport failure is never dressed up as a response the provider did not send.
     */
    void httpFailed(int statusCode, Throwable cause);
  }
}
