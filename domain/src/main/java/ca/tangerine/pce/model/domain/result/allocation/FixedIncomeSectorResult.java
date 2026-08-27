package ca.tangerine.pce.model.domain.result.allocation;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import ca.tangerine.pce.model.domain.result.BaseCalculationResult;
import ca.tangerine.wm.commons.domain.allocation.FixedIncomeSectorAllocationType;
@SuperBuilder
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Response for fixed-income-bond-sector metric. Contains fixed income bond sector allocation breakdown.")
public class FixedIncomeSectorResult extends BaseCalculationResult {

  @Schema(description = "Fixed income allocation percentages by bond sector. Holdings the data source has no record "
      + "of at all, or resolved but did not return a bond sector breakdown for, are counted under "
      + "FixedIncomeSectorAllocationType.UNKNOWN.")
  private Map<FixedIncomeSectorAllocationType, BigDecimal> fixedIncomeSector;
}
