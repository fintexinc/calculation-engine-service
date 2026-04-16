package com.fintex.ce.adapter.webclient.boc.exception;

import org.springframework.http.HttpStatusCode;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import lombok.Getter;

@Getter
public class BankOfCanadaWebClientException extends WebClientResponseException {

  private final String responseBody;

  public BankOfCanadaWebClientException(HttpStatusCode statusCode, String responseBody) {
    super(
        statusCode.value(),
        "Bank of Canada API Error: " + responseBody,
        null,
        null,
        null);
    this.responseBody = responseBody != null ? responseBody : "";
  }

  @NonNull
  @Override
  public String getMessage() {
    return "Bank of Canada API Error [" + getStatusCode() + "]: " + responseBody;
  }
}