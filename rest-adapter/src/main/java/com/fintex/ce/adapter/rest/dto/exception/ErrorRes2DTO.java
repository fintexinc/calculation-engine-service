package com.fintex.ce.adapter.rest.dto.exception;

import com.fintex.ce.model.error.exceptions.DataErrorException;

import lombok.Data;

@Data
public class ErrorRes2DTO {

  private String id;
  private String errorCode;
  private String message;

  public ErrorRes2DTO(DataErrorException err) {
    this.id = err.getId();
    this.errorCode = err.getCode().name();
    this.message = err.getMessage();
  }

  public ErrorRes2DTO(String errorCode, String message) {
    this.errorCode = errorCode;
    this.message = message;
  }

  public ErrorRes2DTO(String id, String errorCode, String message) {
    this.id = id;
    this.errorCode = errorCode;
    this.message = message;
  }

}
