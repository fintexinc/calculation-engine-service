package com.fintex.ce.adapter.rest.dto.response.core;

import com.fintex.ce.adapter.rest.dto.exception.ErrorRes2DTO;
import lombok.Data;

import java.util.List;

@Data
public class ErrorDTO {

  protected List<ErrorRes2DTO> errors;

}
