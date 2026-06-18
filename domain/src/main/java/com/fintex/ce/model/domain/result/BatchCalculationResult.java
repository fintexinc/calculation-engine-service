package com.fintex.ce.model.domain.result;

import com.fintex.wm.commons.error.Notification;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Batch calculation response. Successful per-metric results and per-metric errors are both returned; "
    + "one failing metric does not suppress others.")
public class BatchCalculationResult {

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  @Schema(description = "Successful results keyed by metric name (e.g. \"trailing-total-returns\")")
  private Map<String, BaseCalculationResult> results;

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  @Schema(description = "Per-metric errors keyed by metric name; absent when all metrics succeed")
  private Map<String, List<Notification>> errors;
}
