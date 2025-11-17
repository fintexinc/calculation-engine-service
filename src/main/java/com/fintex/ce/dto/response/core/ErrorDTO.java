package com.fintex.ce.dto.response.core;

import com.fintex.ce.dto.exception.ErrorRes2DTO;
import lombok.Data;

import java.util.List;

@Data
public class ErrorDTO {

    protected List<ErrorRes2DTO> errors;

}
