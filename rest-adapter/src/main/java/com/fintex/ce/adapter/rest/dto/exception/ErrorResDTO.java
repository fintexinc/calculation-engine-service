package com.fintex.ce.adapter.rest.dto.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorResDTO {

  private String code;
  private String message;

}
