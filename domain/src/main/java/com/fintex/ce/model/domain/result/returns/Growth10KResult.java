package com.fintex.ce.model.domain.result.returns;

import com.fintex.ce.model.domain.result.BaseCalculationResult;
import com.fintex.ce.model.domain.result.KeyValueResult;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Accessors(chain = true)
@Schema(description = "Response for growth-of-10k metric. Contains growth of a hypothetical $10,000 investment over time.")
public class Growth10KResult extends BaseCalculationResult {

  @Schema(description = "Performance end date")
  private LocalDate performanceEndDate;
  @Schema(description = "Performance start date")
  private LocalDate performanceStartDate;
  @Schema(description = "Growth of $10K data points over time")
  private List<KeyValueResult> growth10k;
}
