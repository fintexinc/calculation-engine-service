package com.fintex.ce.adapter.webclient.sm.exception;

import org.springframework.http.HttpStatusCode;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import lombok.Getter;

@Getter
public class SecurityMasterWebClientException extends WebClientResponseException {

  private final String responseBody;

  public SecurityMasterWebClientException(HttpStatusCode statusCode, String responseBody) {
    super(
        statusCode.value(),
        "SMS API Error: " + responseBody,
        null,
        null,
        null);
    this.responseBody = responseBody != null ? responseBody : "";
  }

  @NonNull
  @Override
  public String getMessage() {
    return "SMS API Error [" + getStatusCode() + "]: " + responseBody;
  }
}
