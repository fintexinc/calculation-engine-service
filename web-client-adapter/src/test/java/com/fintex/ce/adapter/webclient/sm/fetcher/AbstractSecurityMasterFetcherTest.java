package com.fintex.ce.adapter.webclient.sm.fetcher;

import com.fintex.ce.adapter.webclient.sm.client.SecurityMasterWebClient;
import com.fintex.ce.adapter.webclient.sm.mapper.SecurityMasterResponseMapper;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.attribute.SecurityAttributeResult;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import org.springframework.core.ParameterizedTypeReference;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;

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
 * Abstract test class for SecurityMasterFetcher implementations. Subclasses provide the concrete fetcher, SM response,
 * and domain model instances.
 *
 * @param <D>
 *          CE domain model type
 * @param <R>
 *          SM API response type
 */
abstract class AbstractSecurityMasterFetcherTest<D, R> {

  @Mock
  protected SecurityMasterWebClient securityMasterWebClient;

  protected abstract AbstractSecurityMasterFetcher<D, R> fetcher();

  protected abstract String expectedEndpointPath();

  protected abstract R createSmsResponse();

  protected abstract D createExpectedDomainModel(String holdingId);

  protected abstract SecurityMasterResponseMapper<D, R> mapper();

  @Test
  void shouldReturnMappedResults_whenSmReturnsData() {
    PortfolioHolding holding = createHolding("XIU.TO", FiIdentifierType.TICKER, FinancialInstrumentType.ETF_CANADA);
    R smsResponse = createSmsResponse();
    D expected = createExpectedDomainModel("XIU.TO");

    var identifier = new SecurityIdentifier();
    identifier.setId("XIU.TO");
    identifier.setIdType(FiIdentifierType.TICKER);

    when(securityMasterWebClient.post(eq(expectedEndpointPath()), any(), any(ParameterizedTypeReference.class)))
        .thenReturn(List.of(securityAttributeResult(identifier, smsResponse)));
    when(mapper().map(smsResponse, holding)).thenReturn(expected);

    Map<PortfolioHolding, D> result = fetcher().fetch(
        List.of(holding), List.of(DataProvider.MORNINGSTAR));

    assertThat(result).hasSize(1);
    assertThat(result).containsKey(holding);
    assertThat(result.get(holding)).isEqualTo(expected);
    verify(securityMasterWebClient).post(eq(expectedEndpointPath()), any(), any(ParameterizedTypeReference.class));
    verify(mapper()).map(smsResponse, holding);
  }

  @Test
  void shouldReturnEmptyMap_whenHoldingsListIsEmpty() {
    Map<PortfolioHolding, D> result = fetcher().fetch(
        Collections.emptyList(), List.of(DataProvider.MORNINGSTAR));

    assertThat(result).isEmpty();
    verifyNoInteractions(securityMasterWebClient);
  }

  @Test
  void shouldReturnEmptyMap_whenHoldingsListIsNull() {
    Map<PortfolioHolding, D> result = fetcher().fetch(
        null, List.of(DataProvider.MORNINGSTAR));

    assertThat(result).isEmpty();
    verifyNoInteractions(securityMasterWebClient);
  }

  @Test
  void shouldReturnEmptyMap_whenSmReturnsNull() {
    PortfolioHolding holding = createHolding("VFV.TO", FiIdentifierType.TICKER, FinancialInstrumentType.ETF_CANADA);

    when(securityMasterWebClient.post(eq(expectedEndpointPath()), any(), any(ParameterizedTypeReference.class)))
        .thenReturn(null);

    Map<PortfolioHolding, D> result = fetcher().fetch(
        List.of(holding), List.of(DataProvider.MORNINGSTAR));

    assertThat(result).isEmpty();
  }

  @Test
  void shouldReturnEmptyMap_whenSmReturnsEmptyList() {
    PortfolioHolding holding = createHolding("VFV.TO", FiIdentifierType.TICKER, FinancialInstrumentType.ETF_CANADA);

    when(securityMasterWebClient.post(eq(expectedEndpointPath()), any(), any(ParameterizedTypeReference.class)))
        .thenReturn(Collections.emptyList());

    Map<PortfolioHolding, D> result = fetcher().fetch(
        List.of(holding), List.of(DataProvider.MORNINGSTAR));

    assertThat(result).isEmpty();
  }

  @Test
  void shouldSkipHoldingsWithNullHoldingType() {
    PortfolioHolding validHolding = createHolding("XIU.TO", FiIdentifierType.TICKER,
        FinancialInstrumentType.ETF_CANADA);
    PortfolioHolding nullTypeHolding = createHolding("SKIP.ID", FiIdentifierType.TICKER, null);

    R smsResponse = createSmsResponse();
    D expected = createExpectedDomainModel("XIU.TO");

    var identifier = new SecurityIdentifier();
    identifier.setId("XIU.TO");
    identifier.setIdType(FiIdentifierType.TICKER);

    when(securityMasterWebClient.post(eq(expectedEndpointPath()), any(), any(ParameterizedTypeReference.class)))
        .thenReturn(List.of(securityAttributeResult(identifier, smsResponse)));
    when(mapper().map(smsResponse, validHolding)).thenReturn(expected);

    Map<PortfolioHolding, D> result = fetcher().fetch(
        List.of(validHolding, nullTypeHolding), List.of(DataProvider.MORNINGSTAR));

    assertThat(result).hasSize(1);
    assertThat(result).containsKey(validHolding);
    assertThat(result).doesNotContainKey(nullTypeHolding);
  }

  @Test
  void shouldMapMultipleHoldingsFromSameResponse() {
    PortfolioHolding holding1 = createHolding("XIU.TO", FiIdentifierType.TICKER, FinancialInstrumentType.ETF_CANADA);
    PortfolioHolding holding2 = createHolding("VFV.TO", FiIdentifierType.TICKER, FinancialInstrumentType.ETF_CANADA);

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

    when(securityMasterWebClient.post(eq(expectedEndpointPath()), any(), any(ParameterizedTypeReference.class)))
        .thenReturn(List.of(
            securityAttributeResult(id1, smsResponse1),
            securityAttributeResult(id2, smsResponse2)));
    when(mapper().map(smsResponse1, holding1)).thenReturn(expected1);
    when(mapper().map(smsResponse2, holding2)).thenReturn(expected2);

    Map<PortfolioHolding, D> result = fetcher().fetch(
        List.of(holding1, holding2), List.of(DataProvider.MORNINGSTAR));

    assertThat(result).hasSize(2);
    assertThat(result.get(holding1)).isEqualTo(expected1);
    assertThat(result.get(holding2)).isEqualTo(expected2);
  }

  protected PortfolioHolding createHolding(String id, FiIdentifierType idType, FinancialInstrumentType holdingType) {
    var identifier = new SecurityIdentifier();
    identifier.setId(id);
    identifier.setIdType(idType);
    return new PortfolioHolding(null, holdingType, identifier);
  }

  private static <T> SecurityAttributeResult<T> securityAttributeResult(SecurityIdentifier identifier, T data) {
    SecurityAttributeResult<T> result = new SecurityAttributeResult<>();
    result.setIdentifier(identifier);
    result.setData(data);
    return result;
  }
}