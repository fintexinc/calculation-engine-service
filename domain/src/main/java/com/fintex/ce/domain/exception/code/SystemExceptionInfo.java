package com.fintex.ce.domain.exception.code;

public interface SystemExceptionInfo {
  String getMessage();
  String getErrorEnum();
  ErrorCode getErrorCode();
}
