package com.fintex.ce.adapter.rest.dto.exception;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RuntimeExceptionDTO {

  private List<?> errors;

}
