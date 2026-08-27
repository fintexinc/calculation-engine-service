package com.fintex.ce.test;

import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.currency.Currency;

import java.util.List;

/**
 * The currency datapoint every Security Master attribute carries, as it travels on the wire: the value under
 * {@code type}, not under {@code value}.
 *
 * <p>
 * That is why it cannot be the commons {@code CurrencyDatapoint}, whose field is named {@code value}: a fixture built
 * from the domain class would emit {@code {"value": "CAD"}}, the engine would read no currency at all, and the test
 * would pass on a payload the vendor never sends. Keeping the wire shape explicit here is what makes that mismatch
 * impossible to reintroduce silently.
 */
public record AttributeCurrencyDatapoint(Currency type, List<DataProvider> dataProviders) {
}
