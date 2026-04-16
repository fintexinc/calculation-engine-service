package com.fintex.ce.model.error.exceptions;

import java.util.List;
import lombok.Data;

@Data
public class FdsDataValidationException extends RuntimeException {
  List<DataErrorException> exceptionList;

  public FdsDataValidationException(List<DataErrorException> exceptionList) {
    super(exceptionList.get(0));
    this.exceptionList = exceptionList;
  }

}
