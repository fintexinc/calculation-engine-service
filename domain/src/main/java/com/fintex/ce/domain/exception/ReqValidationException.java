package com.fintex.ce.domain.exception;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class ReqValidationException extends GeneralRuntimeException {

  private String id;
  private String code;
  private List<ReqValidationException> reqValidationExceptions;

  public ReqValidationException(String message) {
    super(message);
  }

  public ReqValidationException(List<ReqValidationException> exceptionList) {
    super("Multiple errors");
    reqValidationExceptions = exceptionList;
  }

  public ReqValidationException(String message, Throwable cause) {
    super(message, cause);
  }

  public ReqValidationException(String code, String message) {
    super(message);
    this.code = code;
  }

  public ReqValidationException(String message, String id, String code) {
    super(message);
    this.id = id;
    this.code = code;
  }

  public ReqValidationException(String code, String message, Throwable cause) {
    super(message, cause);
    this.code = code;
  }

}
