package com.fintex.ce.test;

import com.fintex.wm.commons.domain.DataProvider;

import java.math.BigDecimal;
import java.util.List;

/**
 * A numeric Security Master datapoint as it travels on the wire — the value, and which providers it came from — for
 * tests that stub the attribute endpoint.
 *
 * <p>
 * Hand-written rather than taken from the commons domain because a fixture has to state the wire shape rather than
 * inherit it: Jackson serialization is property-name driven, so a payload built from a domain class silently follows
 * that class through any rename, and the test would then prove the engine parses a shape the vendor never sends. See
 * {@link AttributeCurrencyDatapoint} for the case where the two already differ.
 *
 * <p>
 * Numeric attributes are shaped this way whatever they carry, which is why this lives in the api module beside
 * {@link PortfolioHoldingBuildHelper} rather than inside any one attribute's fixtures.
 */
public record AttributeDatapoint(BigDecimal value, List<DataProvider> dataProviders) {

  /** The datapoint for a value given in the vendor's decimal notation, or none when the vendor reports none. */
  public static AttributeDatapoint of(String value, List<DataProvider> dataProviders) {
    return value == null ? null : new AttributeDatapoint(new BigDecimal(value), dataProviders);
  }
}
