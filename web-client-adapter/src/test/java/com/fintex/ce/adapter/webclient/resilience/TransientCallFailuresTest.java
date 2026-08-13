package com.fintex.ce.adapter.webclient.resilience;

import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pins the classification the retry and the circuit breaker share, because both wrong answers are damaging in opposite
 * directions: treating a rejected request as transient burns attempts and opens a breaker over this service's own bug,
 * while treating a server error as permanent gives up on a provider that would have answered the next call.
 */
class TransientCallFailuresTest {

  private final TransientCallFailures transientFailures = new TransientCallFailures();

  @ParameterizedTest
  @ValueSource(ints = {500, 502, 503, 504, 408, 429})
  void shouldTreatAsTransient_whenTheProviderReportsAFailureItMayRecoverFrom(int status) {
    assertThat(transientFailures.test(responseException(status))).isTrue();
  }

  @ParameterizedTest
  @ValueSource(ints = {400, 401, 403, 404, 422})
  void shouldTreatAsPermanent_whenTheProviderRejectsTheRequestItself(int status) {
    assertThat(transientFailures.test(responseException(status))).isFalse();
  }

  @Test
  void shouldTreatAsTransient_whenTheRequestNeverReachedTheProvider() {
    assertThat(transientFailures.test(new WebClientRequestException(
        new IOException("connection reset"), HttpMethod.GET, URI.create("https://provider.test/rates"),
        HttpHeaders.EMPTY))).isTrue();
  }

  @Test
  void shouldTreatAsTransient_whenTheProviderTimedOut() {
    assertThat(transientFailures.test(new TimeoutException("response timed out"))).isTrue();
    assertThat(transientFailures.test(new HttpTimeoutException("request timed out"))).isTrue();
  }

  @Test
  void shouldTreatAsTransient_whenATransientFailureIsWrappedDeeper() {
    assertThat(transientFailures.test(new IllegalStateException("wrapped", new IOException("connection reset"))))
        .isTrue();
  }

  /**
   * The wrapped cause is what a real decode failure carries, and Jackson's parse exceptions are {@link IOException}s,
   * so a classifier that reached its transport branch first would call a malformed payload transient and retry it.
   */
  @Test
  void shouldTreatAsPermanent_whenTheResponseCouldNotBeRead() {
    assertThat(transientFailures.test(new DecodingException("unexpected token"))).isFalse();
    assertThat(transientFailures.test(new DecodingException("unexpected token", jsonParseFailure()))).isFalse();
  }

  @Test
  void shouldTreatAsPermanent_whenNoRequestWasEverSentBecauseTheBreakerIsOpen() {
    assertThat(transientFailures.test(CallNotPermittedException.createCallNotPermittedException(
        CircuitBreaker.ofDefaults("securityMaster")))).isFalse();
  }

  @Test
  void shouldTreatAsPermanent_whenACauseChainRefersBackToItself() {
    SelfReferencingException selfReferencing = new SelfReferencingException();

    assertThat(transientFailures.test(selfReferencing)).isFalse();
  }

  private static WebClientResponseException responseException(int status) {
    return WebClientResponseException.create(status, HttpStatus.valueOf(status).getReasonPhrase(),
        HttpHeaders.EMPTY, new byte[0], StandardCharsets.UTF_8);
  }

  private static JsonProcessingException jsonParseFailure() {
    try {
      new ObjectMapper().readTree("{\"value\":");
      throw new AssertionError("expected malformed JSON to fail parsing");
    } catch (JsonProcessingException expected) {
      return expected;
    }
  }

  private static final class SelfReferencingException extends RuntimeException {

    @Override
    public synchronized Throwable getCause() {
      return this;
    }
  }
}
