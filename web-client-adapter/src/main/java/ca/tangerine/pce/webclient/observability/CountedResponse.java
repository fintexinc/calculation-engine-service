package ca.tangerine.pce.webclient.observability;

/**
 * Implemented by a provider response whose real payload sits inside an envelope, so that a generic client can report
 * how many items came back without knowing the shape of what it fetched.
 *
 * <p>
 * Without this, an envelope counts as one item and a response carrying nothing would be indistinguishable from a useful
 * one — which is exactly the failure mode the {@code empty} outcome exists to expose.
 */
public interface CountedResponse {

  int itemCount();
}
