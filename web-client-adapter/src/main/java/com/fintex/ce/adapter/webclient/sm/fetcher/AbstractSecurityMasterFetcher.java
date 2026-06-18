package com.fintex.ce.adapter.webclient.sm.fetcher;

import com.fintex.ce.adapter.webclient.sm.client.SecurityMasterWebClient;
import com.fintex.ce.adapter.webclient.sm.mapper.HoldingMappingUtils;
import com.fintex.ce.adapter.webclient.sm.mapper.SecurityMasterResponseMapper;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.ce.util.BatchContext;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.attribute.SecurityAttributeBundle;
import com.fintex.wm.commons.domain.attribute.SecurityAttributeResult;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;
import com.fintex.wm.commons.dto.request.IdsAndDataProvidersRequest;
import com.fintex.wm.commons.dto.search.TypedIdentifiers;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

import static java.util.stream.Collectors.groupingBy;

/**
 * Abstract base class for fetching data from Security Master REST API.
 *
 * @param <D>
 *          Domain model type returned to the application (e.g., AssetAllocation)
 * @param <R>
 *          SMS API response type that will be mapped to domain model
 */
@Slf4j
public abstract class AbstractSecurityMasterFetcher<D, R> implements SecurityDataFetcher<D> {

  private static final String KEY_SEPARATOR = ":";

  protected final SecurityMasterWebClient client;
  protected final String endpointPath;
  protected final SecurityMasterResponseMapper<D, R> mapper;
  protected final ParameterizedTypeReference<List<SecurityAttributeResult<R>>> responseType;

  @Nullable
  private final SecurityAttributeBundle bundle;
  @Nullable
  private final Class<R> smResponseClass;

  protected AbstractSecurityMasterFetcher(SecurityMasterWebClient client, String endpointPath,
      SecurityMasterResponseMapper<D, R> mapper,
      ParameterizedTypeReference<List<SecurityAttributeResult<R>>> responseType,
      @Nullable SecurityAttributeBundle bundle,
      @Nullable Class<R> smResponseClass) {
    this.client = client;
    this.endpointPath = endpointPath;
    this.mapper = mapper;
    this.responseType = responseType;
    this.bundle = bundle;
    this.smResponseClass = smResponseClass;
  }

  protected AbstractSecurityMasterFetcher(SecurityMasterWebClient client, String endpointPath,
      SecurityMasterResponseMapper<D, R> mapper,
      ParameterizedTypeReference<List<SecurityAttributeResult<R>>> responseType) {
    this(client, endpointPath, mapper, responseType, null, null);
  }

  @Nullable
  public SecurityAttributeBundle getBundle() {
    return bundle;
  }

  @Nullable
  public Class<R> getSmResponseClass() {
    return smResponseClass;
  }

  /**
   * Warms up {@link BatchContext} for this fetcher using results already fetched by the SM batch attribute endpoint.
   * Skips if BatchContext is inactive, bundle is null, or the entry is already cached. Must be called before the metric
   * calculation loop.
   */
  public void warmUp(List<SecurityAttributeResult<R>> smResults,
      List<? extends PortfolioHolding> holdings,
      List<DataProvider> providers) {
    if (!BatchContext.isActive() || CollectionUtils.isEmpty(smResults)) {
      return;
    }
    String cacheKey = buildBatchCacheKey(holdings, providers);
    if (BatchContext.get(cacheKey).isPresent()) {
      return;
    }
    Map<String, List<PortfolioHolding>> identifierMap = buildIdentifierMap(holdings);
    Map<PortfolioHolding, D> result = mapResponsesToHoldings(smResults, identifierMap);
    BatchContext.put(cacheKey, result);
    log.debug("Batch SM pre-warm for bundle {} ({} results, {} holdings)", bundle, smResults.size(),
        holdings.size());
  }

  @Override
  public Map<PortfolioHolding, D> fetch(List<? extends PortfolioHolding> holdings, List<DataProvider> providers) {
    if (CollectionUtils.isEmpty(holdings)) {
      return Collections.emptyMap();
    }
    if (!BatchContext.isActive()) {
      return doFetch(holdings, providers);
    }
    String cacheKey = buildBatchCacheKey(holdings, providers);
    Optional<Map<PortfolioHolding, D>> cached = BatchContext.get(cacheKey);
    if (cached.isPresent()) {
      log.debug("Batch SM cache hit for endpoint {} ({} holdings)", endpointPath, holdings.size());
      return cached.get();
    }
    Map<PortfolioHolding, D> result = doFetch(holdings, providers);
    BatchContext.put(cacheKey, result);
    return result;
  }

  String buildBatchCacheKey(List<? extends PortfolioHolding> holdings, List<DataProvider> providers) {
    String holdingsKey = holdings.stream()
        .filter(h -> h.getSecurityIdentifier() != null)
        .map(h -> (h.getHoldingType() != null ? h.getHoldingType().name() : "")
            + ":" + Objects.toString(h.getSecurityIdentifier().getIdType(), "")
            + ":" + Objects.toString(h.getSecurityIdentifier().getId(), ""))
        .sorted()
        .collect(Collectors.joining(","));
    String providersKey = CollectionUtils.isEmpty(providers)
        ? ""
        : providers.stream().map(Object::toString).sorted().collect(Collectors.joining(","));
    return endpointPath + "|" + holdingsKey + "|" + providersKey;
  }

  /**
   * Builds the identifier → holdings map used to match SM responses back to portfolio holdings. Package-visible for use
   * by {@link SmBatchAttributeFetcherImpl}.
   */
  Map<String, List<PortfolioHolding>> buildIdentifierMap(List<? extends PortfolioHolding> holdings) {
    Map<String, List<PortfolioHolding>> result = new HashMap<>();
    holdings.stream()
        .filter(h -> h.getSecurityIdentifier() != null && hasValidHoldingType(h))
        .forEach(h -> result
            .computeIfAbsent(buildKey(h.getSecurityIdentifier()), k -> new ArrayList<>())
            .add(h));
    return result;
  }

  private Map<PortfolioHolding, D> doFetch(List<? extends PortfolioHolding> holdings, List<DataProvider> providers) {
    Map<FinancialInstrumentType, List<PortfolioHolding>> groupedHoldings = groupHoldingsByType(holdings);
    if (groupedHoldings.isEmpty()) {
      return Collections.emptyMap();
    }

    // List because multiple holdings can share the same security (e.g., same stock in different accounts)
    Map<String, List<PortfolioHolding>> identifierToHoldings = new HashMap<>();
    List<TypedIdentifiers> typedIdentifiers = buildTypedIdentifiers(groupedHoldings, identifierToHoldings);

    if (CollectionUtils.isEmpty(typedIdentifiers)) {
      log.debug("No valid identifiers to fetch for endpoint: {}", endpointPath);
      return Collections.emptyMap();
    }

    IdsAndDataProvidersRequest request = IdsAndDataProvidersRequest.builder()
        .typedIdentifiers(typedIdentifiers)
        .dataProviders(providers)
        .build();

    List<SecurityAttributeResult<R>> responses = client.post(
        endpointPath,
        request,
        responseType);

    Map<PortfolioHolding, D> result = mapResponsesToHoldings(
        responses != null ? responses : List.of(),
        identifierToHoldings);

    log.debug("Fetched {} results for {} holdings from endpoint: {}",
        result.size(), holdings.size(), endpointPath);

    return result;
  }

  private Map<FinancialInstrumentType, List<PortfolioHolding>> groupHoldingsByType(
      List<? extends PortfolioHolding> holdings) {
    return holdings.stream()
        .filter(this::hasValidHoldingType)
        .collect(groupingBy(PortfolioHolding::getHoldingType));
  }

  private boolean hasValidHoldingType(PortfolioHolding holding) {
    return holding.getHoldingType() != null && !HoldingMappingUtils.isSkipped(holding.getHoldingType());
  }

  private List<TypedIdentifiers> buildTypedIdentifiers(
      Map<FinancialInstrumentType, List<PortfolioHolding>> groupedHoldings,
      Map<String, List<PortfolioHolding>> identifierToHoldings) {

    return groupedHoldings.entrySet().stream()
        .map(entry -> buildTypedIdentifierForType(entry, identifierToHoldings))
        .filter(Objects::nonNull)
        .toList();
  }

  private TypedIdentifiers buildTypedIdentifierForType(
      Map.Entry<FinancialInstrumentType, List<PortfolioHolding>> entry,
      Map<String, List<PortfolioHolding>> identifierToHoldings) {

    List<SecurityIdentifier> identifiers = entry.getValue().stream()
        .filter(h -> h.getSecurityIdentifier() != null)
        .peek(h -> {
          String key = buildKey(h.getSecurityIdentifier());
          identifierToHoldings.computeIfAbsent(key, k -> new ArrayList<>()).add(h);
        })
        .map(PortfolioHolding::getSecurityIdentifier)
        .toList();

    if (identifiers.isEmpty()) {
      return null;
    }

    return TypedIdentifiers.builder()
        .type(entry.getKey())
        .ids(identifiers)
        .build();
  }

  private Map<PortfolioHolding, D> mapResponsesToHoldings(
      List<SecurityAttributeResult<R>> responses,
      Map<String, List<PortfolioHolding>> identifierToHoldings) {

    if (CollectionUtils.isEmpty(responses)) {
      return Collections.emptyMap();
    }

    return responses.stream()
        .filter(this::isValidResponse)
        .flatMap(response -> mapResponseToHoldings(response, identifierToHoldings).entrySet().stream())
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            Map.Entry::getValue,
            (existing, replacement) -> existing));
  }

  private Map<PortfolioHolding, D> mapResponseToHoldings(
      SecurityAttributeResult<R> response,
      Map<String, List<PortfolioHolding>> identifierToHoldings) {

    String responseKey = buildKey(response.getIdentifier());
    List<PortfolioHolding> holdings = identifierToHoldings.get(responseKey);

    if (CollectionUtils.isEmpty(holdings)) {
      log.warn("No matching holdings for identifier: {}", response.getIdentifier());
      return Collections.emptyMap();
    }

    return holdings.stream()
        .map(holding -> {
          D mapped = mapper.map(response.getData(), holding);
          return mapped != null ? Map.entry(holding, mapped) : null;
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private boolean isValidResponse(SecurityAttributeResult<R> response) {
    return response != null && response.getIdentifier() != null && response.getData() != null;
  }

  private String buildKey(SecurityIdentifier identifier) {
    return Objects.toString(identifier.getIdType(), "") + KEY_SEPARATOR + Objects.toString(identifier.getId(), "");
  }
}
