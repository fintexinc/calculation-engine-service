package com.fintex.ce.exception;

import com.fintex.ce.dto.exception.ErrorResDTO;
import com.shopify.graphql.support.Error;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

public class GraphqlTransportException extends RuntimeException {

    @Getter
    private List<ErrorResDTO> errors;

    public GraphqlTransportException(final Throwable cause) {
        super(cause);
    }

    public GraphqlTransportException(List<Error> errors, String message) {
        this.errors = errors.stream().map(e -> new ErrorResDTO(message, e.message())).collect(Collectors.toList());
    }

    public GraphqlTransportException(String message) {
        this.errors = List.of(new ErrorResDTO(null, message));
    }

}
