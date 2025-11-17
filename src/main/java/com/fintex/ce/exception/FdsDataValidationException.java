package com.fintex.ce.exception;

import lombok.Data;

import java.util.List;

@Data
public class FdsDataValidationException extends RuntimeException {
    List<DataErrorException> exceptionList;

    public FdsDataValidationException(List<DataErrorException> exceptionList) {
        super(exceptionList.get(0));
        this.exceptionList = exceptionList;
    }

}
