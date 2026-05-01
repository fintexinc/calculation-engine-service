package com.fintex.ce.model.domain.result;

import com.fintex.ce.model.error.Warning;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Base response for all calculation metrics. Carries optional warnings produced during the calculation.")
public abstract class BaseCalculationResult {

  @Schema(description = "List of warnings encountered during the calculation")
  @Builder.Default
  protected List<Warning> warnings = new ArrayList<>();
}