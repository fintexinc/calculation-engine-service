package com.fintex.ce.adapter.rest.dto.response.core;

import com.fintex.ce.domain.model.core.Warning;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "Base response containing warning and error information.")
public class WarningDTO extends ErrorDTO {

  @Schema(description = "List of warnings encountered during calculation")
  protected List<Warning> warnings;

  public WarningDTO(List<Warning> warnings) {
    this.warnings = warnings;
  }

  public WarningDTO() {
    warnings = new ArrayList<>();
  }
}
