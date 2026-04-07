package com.fintex.ce.adapter.webclient.sm.fetcher;

import com.fintex.ce.adapter.webclient.mapper.HoldingTypeMapper;
import com.fintex.ce.adapter.webclient.sm.client.SecurityMasterWebClient;
import com.fintex.ce.adapter.webclient.sm.dto.IdsAndDataProvidersRequest;
import com.fintex.ce.adapter.webclient.sm.dto.IdsAndDataProvidersRequest.TypedIdentifiers;
import com.fintex.ce.adapter.webclient.sm.dto.SecurityAttributeResult;
import com.fintex.ce.adapter.webclient.sm.mapper.SecurityMasterResponseMapper;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.sm.model.DataProvider;
import com.fintex.sm.model.domain.SecurityIdentifier;
import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

/**
 * Abstract base class for fetching data from Security Master REST API.
 *
 * @param <D> Domain model type returned to the application (e.g., AssetAllocation)
 * @param <R> SMS API response type that will be mapped to domain model
 */
@Slf4j
public abstract class AbstractSecurityMasterFetcher<D, R> implements SecurityDataFetcher<D> {

    private static final String KEY_SEPARATOR = ":";

    protected final SecurityMasterWebClient client;
    protected final String endpointPath;
    protected final SecurityMasterResponseMapper<D, R> mapper;
    protected final ParameterizedTypeReference<List<SecurityAttributeResult<R>>> responseType;

    protected AbstractSecurityMasterFetcher(SecurityMasterWebClient client, String endpointPath,
        SecurityMasterResponseMapper<D, R> mapper,
        ParameterizedTypeReference<List<SecurityAttributeResult<R>>> responseType) {
        this.client = client;
        this.endpointPath = endpointPath;
        this.mapper = mapper;
        this.responseType = responseType;
    }

    @Override
    public Map<Holding, D> fetch(List<? extends Holding> holdings, List<DataProvider> providers) {
        if (CollectionUtils.isEmpty(holdings)) {
            return Collections.emptyMap();
        }

        Map<FinancialInstrumentType, List<Holding>> groupedHoldings = groupHoldingsByType(holdings);
        if (groupedHoldings.isEmpty()) {
            return Collections.emptyMap();
        }

        // List because multiple holdings can share the same security (e.g., same stock in different accounts)
        Map<String, List<Holding>> identifierToHoldings = new HashMap<>();
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

        Map<Holding, D> result = mapResponsesToHoldings(
                responses != null ? responses : List.of(),
                identifierToHoldings);

        log.debug("Fetched {} results for {} holdings from endpoint: {}",
                result.size(), holdings.size(), endpointPath);

        return result;
    }

    private Map<FinancialInstrumentType, List<Holding>> groupHoldingsByType(List<? extends Holding> holdings) {
        return holdings.stream()
                .filter(this::hasValidHoldingType)
                .collect(groupingBy(Holding::getHoldingType));
    }

    private boolean hasValidHoldingType(Holding holding) {
        return holding.getHoldingType() != null && !HoldingTypeMapper.isSkipped(holding.getHoldingType());
    }

    private List<TypedIdentifiers> buildTypedIdentifiers(
            Map<FinancialInstrumentType, List<Holding>> groupedHoldings,
            Map<String, List<Holding>> identifierToHoldings) {

        return groupedHoldings.entrySet().stream()
                .map(entry -> buildTypedIdentifierForType(entry, identifierToHoldings))
                .filter(Objects::nonNull)
                .toList();
    }

    private TypedIdentifiers buildTypedIdentifierForType(
            Map.Entry<FinancialInstrumentType, List<Holding>> entry,
            Map<String, List<Holding>> identifierToHoldings) {

        List<SecurityIdentifier> identifiers = entry.getValue().stream()
                .filter(h -> h.getSecurityIdentifier() != null)
                .peek(h -> {
                    String key = buildKey(h.getSecurityIdentifier());
                    identifierToHoldings.computeIfAbsent(key, k -> new ArrayList<>()).add(h);
                })
                .map(Holding::getSecurityIdentifier)
                .toList();

        if (identifiers.isEmpty()) {
            return null;
        }

        return TypedIdentifiers.builder()
                .type(entry.getKey().name())
                .ids(identifiers)
                .build();
    }

    private Map<Holding, D> mapResponsesToHoldings(
            List<SecurityAttributeResult<R>> responses,
            Map<String, List<Holding>> identifierToHoldings) {

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

    private Map<Holding, D> mapResponseToHoldings(
            SecurityAttributeResult<R> response,
            Map<String, List<Holding>> identifierToHoldings) {

        String responseKey = buildKey(response.getIdentifier());
        List<Holding> holdings = identifierToHoldings.get(responseKey);

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
