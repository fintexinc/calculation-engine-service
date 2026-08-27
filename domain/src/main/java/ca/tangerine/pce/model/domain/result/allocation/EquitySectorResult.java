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
import ca.tangerine.wm.commons.domain.allocation.EquitySectorAllocationType;
@SuperBuilder
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Response for equity-sector metric. Contains equity sector allocation breakdown.")
public class EquitySectorResult extends BaseCalculationResult {

  @Schema(description = "Equity allocation percentages by sector. Holdings the data source has no record of at all, "
      + "or resolved but did not return a sector breakdown for, are counted under EquitySectorAllocationType.UNKNOWN.")
  private Map<EquitySectorAllocationType, BigDecimal> equitySector;
}
