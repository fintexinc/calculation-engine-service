package com.fintex.ce.adapter.webclient.sm.fetcher;

import com.fintex.ce.adapter.webclient.sm.client.SecurityMasterWebClient;
import com.fintex.ce.adapter.webclient.sm.dto.SecurityAttributeResult;
import com.fintex.ce.adapter.webclient.sm.mapper.SecurityMasterResponseMapper;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.sm.model.DataProvider;
import com.fintex.sm.model.domain.SecurityIdentifier;
import com.fintex.sm.model.domain.enumeration.FiIdentifierType;
import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.core.ParameterizedTypeReference;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Abstract test class for SecurityMasterFetcher implementations.
 * Subclasses provide the concrete fetcher, SM response, and domain model instances.
 *
 * @param <D> CE domain model type
 * @param <R> SM API response type
 */
abstract class AbstractSecurityMasterFetcherTest<D, R> {

  @Mock
  protected SecurityMasterWebClient client;

  protected abstract AbstractSecurityMasterFetcher<D, R> fetcher();

  protected abstract String expectedEndpointPath();

  protected abstract R createSmsResponse();

  protected abstract D createExpectedDomainModel(String holdingId);

  protected abstract SecurityMasterResponseMapper<D, R> mapper();

  @Test
  void shouldReturnMappedResults_whenSmReturnsData() {
    Holding holding = createHolding("XIU.TO", FiIdentifierType.TICKER, FinancialInstrumentType.ETF_CANADA);
    R smsResponse = createSmsResponse();
    D expected = createExpectedDomainModel("XIU.TO");

    var identifier = new SecurityIdentifier();
    identifier.setId("XIU.TO");
    identifier.setIdType(FiIdentifierType.TICKER);

    when(client.post(eq(expectedEndpointPath()), any(), any(ParameterizedTypeReference.class)))
        .thenReturn(List.of(new SecurityAttributeResult<>(identifier, smsResponse)));
    when(mapper().map(smsResponse, holding)).thenReturn(expected);

    Map<Holding, D> result = fetcher().fetch(
        List.of(holding), List.of(DataProvider.MORNINGSTAR));

    assertThat(result).hasSize(1);
    assertThat(result).containsKey(holding);
    assertThat(result.get(holding)).isEqualTo(expected);
    verify(client).post(eq(expectedEndpointPath()), any(), any(ParameterizedTypeReference.class));
    verify(mapper()).map(smsResponse, holding);
  }

  @Test
  void shouldReturnEmptyMap_whenHoldingsListIsEmpty() {
    Map<Holding, D> result = fetcher().fetch(
        Collections.emptyList(), List.of(DataProvider.MORNINGSTAR));

    assertThat(result).isEmpty();
    verifyNoInteractions(client);
  }

  @Test
  void shouldReturnEmptyMap_whenHoldingsListIsNull() {
    Map<Holding, D> result = fetcher().fetch(
        null, List.of(DataProvider.MORNINGSTAR));

    assertThat(result).isEmpty();
    verifyNoInteractions(client);
  }

  @Test
  void shouldReturnEmptyMap_whenSmReturnsNull() {
    Holding holding = createHolding("VFV.TO", FiIdentifierType.TICKER, FinancialInstrumentType.ETF_CANADA);

    when(client.post(eq(expectedEndpointPath()), any(), any(ParameterizedTypeReference.class)))
        .thenReturn(null);

    Map<Holding, D> result = fetcher().fetch(
        List.of(holding), List.of(DataProvider.MORNINGSTAR));

    assertThat(result).isEmpty();
  }

  @Test
  void shouldReturnEmptyMap_whenSmReturnsEmptyList() {
    Holding holding = createHolding("VFV.TO", FiIdentifierType.TICKER, FinancialInstrumentType.ETF_CANADA);

    when(client.post(eq(expectedEndpointPath()), any(), any(ParameterizedTypeReference.class)))
        .thenReturn(Collections.emptyList());

    Map<Holding, D> result = fetcher().fetch(
        List.of(holding), List.of(DataProvider.MORNINGSTAR));

    assertThat(result).isEmpty();
  }

  @Test
  void shouldSkipHoldingsWithNullHoldingType() {
    Holding validHolding = createHolding("XIU.TO", FiIdentifierType.TICKER, FinancialInstrumentType.ETF_CANADA);
    Holding nullTypeHolding = createHolding("SKIP.ID", FiIdentifierType.TICKER, null);

    R smsResponse = createSmsResponse();
    D expected = createExpectedDomainModel("XIU.TO");

    var identifier = new SecurityIdentifier();
    identifier.setId("XIU.TO");
    identifier.setIdType(FiIdentifierType.TICKER);

    when(client.post(eq(expectedEndpointPath()), any(), any(ParameterizedTypeReference.class)))
        .thenReturn(List.of(new SecurityAttributeResult<>(identifier, smsResponse)));
    when(mapper().map(smsResponse, validHolding)).thenReturn(expected);

    Map<Holding, D> result = fetcher().fetch(
        List.of(validHolding, nullTypeHolding), List.of(DataProvider.MORNINGSTAR));

    assertThat(result).hasSize(1);
    assertThat(result).containsKey(validHolding);
    assertThat(result).doesNotContainKey(nullTypeHolding);
  }

  @Test
  void shouldMapMultipleHoldingsFromSameResponse() {
    Holding holding1 = createHolding("XIU.TO", FiIdentifierType.TICKER, FinancialInstrumentType.ETF_CANADA);
    Holding holding2 = createHolding("VFV.TO", FiIdentifierType.TICKER, FinancialInstrumentType.ETF_CANADA);

    R smsResponse1 = createSmsResponse();
    R smsResponse2 = createSmsResponse();
    D expected1 = createExpectedDomainModel("XIU.TO");
    D expected2 = createExpectedDomainModel("VFV.TO");

    var id1 = new SecurityIdentifier();
    id1.setId("XIU.TO");
    id1.setIdType(FiIdentifierType.TICKER);
    var id2 = new SecurityIdentifier();
    id2.setId("VFV.TO");
    id2.setIdType(FiIdentifierType.TICKER);

    when(client.post(eq(expectedEndpointPath()), any(), any(ParameterizedTypeReference.class)))
        .thenReturn(List.of(
            new SecurityAttributeResult<>(id1, smsResponse1),
            new SecurityAttributeResult<>(id2, smsResponse2)));
    when(mapper().map(smsResponse1, holding1)).thenReturn(expected1);
    when(mapper().map(smsResponse2, holding2)).thenReturn(expected2);

    Map<Holding, D> result = fetcher().fetch(
        List.of(holding1, holding2), List.of(DataProvider.MORNINGSTAR));

    assertThat(result).hasSize(2);
    assertThat(result.get(holding1)).isEqualTo(expected1);
    assertThat(result.get(holding2)).isEqualTo(expected2);
  }

  protected Holding createHolding(String id, FiIdentifierType idType, FinancialInstrumentType holdingType) {
    var identifier = new SecurityIdentifier();
    identifier.setId(id);
    identifier.setIdType(idType);
    return new Holding(null, holdingType, identifier);
  }
}