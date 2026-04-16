package com.fintex.ce.model.error.exceptions;

import com.fintex.ce.model.error.ErrorCode;

import lombok.Getter;

@Getter
public class DataErrorException extends RuntimeException {

  private final String id;
  private final ErrorCode code;
  private final int httpStatusCode = 200;

  public DataErrorException(final String message, final String id, final ErrorCode code) {
    super(message);
    this.id = id;
    this.code = code;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    DataErrorException that = (DataErrorException) o;

    if (id != null ? !id.equals(that.id) : that.id != null) return false;
    if (code != that.code) return false;
    if (!getMessage().equals(that.getMessage())) return false;
    return httpStatusCode == that.httpStatusCode;
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (code != null ? code.hashCode() : 0);
    result = 31 * result + httpStatusCode;
    result = 31 * result + (getMessage() != null ? getMessage().hashCode() : 0);
    return result;
  }
}
