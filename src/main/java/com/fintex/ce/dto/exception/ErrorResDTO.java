package com.fintex.ce.dto.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorResDTO {

    private String code;
    private String message;

}
