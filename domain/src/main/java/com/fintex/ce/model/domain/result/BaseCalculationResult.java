package com.fintex.ce.model.domain.result;

import com.fintex.ce.model.error.Warning;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
@Schema(description = "Base response for all calculation metrics. Carries optional warnings produced during the calculation.")
public abstract class BaseCalculationResult {

  @Schema(description = "List of warnings encountered during the calculation")
  protected List<Warning> warnings = new ArrayList<>();
}
