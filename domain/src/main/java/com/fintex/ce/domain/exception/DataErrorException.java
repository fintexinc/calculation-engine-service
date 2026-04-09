package com.fintex.ce.domain.exception;

import com.fintex.ce.domain.model.enumeration.ExceptionCode;

import lombok.Getter;

@Getter
public class DataErrorException extends GeneralRuntimeException {

  private final String id;
  private final ExceptionCode code;
  private final int httpStatusCode = 200;

  public DataErrorException(final String message, final String id, final ExceptionCode code) {
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
