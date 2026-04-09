package com.fintex.ce.domain.exception;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

/**
 * Exception for GraphQL transport errors. Stores error messages without external dependencies.
 */
public class GraphqlTransportException extends RuntimeException {

  @Getter
  private final List<String> errorMessages;

  public GraphqlTransportException(final Throwable cause) {
    super(cause);
    this.errorMessages = new ArrayList<>();
  }

  public GraphqlTransportException(List<String> errorMessages, String message) {
    super(message);
    this.errorMessages = errorMessages;
  }

  public GraphqlTransportException(String message) {
    super(message);
    this.errorMessages = List.of(message);
  }
}
