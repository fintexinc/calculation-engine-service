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
import ca.tangerine.wm.commons.domain.allocation.SectorAllocationType;

/**
 * Result of the consolidated {@code sector-exposure} metric.
 *
 * <p>
 * A distinct key from {@code equitySector} / {@code fixedIncomeSector} on purpose: unlike the three geographic metrics,
 * which all report the same region scale and can share a key, this payload is keyed by a different bucket scale than
 * either per-sleeve sector metric, so reusing one of their keys would give the same name to two different taxonomies.
 */
@SuperBuilder
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Response for sector-exposure metric. Contains the consolidated sector breakdown of the whole "
    + "portfolio, equity and fixed income together.")
public class ConsolidatedSectorExposureResult extends BaseCalculationResult {

  @Schema(description = "Portfolio proportion held in each sector, as a fraction of the whole portfolio (the buckets "
      + "sum to 1). Exposure the provider classifies as neither equity nor fixed income is reported under OTHER; "
      + "holdings the data source has no record of, and sleeves it resolved without a sector breakdown, under UNKNOWN.")
  private Map<SectorAllocationType, BigDecimal> sectorExposure;
}
