package com.fintex.ce.adapter.webclient.repository.core;

import com.fintex.ce.adapter.webclient.client.SmRestClient;
import com.fintex.ce.adapter.webclient.dto.SmAttributeRequest;
import com.fintex.ce.adapter.webclient.dto.SmAttributeResponse;
import com.fintex.ce.adapter.webclient.dto.SmTypedIdentifiers;
import com.fintex.ce.adapter.webclient.mapper.HoldingTypeMapper;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.port.output.sm.SecurityDataPort;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.ParameterizedTypeReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.groupingBy;

@Log4j2
public abstract class AbstractRestDataFetcher<T, S> implements SecurityDataPort<T> {

  protected final SmRestClient client;

  protected AbstractRestDataFetcher(SmRestClient client) {
    this.client = client;
  }

  protected abstract String endpointPath();

  protected abstract ParameterizedTypeReference<List<SmAttributeResponse<S>>> responseType();

  protected abstract T mapResponse(S smResponse, Holding holding);

  @Override
  public Map<Holding, T> fetch(List<? extends Holding> holdings, List<DataProvider> providers) {
    Map<Holding, T> result = new HashMap<>();

    Map<HoldingType, List<Holding>> grouped = holdings.stream()
        .filter(h -> h.getType() != null && !HoldingTypeMapper.isSkipped(h.getType()))
        .collect(groupingBy(Holding::getType));

    if (grouped.isEmpty()) {
      return result;
    }

    List<SmTypedIdentifiers> typedIdentifiers = new ArrayList<>();
    Map<String, Holding> idToHolding = new HashMap<>();

    grouped.forEach((type, group) -> {
      String fiType = HoldingTypeMapper.toFinancialInstrumentType(type);
      if (fiType == null) {
        return;
      }
      List<com.fintex.sm.model.domain.SecurityIdentifier> ids = new ArrayList<>();
      for (Holding h : group) {
        if (h.getSecurityIdentifier() != null) {
          ids.add(h.getSecurityIdentifier());
          String key = buildKey(h.getSecurityIdentifier());
          idToHolding.put(key, h);
        }
      }
      if (!ids.isEmpty()) {
        typedIdentifiers.add(SmTypedIdentifiers.builder()
            .type(fiType)
            .ids(ids)
            .build());
      }
    });

    if (typedIdentifiers.isEmpty()) {
      return result;
    }

    List<String> providerNames = providers != null
        ? providers.stream().map(Enum::name).toList()
        : List.of();

    SmAttributeRequest request = SmAttributeRequest.builder()
        .typedIdentifiers(typedIdentifiers)
        .dataProviders(providerNames)
        .build();

    List<SmAttributeResponse<S>> responses = client.postAttributes(
        endpointPath(), request, responseType());

    for (SmAttributeResponse<S> resp : responses) {
      if (resp.getIdentifier() == null || resp.getData() == null) {
        continue;
      }
      String key = buildKey(resp.getIdentifier());
      Holding holding = idToHolding.get(key);
      if (holding != null) {
        T mapped = mapResponse(resp.getData(), holding);
        result.put(holding, mapped);
      } else {
        log.warn("No matching holding found for identifier: {}", resp.getIdentifier());
      }
    }

    return result;
  }

  private String buildKey(com.fintex.sm.model.domain.SecurityIdentifier id) {
    return Objects.toString(id.getIdType(), "") + ":" + Objects.toString(id.getId(), "");
  }
}
