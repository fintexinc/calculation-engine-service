package com.fintex.ce.adapter.webclient.sm.fetcher;

import com.fintex.ce.adapter.webclient.sm.client.SecurityMasterWebClient;
import com.fintex.ce.adapter.webclient.sm.mapper.HoldingMappingUtils;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.port.webclient.sm.SmBatchAttributeFetcher;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.attribute.BatchAttributeResponse;
import com.fintex.wm.commons.domain.attribute.SecurityAttributeBundle;
import com.fintex.wm.commons.domain.attribute.SecurityAttributeResult;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;
import com.fintex.wm.commons.dto.BatchAttributeRequest;
import com.fintex.wm.commons.dto.search.TypedIdentifiers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(name = "external-services.security-master.api-type", havingValue = "rest", matchIfMissing = true)
public class SmBatchAttributeFetcherImpl implements SmBatchAttributeFetcher {

  private static final ParameterizedTypeReference<BatchAttributeResponse> RESPONSE_TYPE = new ParameterizedTypeReference<>() {};

  private final SecurityMasterWebClient client;
  private final String batchEndpointPath;
  private final ObjectMapper objectMapper;
  private final Map<SecurityAttributeBundle, AbstractSecurityMasterFetcher<?, ?>> fetcherByBundle;

  public SmBatchAttributeFetcherImpl(
      SecurityMasterWebClient client,
      @Value("${external-services.security-master.rest.endpoints.attributes.batch}") String batchEndpointPath,
      ObjectMapper objectMapper,
      Collection<AbstractSecurityMasterFetcher<?, ?>> fetchers) {
    this.client = client;
    this.batchEndpointPath = batchEndpointPath;
    this.objectMapper = objectMapper;
    this.fetcherByBundle = new EnumMap<>(SecurityAttributeBundle.class);
    for (AbstractSecurityMasterFetcher<?, ?> fetcher : fetchers) {
      if (fetcher.getBundle() != null) {
        fetcherByBundle.put(fetcher.getBundle(), fetcher);
      }
    }
  }

  @Override
  public void prefetchIntoContext(List<PortfolioHolding> holdings,
      List<SecurityAttributeBundle> bundles,
      List<DataProvider> providers) {
    if (holdings == null || holdings.isEmpty() || bundles == null || bundles.isEmpty()) {
      return;
    }

    List<TypedIdentifiers> typedIdentifiers = buildTypedIdentifiers(holdings);
    if (typedIdentifiers.isEmpty()) {
      return;
    }

    BatchAttributeRequest request = new BatchAttributeRequest(typedIdentifiers, providers, bundles);
    BatchAttributeResponse batchResponse = client.post(batchEndpointPath, request, RESPONSE_TYPE);

    if (batchResponse == null) {
      log.warn("SM batch attribute endpoint returned null — skipping pre-warm");
      return;
    }

    batchResponse.forEach((bundle, rawResults) -> warmUpFetcher(bundle, rawResults, holdings, providers));
  }

  @SuppressWarnings("unchecked")
  private <R> void warmUpFetcher(SecurityAttributeBundle bundle,
      List<SecurityAttributeResult<?>> rawResults,
      List<PortfolioHolding> holdings,
      List<DataProvider> providers) {
    AbstractSecurityMasterFetcher<?, R> fetcher = (AbstractSecurityMasterFetcher<?, R>) fetcherByBundle.get(bundle);
    if (fetcher == null || fetcher.getSmResponseClass() == null) {
      log.debug("No registered fetcher for bundle {} — skipping warm-up", bundle);
      return;
    }

    Class<R> responseClass = fetcher.getSmResponseClass();
    List<SecurityAttributeResult<R>> typedResults = rawResults.stream()
        .filter(Objects::nonNull)
        .map(r -> convertResult(r, responseClass))
        .filter(Objects::nonNull)
        .toList();

    fetcher.warmUp(typedResults, holdings, providers);
  }

  private <R> SecurityAttributeResult<R> convertResult(SecurityAttributeResult<?> raw, Class<R> targetClass) {
    if (raw.getData() == null) {
      return null;
    }
    try {
      R converted = objectMapper.convertValue(raw.getData(), targetClass);
      return SecurityAttributeResult.<R>builder()
          .identifier(raw.getIdentifier())
          .data(converted)
          .build();
    } catch (Exception e) {
      log.warn("Failed to convert SM batch result to {}: {}", targetClass.getSimpleName(), e.getMessage());
      return null;
    }
  }

  private List<TypedIdentifiers> buildTypedIdentifiers(List<PortfolioHolding> holdings) {
    Map<FinancialInstrumentType, List<SecurityIdentifier>> byType = holdings.stream()
        .filter(h -> h.getHoldingType() != null
            && !HoldingMappingUtils.isSkipped(h.getHoldingType())
            && h.getSecurityIdentifier() != null)
        .collect(Collectors.groupingBy(
            PortfolioHolding::getHoldingType,
            Collectors.mapping(PortfolioHolding::getSecurityIdentifier,
                Collectors.toCollection(ArrayList::new))));

    return byType.entrySet().stream()
        .filter(e -> !e.getValue().isEmpty())
        .map(e -> TypedIdentifiers.builder()
            .type(e.getKey())
            .ids(e.getValue())
            .build())
        .toList();
  }
}
