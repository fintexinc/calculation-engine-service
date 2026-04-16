package com.fintex.ce.model.error;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a validation error in domain models. This is a domain-level representation that gets converted to/from
 * infrastructure-specific error types (like DataErrorException) at boundaries.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidationError {

  private String id;
  private String code;
  private String message;

}
