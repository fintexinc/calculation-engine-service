package com.fintex.ce.adapter.webclient.sm.integration;

import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.attribute.SecurityAttributeResult;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.EquitySecurityIdentifier;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;
import com.fintex.wm.commons.dto.request.IdsAndDataProvidersRequest;
import com.fintex.wm.commons.dto.search.TypedIdentifiers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

/**
 * HTTP-level tests for SM fetchers using {@link SecurityMasterWebClientIntegrationTestConfiguration} (see its Javadoc
 * for why the slice exists). OkHttp {@link MockWebServer} has neither {@code getBaseUrl()} nor {@code baseUrl()} — only
 * {@link MockWebServer#url(String)} returning {@link okhttp3.HttpUrl}. {@link #smsMockBaseUrl()} wraps that (no
 * trailing slash) and is used as the {@link DynamicPropertySource} supplier; {@code smsMockServer::baseUrl} cannot
 * compile.
 */
@Tag("integration")
@SpringBootTest(classes = SecurityMasterWebClientIntegrationTestConfiguration.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
abstract class AbstractSecurityDataFetcherTest<D, R> {

  private static final String basePath = "/api/v1/wealth/securities";

  private static final BigDecimal testHoldingValue = BigDecimal.ONE;

  private static MockWebServer smsMockServer;

  private static void ensureSmsMockServerStarted() throws IOException {
    if (smsMockServer == null) {
      smsMockServer = new MockWebServer();
      smsMockServer.start();
    }
  }

  @BeforeAll
  static void startSmsMockServerBeforeAll() throws IOException {
    ensureSmsMockServerStarted();
  }

  @AfterAll
  static void shutdownSmsMockServer() throws IOException {
    if (smsMockServer != null) {
      smsMockServer.shutdown();
      smsMockServer = null;
    }
  }

  private static String smsMockBaseUrl() {
    try {
      ensureSmsMockServerStarted();
      return smsMockServer.url("/").toString().replaceAll("/$", "");
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  @DynamicPropertySource
  static void registerSecurityMasterBaseUrl(DynamicPropertyRegistry registry) {
    // Not smsMockServer::baseUrl — MockWebServer has no baseUrl(); supplier also lazy-starts the server for context
    // init.
    registry.add("external-services.security-master.rest.base-url", AbstractSecurityDataFetcherTest::smsMockBaseUrl);
  }

  @Autowired
  protected ObjectMapper objectMapper;

  protected abstract SecurityDataFetcher<D> fetcherUnderTest();

  protected abstract String endpointPath();

  protected abstract List<PortfolioHolding> holdingsForComplexScenario();

  protected List<DataProvider> providersForComplexScenario() {
    return List.of(DataProvider.MORNINGSTAR);
  }

  protected abstract List<SecurityAttributeResult<R>> smsResponseForComplexScenario();

  protected abstract void assertComplexScenario(Map<PortfolioHolding, D> result);

  protected abstract PortfolioHolding holdingForEmptyResponseScenario();

  protected abstract SecurityAttributeResult<R> responseForIdentifierNotPresentInRequest();

  @Test
  void shouldNotIssueHttpRequest_whenHoldingsListIsEmpty() throws Exception {
    Map<PortfolioHolding, D> result = fetcherUnderTest().fetch(List.of(), providersForComplexScenario());

    assertThat(result).isEmpty();
    assertThat(smsMockServer.takeRequest(200, TimeUnit.MILLISECONDS)).isNull();
  }

  @Test
  void shouldReturnEmptyMap_whenSmsReturnsEmptyJsonArray() throws Exception {
    PortfolioHolding holding = holdingForEmptyResponseScenario();
    smsMockServer.enqueue(
        new MockResponse()
            .setBody("[]")
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

    Map<PortfolioHolding, D> result = fetcherUnderTest().fetch(List.of(holding), providersForComplexScenario());

    RecordedRequest recorded = smsMockServer.takeRequest();
    assertThat(recorded.getMethod()).isEqualTo("POST");
    assertThat(recorded.getPath()).isEqualTo(basePath + endpointPath());
    assertThat(result).isEmpty();
  }

  @Test
  void shouldPostTypedIdentifiersAndMapHoldings_whenSmsReturnsMatchingAttributes() throws Exception {
    List<PortfolioHolding> holdings = holdingsForComplexScenario();
    List<SecurityAttributeResult<R>> smsResponses = smsResponseForComplexScenario();

    smsMockServer.enqueue(
        new MockResponse()
            .setBody(objectMapper.writeValueAsString(smsResponses))
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

    Map<PortfolioHolding, D> result = fetcherUnderTest().fetch(holdings, providersForComplexScenario());

    RecordedRequest recorded = smsMockServer.takeRequest();
    assertThat(recorded.getMethod()).isEqualTo("POST");
    assertThat(recorded.getPath()).isEqualTo(basePath + endpointPath());

    IdsAndDataProvidersRequest body = objectMapper.readValue(recorded.getBody().readUtf8(),
        IdsAndDataProvidersRequest.class);

    assertThat(body.getDataProviders()).containsExactlyElementsOf(providersForComplexScenario());
    assertThat(body.getTypedIdentifiers())
        .extracting(TypedIdentifiers::getType)
        .containsExactlyInAnyOrderElementsOf(
            holdings.stream().map(PortfolioHolding::getHoldingType).distinct().toList());

    assertThat(body.getTypedIdentifiers())
        .flatExtracting(TypedIdentifiers::getIds)
        .extracting(SecurityIdentifier::getIdType)
        .containsExactlyInAnyOrderElementsOf(
            holdings.stream().map(h -> h.getSecurityIdentifier().getIdType()).toList());

    assertThat(body.getTypedIdentifiers())
        .flatExtracting(TypedIdentifiers::getIds)
        .extracting(SecurityIdentifier::getId)
        .containsExactlyInAnyOrderElementsOf(
            holdings.stream().map(h -> h.getSecurityIdentifier().getId()).toList());

    assertComplexScenario(result);
  }

  @Test
  void shouldIgnoreExtraSmsRows_whenIdentifierNotRequestedInHoldings() throws Exception {
    List<PortfolioHolding> holdings = holdingsForComplexScenario();

    List<SecurityAttributeResult<R>> smsResponses = new java.util.ArrayList<>(smsResponseForComplexScenario());
    smsResponses.add(responseForIdentifierNotPresentInRequest());

    smsMockServer.enqueue(
        new MockResponse()
            .setBody(objectMapper.writeValueAsString(smsResponses))
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

    Map<PortfolioHolding, D> result = fetcherUnderTest().fetch(holdings, providersForComplexScenario());

    RecordedRequest recorded = smsMockServer.takeRequest();
    assertThat(recorded.getMethod()).isEqualTo("POST");
    assertThat(recorded.getPath()).isEqualTo(basePath + endpointPath());
    assertComplexScenario(result);
  }

  protected static PortfolioHolding createHolding(String id, FiIdentifierType idType,
      FinancialInstrumentType holdingType) {
    return new PortfolioHolding(testHoldingValue, holdingType, new SecurityIdentifier(id, idType));
  }

  protected static PortfolioHolding createEquityHolding(
      String id, FiIdentifierType idType, String exchangeId, FinancialInstrumentType holdingType) {
    EquitySecurityIdentifier identifier = new EquitySecurityIdentifier();
    identifier.setId(id);
    identifier.setIdType(idType);
    identifier.setExchangeId(exchangeId);
    return new PortfolioHolding(testHoldingValue, holdingType, identifier);
  }

  protected static SecurityIdentifier createSecurityIdentifier(String id, FiIdentifierType idType) {
    return new SecurityIdentifier(id, idType);
  }

  protected static <T> SecurityAttributeResult<T> securityAttributeResult(SecurityIdentifier identifier, T data) {
    SecurityAttributeResult<T> result = new SecurityAttributeResult<>();
    result.setIdentifier(identifier);
    result.setData(data);
    return result;
  }

  protected final void enqueueSmsJsonResponse(String jsonBody) {
    smsMockServer.enqueue(
        new MockResponse()
            .setBody(jsonBody)
            .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
  }

  protected final RecordedRequest takeSmsRequest() throws InterruptedException {
    return smsMockServer.takeRequest();
  }
}
