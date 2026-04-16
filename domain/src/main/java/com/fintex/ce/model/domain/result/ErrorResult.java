package com.fintex.ce.model.domain.result;

import com.fintex.ce.model.error.ValidationError;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public abstract class ErrorResult {

  protected List<ValidationError> errors = new ArrayList<>();
}