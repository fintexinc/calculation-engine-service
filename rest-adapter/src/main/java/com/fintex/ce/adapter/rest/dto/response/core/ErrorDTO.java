package com.fintex.ce.adapter.rest.dto.response.core;

import com.fintex.ce.adapter.rest.dto.exception.ErrorRes2DTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;

@Data
@Schema(description = "Base response containing error information.")
public class ErrorDTO {

  @Schema(description = "List of errors encountered during calculation")
  protected List<ErrorRes2DTO> errors;

}
