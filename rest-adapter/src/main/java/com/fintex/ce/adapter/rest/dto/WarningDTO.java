package com.fintex.ce.adapter.rest.dto;

import com.fintex.ce.model.error.Warning;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(description = "Base response for all calculation metrics. Carries optional warnings produced during the calculation.")
public class WarningDTO {

  @Schema(description = "List of warnings encountered during the calculation")
  protected List<Warning> warnings = new ArrayList<>();

  public WarningDTO() {
  }

  public WarningDTO(final List<Warning> warnings) {
    this.warnings = warnings;
  }
}
