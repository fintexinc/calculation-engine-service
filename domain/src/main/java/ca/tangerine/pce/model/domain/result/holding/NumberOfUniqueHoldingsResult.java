package ca.tangerine.pce.model.domain.result.holding;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import ca.tangerine.pce.model.domain.result.BaseCalculationResult;
import ca.tangerine.wm.commons.error.Notification;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Response for number-of-unique-holdings metric. Contains the count of unique underlying holding IDs across all portfolio securities.")
public class NumberOfUniqueHoldingsResult extends BaseCalculationResult {

  @Schema(description = "Count of unique underlying holdings across all portfolio securities. Holdings without a "
      + "resolvable identifier are each counted individually; check the warnings list for details.")
  private Long numberOfUniqueHoldings;

  public NumberOfUniqueHoldingsResult(Long count, List<Notification> warnings) {
    this.numberOfUniqueHoldings = count;
    this.warnings = warnings;
  }
}
