package ca.tangerine.pce.model.domain.calculation.allocation;

import ca.tangerine.pce.model.domain.calculation.BaseCalculationData;
import ca.tangerine.wm.commons.domain.allocation.SectorAllocationType;
import ca.tangerine.wm.commons.domain.currency.Currency;

import java.math.BigDecimal;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Per-security consolidated sector distribution — equity sectors and fixed-income buckets on one scale, as fractions of
 * the whole security — plus the currency its values are quoted in.
 *
 * <p>
 * Unlike {@link EquitySector} and {@link FixedIncomeBondSector}, which each describe one sleeve and are rescaled to 1
 * within it, this distribution already carries each sleeve's share of the security, so a balanced fund arrives split
 * between stock sectors and bond buckets rather than as two distributions with no relative weight.
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class HoldingSectorAllocation extends BaseCalculationData {

  private Map<SectorAllocationType, BigDecimal> allocations;
  private Currency currency;
}
