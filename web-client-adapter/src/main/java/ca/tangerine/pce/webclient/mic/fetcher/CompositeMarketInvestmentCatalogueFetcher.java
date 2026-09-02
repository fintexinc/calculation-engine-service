package ca.tangerine.pce.webclient.mic.fetcher;

import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.pce.model.domain.security.SecurityData;
import ca.tangerine.pce.model.error.ErrorCode;
import ca.tangerine.pce.port.webclient.mic.SecurityAttributesFetcher;
import ca.tangerine.pce.webclient.mic.client.MarketInvestmentCatalogueWebClient;
import ca.tangerine.wm.commons.domain.DataProvider;
import ca.tangerine.wm.commons.domain.attribute.SecurityAttributeResult;
import ca.tangerine.wm.commons.domain.enumeration.CompositeSecurityAttribute;
import ca.tangerine.wm.commons.dto.request.CompositeAttributesRequest;
import ca.tangerine.wm.commons.dto.request.IdsAndDataProvidersRequest;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Generic Market Investment Catalogue client that fetches any combination of composite security attributes in a single
 * request to the MIC {@code /attributes} endpoint. The attribute payloads arrive as raw JSON and are converted into the
 * CE domain models declared by the {@link CompositeAttributeBinding} registered for each attribute. Attributes without
 * a registered binding are rejected up front so a misconfigured request fails loudly instead of silently dropping data.
 */
@Slf4j
public class CompositeMarketInvestmentCatalogueFetcher implements SecurityAttributesFetcher {

  private static final ParameterizedTypeReference<Map<CompositeSecurityAttribute, List<SecurityAttributeResult<JsonNode>>>> RESPONSE_TYPE = new ParameterizedTypeReference<>() {};

  private static final ParameterizedTypeReference<List<SecurityAttributeResult<JsonNode>>> SINGLE_ATTRIBUTE_RESPONSE_TYPE = new ParameterizedTypeReference<>() {};

  private static final String SINGLE_ATTRIBUTE_PATH_TEMPLATE = "%s/%s";

  private final MarketInvestmentCatalogueWebClient client;
  private final String endpointPath;
  private final ObjectMapper objectMapper;
  private final Map<CompositeSecurityAttribute, CompositeAttributeBinding<?, ?>> bindings;

  public CompositeMarketInvestmentCatalogueFetcher(MarketInvestmentCatalogueWebClient client, String endpointPath,
      ObjectMapper objectMapper, List<CompositeAttributeBinding<?, ?>> bindings) {
    this.client = client;
    this.endpointPath = endpointPath;
    this.objectMapper = objectMapper;
    this.bindings = bindings.stream()
        .collect(Collectors.toMap(CompositeAttributeBinding::attribute, Function.identity(),
            (existing, duplicate) -> {
              throw ErrorCode.INTERNAL_SERVER_ERROR.toException();
            },
            () -> new EnumMap<>(CompositeSecurityAttribute.class)));
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> Map<PortfolioHolding, T> fetch(
      List<? extends PortfolioHolding> holdings,
      CompositeSecurityAttribute attribute,
      List<DataProvider> providers) {
    if (CollectionUtils.isEmpty(holdings) || attribute == null) {
      return Map.of();
    }
    requireBound(List.of(attribute));

    HoldingIdentifierIndex index = HoldingIdentifierIndex.of(holdings);
    if (index.isEmpty()) {
      log.warn("No valid identifiers to fetch attribute {} for", attribute);
      return Map.of();
    }

    IdsAndDataProvidersRequest request = IdsAndDataProvidersRequest.builder()
        .typedIdentifiers(index.getTypedIdentifiers())
        .dataProviders(providers)
        .build();

    List<SecurityAttributeResult<JsonNode>> response = client.post(
        singleAttributePath(attribute), request, SINGLE_ATTRIBUTE_RESPONSE_TYPE);

    Map<PortfolioHolding, Object> result = bindings.get(attribute)
        .mapResults(response == null ? List.of() : response, index, objectMapper);

    log.debug("Fetched attribute {} for {} of {} holdings", attribute, result.size(), holdings.size());

    return (Map<PortfolioHolding, T>) result;
  }

  private String singleAttributePath(CompositeSecurityAttribute attribute) {
    return SINGLE_ATTRIBUTE_PATH_TEMPLATE.formatted(endpointPath, attribute.getAttributeName());
  }

  @Override
  public SecurityData fetch(
      List<? extends PortfolioHolding> holdings,
      Collection<CompositeSecurityAttribute> attributes,
      List<DataProvider> providers) {
    if (CollectionUtils.isEmpty(holdings) || CollectionUtils.isEmpty(attributes)) {
      return SecurityData.EMPTY;
    }
    requireBound(attributes);

    HoldingIdentifierIndex index = HoldingIdentifierIndex.of(holdings);
    if (index.isEmpty()) {
      log.debug("No valid identifiers to fetch composite attributes {} for", attributes);
      return SecurityData.EMPTY;
    }

    CompositeAttributesRequest request = CompositeAttributesRequest.builder()
        .typedIdentifiers(index.getTypedIdentifiers())
        .dataProviders(providers)
        .attributes(List.copyOf(attributes))
        .build();

    Map<CompositeSecurityAttribute, List<SecurityAttributeResult<JsonNode>>> response = client.post(
        endpointPath, request, RESPONSE_TYPE);

    Map<CompositeSecurityAttribute, Map<PortfolioHolding, Object>> result = mapResponse(response, attributes, index);

    log.debug("Fetched {} composite attributes for {} holdings from endpoint: {}",
        result.size(), holdings.size(), endpointPath);

    return SecurityData.of(result);
  }

  private Map<CompositeSecurityAttribute, Map<PortfolioHolding, Object>> mapResponse(
      Map<CompositeSecurityAttribute, List<SecurityAttributeResult<JsonNode>>> response,
      Collection<CompositeSecurityAttribute> attributes,
      HoldingIdentifierIndex index) {
    if (CollectionUtils.isEmpty(response)) {
      return Map.of();
    }

    return response.entrySet().stream()
        .filter(entry -> attributes.contains(entry.getKey()))
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            entry -> bindings.get(entry.getKey()).mapResults(entry.getValue(), index, objectMapper),
            (existing, duplicate) -> existing,
            () -> new EnumMap<>(CompositeSecurityAttribute.class)));
  }

  private void requireBound(Collection<CompositeSecurityAttribute> attributes) {
    List<CompositeSecurityAttribute> unbound = attributes.stream()
        .filter(attribute -> !bindings.containsKey(attribute))
        .toList();
    if (!unbound.isEmpty()) {
      log.error("No composite attribute binding registered for: {}", unbound);
      throw ErrorCode.INTERNAL_SERVER_ERROR.toException();
    }
  }

}
