package ca.tangerine.pce.webclient.mic.fetcher;

import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.pce.model.domain.security.SecurityData;
import ca.tangerine.pce.model.error.exceptions.CalculationException;
import ca.tangerine.pce.webclient.mic.client.MarketInvestmentCatalogueWebClient;
import ca.tangerine.wm.commons.domain.DataProvider;
import ca.tangerine.wm.commons.domain.attribute.SecurityAttributeResult;
import ca.tangerine.wm.commons.domain.enumeration.CompositeSecurityAttribute;
import ca.tangerine.wm.commons.domain.enumeration.Country;
import ca.tangerine.wm.commons.domain.id.FiIdentifierType;
import ca.tangerine.wm.commons.domain.id.SecurityIdentifier;
import ca.tangerine.wm.commons.dto.request.CompositeAttributesRequest;

import org.springframework.core.ParameterizedTypeReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;

import java.util.List;
import java.util.Map;

import static ca.tangerine.pce.util.PortfolioHoldingBuildHelper.etf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompositeMarketInvestmentCatalogueFetcherTest {

  private static final String ENDPOINT = "/api/v1/wealth/securities/attributes";
  private static final List<DataProvider> PROVIDERS = List.of(DataProvider.MORNINGSTAR);

  @Mock
  private MarketInvestmentCatalogueWebClient client;

  private CompositeMarketInvestmentCatalogueFetcher fetcher;

  @BeforeEach
  void setUp() {
    fetcher = new CompositeMarketInvestmentCatalogueFetcher(client, ENDPOINT, new ObjectMapper(), List.of(
        new CompositeAttributeBinding<>(CompositeSecurityAttribute.FEES, String.class, String.class,
            (response, holding) -> "fees:" + response),
        new CompositeAttributeBinding<>(CompositeSecurityAttribute.GEOGRAPHY, String.class, String.class,
            (response, holding) -> "geo:" + response)));
  }

  @Test
  void shouldFetchAndMapAttributes_whenSmReturnsData() {
    PortfolioHolding holding = etf("XIU", Country.CANADA, 1);
    when(client.post(eq(ENDPOINT), any(), any(ParameterizedTypeReference.class))).thenReturn(Map.of(
        CompositeSecurityAttribute.FEES, List.of(attributeResult("XIU", "fee-payload")),
        CompositeSecurityAttribute.GEOGRAPHY, List.of(attributeResult("XIU", "geo-payload"))));

    SecurityData result = fetcher.fetch(
        List.of(holding), List.of(CompositeSecurityAttribute.FEES, CompositeSecurityAttribute.GEOGRAPHY), PROVIDERS);

    assertThat(result.get(CompositeSecurityAttribute.FEES))
        .containsExactlyEntriesOf(Map.of(holding, "fees:fee-payload"));
    assertThat(result.get(CompositeSecurityAttribute.GEOGRAPHY))
        .containsExactlyEntriesOf(Map.of(holding, "geo:geo-payload"));
  }

  @Test
  void shouldSendCompositeAttributesRequest_whenFetching() {
    PortfolioHolding holding = etf("XIU", Country.CANADA, 1);
    when(client.post(eq(ENDPOINT), any(), any(ParameterizedTypeReference.class))).thenReturn(Map.of());

    fetcher.fetch(List.of(holding), List.of(CompositeSecurityAttribute.FEES), PROVIDERS);

    ArgumentCaptor<CompositeAttributesRequest> captor = ArgumentCaptor.forClass(CompositeAttributesRequest.class);
    verify(client).post(eq(ENDPOINT), captor.capture(), any(ParameterizedTypeReference.class));
    CompositeAttributesRequest request = captor.getValue();
    assertThat(request.getAttributes()).containsExactly(CompositeSecurityAttribute.FEES);
    assertThat(request.getDataProviders()).isEqualTo(PROVIDERS);
    assertThat(request.getTypedIdentifiers()).hasSize(1);
    assertThat(request.getTypedIdentifiers().getFirst().getIds())
        .containsExactly(holding.getSecurityIdentifier());
  }

  @Test
  void shouldReturnEmptySecurityData_whenHoldingsEmpty() {
    SecurityData result = fetcher.fetch(List.of(), List.of(CompositeSecurityAttribute.FEES), PROVIDERS);

    assertThat(result.asMap()).isEmpty();
    verifyNoInteractions(client);
  }

  @Test
  void shouldReturnEmptySecurityData_whenAttributesEmpty() {
    SecurityData result = fetcher.fetch(List.of(etf("XIU", Country.CANADA, 1)), List.of(), PROVIDERS);

    assertThat(result.asMap()).isEmpty();
    verifyNoInteractions(client);
  }

  @Test
  void shouldThrowException_whenAttributeHasNoBinding() {
    List<PortfolioHolding> holdings = List.of(etf("XIU", Country.CANADA, 1));
    List<CompositeSecurityAttribute> attributes = List.of(CompositeSecurityAttribute.MONTHLY_RETURNS);

    assertThatThrownBy(() -> fetcher.fetch(holdings, attributes, PROVIDERS))
        .isInstanceOf(CalculationException.class);
    verifyNoInteractions(client);
  }

  @Test
  void shouldIgnoreUnrequestedAttributes_whenSmReturnsMore() {
    PortfolioHolding holding = etf("XIU", Country.CANADA, 1);
    when(client.post(eq(ENDPOINT), any(), any(ParameterizedTypeReference.class))).thenReturn(Map.of(
        CompositeSecurityAttribute.FEES, List.of(attributeResult("XIU", "fee-payload")),
        CompositeSecurityAttribute.GEOGRAPHY, List.of(attributeResult("XIU", "geo-payload"))));

    SecurityData result = fetcher.fetch(List.of(holding), List.of(CompositeSecurityAttribute.FEES), PROVIDERS);

    assertThat(result.asMap()).containsOnlyKeys(CompositeSecurityAttribute.FEES);
  }

  @Test
  void shouldFetchSingleAttributeFromDedicatedEndpoint_whenOneAttributeRequested() {
    PortfolioHolding holding = etf("XIU", Country.CANADA, 1);
    String expectedPath = ENDPOINT + "/" + CompositeSecurityAttribute.FEES.getAttributeName();
    when(client.post(eq(expectedPath), any(), any(ParameterizedTypeReference.class)))
        .thenReturn(List.of(attributeResult("XIU", "fee-payload")));

    Map<PortfolioHolding, String> result = fetcher.fetch(List.of(holding), CompositeSecurityAttribute.FEES,
        PROVIDERS);

    assertThat(result).containsExactlyEntriesOf(Map.of(holding, "fees:fee-payload"));
    verify(client).post(eq(expectedPath), any(), any(ParameterizedTypeReference.class));
  }

  @Test
  void shouldReturnEmptyMap_whenSingleAttributeRequestedForNoHoldings() {
    Map<PortfolioHolding, String> result = fetcher.fetch(List.of(), CompositeSecurityAttribute.FEES, PROVIDERS);

    assertThat(result).isEmpty();
    verifyNoInteractions(client);
  }

  @Test
  void shouldThrowException_whenSingleAttributeHasNoBinding() {
    List<PortfolioHolding> holdings = List.of(etf("XIU", Country.CANADA, 1));

    assertThatThrownBy(() -> fetcher.fetch(holdings, CompositeSecurityAttribute.MONTHLY_RETURNS, PROVIDERS))
        .isInstanceOf(CalculationException.class);
    verifyNoInteractions(client);
  }

  @Test
  void shouldReturnEmptySecurityData_whenSmReturnsNull() {
    when(client.post(eq(ENDPOINT), any(), any(ParameterizedTypeReference.class))).thenReturn(null);

    SecurityData result = fetcher.fetch(
        List.of(etf("XIU", Country.CANADA, 1)), List.of(CompositeSecurityAttribute.FEES), PROVIDERS);

    assertThat(result.asMap()).isEmpty();
  }

  private static SecurityAttributeResult<JsonNode> attributeResult(String ticker, String payload) {
    SecurityAttributeResult<JsonNode> result = new SecurityAttributeResult<>();
    result.setIdentifier(new SecurityIdentifier(ticker, FiIdentifierType.TICKER));
    result.setData(TextNode.valueOf(payload));
    return result;
  }

}
