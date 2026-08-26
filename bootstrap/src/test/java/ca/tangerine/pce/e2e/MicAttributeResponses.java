package ca.tangerine.pce.e2e;

import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import lombok.experimental.UtilityClass;

import ca.tangerine.wm.commons.domain.DataProvider;
import ca.tangerine.wm.commons.domain.allocation.GeographicAllocation;
import ca.tangerine.wm.commons.domain.allocation.GeographicAllocationValue;
import ca.tangerine.wm.commons.domain.allocation.GeographicAllocationWithCurrency;
import ca.tangerine.wm.commons.domain.allocation.GeographicRegionType;
import ca.tangerine.wm.commons.domain.allocation.RegionDatapoint;
import ca.tangerine.wm.commons.domain.allocation.SecurityRegion;
import ca.tangerine.wm.commons.domain.attribute.SecurityAttributeResult;
import ca.tangerine.wm.commons.domain.currency.Currency;
import ca.tangerine.wm.commons.domain.currency.CurrencyDatapoint;
import ca.tangerine.wm.commons.domain.enumeration.CompositeSecurityAttribute;
import ca.tangerine.wm.commons.domain.enumeration.Country;
import ca.tangerine.wm.commons.domain.financial.Geography;
import ca.tangerine.wm.commons.domain.id.FiIdentifierType;
import ca.tangerine.wm.commons.domain.id.SecurityIdentifier;
import ca.tangerine.wm.commons.domain.reference.CountryDatapoint;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;

/**
 * Shared helpers for stubbing the Market Investment Catalogue {@code /attributes} endpoint in bootstrap-level e2e
 * tests, and for assembling the attribute rows it answers with. Every breakdown metric fetches its data through that
 * one endpoint, so the dispatcher, the row envelope and the datapoint wrappers are the same in every such test; only
 * which attributes are asked for and what they carry is metric-specific.
 *
 * <p>
 * The dispatchers answer <em>every</em> matching call rather than one enqueued response, because how many calls the
 * fetcher batches a portfolio into is an implementation detail — a test that enqueues a fixed number of responses is a
 * test about batching.
 */
@UtilityClass
final class MicAttributeResponses {

  static final String ATTRIBUTES_PATH = "/api/v1/wealth/securities/attributes";

  private static final List<DataProvider> MORNINGSTAR_ONLY = List.of(DataProvider.MORNINGSTAR);

  /**
   * Answers a metric that asks for several attributes at once: Market Investment Catalogue serves those from the
   * collection endpoint, keyed by attribute.
   */
  static Dispatcher compositeDispatcher(
      Map<CompositeSecurityAttribute, List<? extends SecurityAttributeResult<?>>> rowsByAttribute) {
    return pathDispatcher(ATTRIBUTES_PATH, AbstractPortfolioCalculationE2ETest.writeJson(rowsByAttribute));
  }

  /**
   * Answers a metric that asks for exactly one attribute, which Market Investment Catalogue serves from
   * {@code …/attributes/{attribute-name}} as a bare list.
   *
   * <p>
   * The path is matched rather than ignored, so this also pins the metric's declared {@code requiredAttribute()}: a
   * metric that starts asking for a different attribute stops being answered and fails the test loudly instead of
   * silently receiving rows meant for the old one.
   */
  static Dispatcher singleAttributeDispatcher(CompositeSecurityAttribute attribute,
      List<? extends SecurityAttributeResult<?>> rows) {
    return singleAttributeDispatcher(attribute, AbstractPortfolioCalculationE2ETest.writeJson(rows));
  }

  /**
   * As above, for an attribute whose rows a test builds from its own DTOs rather than from
   * {@link SecurityAttributeResult} — the fee attribute is one, because its currency datapoint travels under a field
   * name the domain class does not expose.
   */
  static Dispatcher singleAttributeDispatcher(CompositeSecurityAttribute attribute, String body) {
    return pathDispatcher(ATTRIBUTES_PATH + "/" + attribute.getAttributeName(), body);
  }

  /**
   * As {@link #compositeDispatcher}, for a caller that already holds the serialized body — the shared positive scenario
   * hands one over as a string, and re-parsing it only to serialize it again would be a round trip for nothing.
   */
  static Dispatcher attributesDispatcher(String body) {
    return pathDispatcher(ATTRIBUTES_PATH, body);
  }

  /**
   * Matches the request path against the endpoint the dispatcher stands for, ending at it rather than merely containing
   * it. The distinction matters for the collection endpoint: {@code …/attributes} is a prefix of every
   * {@code …/attributes/{name}}, so a containment test would have a composite dispatcher answer single-attribute calls
   * too — and the guarantee these dispatchers are built for, that a metric asking for the wrong attribute goes
   * unanswered, would not hold for exactly the metrics that ask for several.
   */
  private static Dispatcher pathDispatcher(String expectedPath, String body) {
    return new Dispatcher() {
      @Override
      public MockResponse dispatch(RecordedRequest request) {
        String path = request.getPath();
        if (path != null && withoutQuery(path).endsWith(expectedPath)) {
          return jsonResponse(body);
        }
        return new MockResponse().setResponseCode(404);
      }
    };
  }

  private static String withoutQuery(String path) {
    int query = path.indexOf('?');
    return query < 0 ? path : path.substring(0, query);
  }

  static MockResponse jsonResponse(String body) {
    return new MockResponse()
        .setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .setBody(body);
  }

  /**
   * One attribute row as Market Investment Catalogue serves it: the identifier the holding was fetched by, paired with
   * the attribute payload. Built from the identifier's parts rather than a {@link SecurityIdentifier} so a fixture
   * reads as the ticker or Morningstar id it is about.
   */
  static <T> SecurityAttributeResult<T> attributeResult(String id, FiIdentifierType idType, T data) {
    return AbstractPortfolioCalculationE2ETest.securityAttributeResult(new SecurityIdentifier(id, idType), data);
  }

  static CurrencyDatapoint currencyDatapoint(Currency currency) {
    CurrencyDatapoint datapoint = new CurrencyDatapoint();
    datapoint.setValue(currency);
    return datapoint;
  }

  /**
   * The {@code GEOGRAPHY} attribute row. Both of its region-bearing fields are optional on purpose: a stock resolves
   * its region from {@code businessCountry} when Market Investment Catalogue knows it and falls back to the coarse
   * {@link SecurityRegion}, and a fund carries neither — it is fetched for its currency alone.
   */
  static SecurityAttributeResult<Geography> geographyRow(String id, FiIdentifierType idType, Country businessCountry,
      SecurityRegion region, Currency currency) {
    Geography geography = new Geography();
    if (businessCountry != null) {
      geography.setBusinessCountry(new CountryDatapoint(businessCountry));
    }
    if (region != null) {
      RegionDatapoint regionDatapoint = new RegionDatapoint();
      regionDatapoint.setValue(region);
      geography.setRegion(regionDatapoint);
    }
    if (currency != null) {
      geography.setCurrency(currencyDatapoint(currency));
    }
    geography.setDataProviders(MORNINGSTAR_ONLY);
    return attributeResult(id, idType, geography);
  }

  /**
   * A region-allocation row, the shape all three geographic-exposure metrics read — the equity sleeve, the fixed-income
   * sleeve and the whole security each under their own attribute, but each as a region vector paired with the currency
   * its security is quoted in, which is why this metric family needs no second attribute to weight across currencies.
   */
  static SecurityAttributeResult<GeographicAllocationWithCurrency> geographicAllocationRow(String id,
      FiIdentifierType idType, Currency currency, GeographicAllocationValue... values) {
    GeographicAllocation allocation = new GeographicAllocation();
    allocation.setAllocations(new ArrayList<>(List.of(values)));
    allocation.setDataProviders(MORNINGSTAR_ONLY);

    GeographicAllocationWithCurrency row = new GeographicAllocationWithCurrency();
    row.setGeographicAllocation(allocation);
    row.setCurrency(currencyDatapoint(currency));
    row.setDataProviders(MORNINGSTAR_ONLY);
    return attributeResult(id, idType, row);
  }

  /**
   * The row Market Investment Catalogue serves for a security that declares the datapoint without any regions on it —
   * the datapoint is assembled from whichever of its columns the security's table carries, and every security carries
   * {@code currency}, so a currency-only row is a shape the caller actually meets and must not mistake for data.
   */
  static SecurityAttributeResult<GeographicAllocationWithCurrency> emptyGeographicAllocationRow(String id,
      FiIdentifierType idType, Currency currency) {
    GeographicAllocationWithCurrency row = new GeographicAllocationWithCurrency();
    row.setCurrency(currencyDatapoint(currency));
    row.setDataProviders(MORNINGSTAR_ONLY);
    return attributeResult(id, idType, row);
  }

  static GeographicAllocationValue regionValue(GeographicRegionType region, String value,
      String... originalTypeNames) {
    return new GeographicAllocationValue(region, new BigDecimal(value), new TreeSet<>(List.of(originalTypeNames)));
  }

  static List<DataProvider> morningstarOnly() {
    return MORNINGSTAR_ONLY;
  }
}
