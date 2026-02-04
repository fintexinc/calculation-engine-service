package com.fintex.ce.framework.exceptions;

public class InternalTestGeneralException extends RuntimeException {
  private static final String MESSAGE = "Internal test general exception";

  public InternalTestGeneralException() {
    super(MESSAGE);
  }

  public InternalTestGeneralException(String message) {
    super(message);
  }

  public InternalTestGeneralException(String message, Throwable cause) {
    super(message, cause);
  }
}
