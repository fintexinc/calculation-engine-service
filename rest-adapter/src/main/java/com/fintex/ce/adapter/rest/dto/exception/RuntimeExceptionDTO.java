package com.fintex.ce.adapter.rest.dto.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RuntimeExceptionDTO {

  private List<?> errors;

}
