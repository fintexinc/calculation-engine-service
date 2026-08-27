package ca.tangerine.pce.webclient.observability;

import java.util.Collection;
import java.util.Map;

/**
 * Counts the top-level items in a deserialized provider response, so that a generic client can report how much came
 * back without knowing what it fetched.
 *
 * <p>
 * A missing body counts as zero, which is what makes an empty response distinguishable from a useful one. A payload
 * that is neither a collection nor a map is one item: a client that asked for a single object and got it did not get
 * nothing. A response whose items sit inside an envelope says so by implementing {@link CountedResponse}, because only
 * it knows where they are.
 */
public final class ResponseItemCount {

  private ResponseItemCount() {
  }

  public static int of(Object body) {
    if (body == null) {
      return 0;
    }
    if (body instanceof CountedResponse response) {
      return Math.max(0, response.itemCount());
    }
    if (body instanceof Collection<?> items) {
      return items.size();
    }
    if (body instanceof Map<?, ?> items) {
      return items.size();
    }
    return 1;
  }
}
