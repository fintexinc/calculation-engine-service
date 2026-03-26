package com.fintex.ce.adapter.webclient.sm.fetcher;

import com.fintex.ce.adapter.webclient.mapper.HoldingTypeMapper;
import com.fintex.ce.adapter.webclient.sm.client.SecurityMasterWebClient;
import com.fintex.ce.adapter.webclient.sm.dto.IdsAndDataProvidersRequest;
import com.fintex.ce.adapter.webclient.sm.dto.IdsAndDataProvidersRequest.TypedIdentifiers;
import com.fintex.ce.adapter.webclient.sm.dto.SecurityAttributeResult;
import com.fintex.ce.adapter.webclient.sm.mapper.SecurityMasterResponseMapper;
import com.fintex.ce.domain.model.enumeration.DataProvider;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.sm.model.domain.SecurityIdentifier;
import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.util.CollectionUtils;
import static java.util.stream.Collectors.groupingBy;

/**
 * Abstract base class for fetching data from Security Master REST API.
 *
 * @param <DomainModel> Domain model type returned to the application (e.g., AssetAllocation)
 * @param <SmsResponse> SMS API response type that will be mapped to domain model
 */
@Slf4j
public abstract class AbstractSecurityMasterFetcher<DomainModel, SmsResponse> implements SecurityDataFetcher<DomainModel> {

    private static final String KEY_SEPARATOR = ":";

    protected final SecurityMasterWebClient client;

    protected AbstractSecurityMasterFetcher(SecurityMasterWebClient client) {
        this.client = client;
    }

    /**
     * Returns the endpoint path for this fetcher.
     */
    protected abstract String endpointPath();

    /**
     * Returns the parameterized type reference for deserializing the response.
     */
    protected abstract ParameterizedTypeReference<List<SecurityAttributeResult<SmsResponse>>> responseType();

    /**
     * Returns the mapper for converting SMS response to domain model.
     */
    protected abstract SecurityMasterResponseMapper<DomainModel, SmsResponse> responseMapper();

    @Override
    public Map<Holding, DomainModel> fetch(List<? extends Holding> holdings, List<DataProvider> providers) {
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
            log.debug("No valid identifiers to fetch for endpoint: {}", endpointPath());
            return Collections.emptyMap();
        }

        IdsAndDataProvidersRequest request = IdsAndDataProvidersRequest.builder()
                .typedIdentifiers(typedIdentifiers)
                .dataProviders(providers)
                .build();

        List<SecurityAttributeResult<SmsResponse>> responses = client.post(
                endpointPath(),
                request,
                responseType());

        Map<Holding, DomainModel> result = mapResponsesToHoldings(
                responses != null ? responses : List.of(),
                identifierToHoldings);

        log.debug("Fetched {} results for {} holdings from endpoint: {}",
                result.size(), holdings.size(), endpointPath());

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

    private Map<Holding, DomainModel> mapResponsesToHoldings(
            List<SecurityAttributeResult<SmsResponse>> responses,
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

    private Map<Holding, DomainModel> mapResponseToHoldings(
            SecurityAttributeResult<SmsResponse> response,
            Map<String, List<Holding>> identifierToHoldings) {

        String key = buildKey(response.getIdentifier());
        List<Holding> holdings = identifierToHoldings.get(key);

        if (CollectionUtils.isEmpty(holdings)) {
            log.warn("No matching holdings for identifier: {}", response.getIdentifier());
            return Collections.emptyMap();
        }

        SecurityMasterResponseMapper<DomainModel, SmsResponse> mapper = responseMapper();
        return holdings.stream()
                .map(holding -> {
                    DomainModel mapped = mapper.map(response.getData(), holding);
                    return mapped != null ? Map.entry(holding, mapped) : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private boolean isValidResponse(SecurityAttributeResult<SmsResponse> response) {
        return response != null && response.getIdentifier() != null && response.getData() != null;
    }

    private String buildKey(SecurityIdentifier identifier) {
        return Objects.toString(identifier.getIdType(), "") + KEY_SEPARATOR + Objects.toString(identifier.getId(), "");
    }
}
