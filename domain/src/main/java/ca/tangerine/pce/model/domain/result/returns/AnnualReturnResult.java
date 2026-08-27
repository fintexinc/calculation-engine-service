package ca.tangerine.pce.model.domain.result.returns;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import ca.tangerine.pce.model.domain.result.DatesResult;
import ca.tangerine.pce.model.domain.result.KeyValueResult;
@SuperBuilder
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Response for annual-return metric. Contains calendar-year annual returns.")
public class AnnualReturnResult extends DatesResult {

  @Schema(description = "Annual returns keyed by calendar year")
  private List<KeyValueResult<Integer>> annualReturns;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  @Schema(description = "Portfolio-versus-benchmark annual return comparison by calendar year")
  private List<ReturnComparison<Integer>> comparison;
}
