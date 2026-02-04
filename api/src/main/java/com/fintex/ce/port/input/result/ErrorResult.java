package com.fintex.ce.port.input.result;

import com.fintex.ce.domain.model.ValidationError;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public abstract class ErrorResult {

  protected List<ValidationError> errors = new ArrayList<>();
}