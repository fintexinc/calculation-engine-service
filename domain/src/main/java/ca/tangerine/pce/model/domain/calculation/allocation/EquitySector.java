package ca.tangerine.pce.model.domain.calculation.allocation;

import java.math.BigDecimal;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import ca.tangerine.pce.model.domain.calculation.BaseCalculationData;
import ca.tangerine.wm.commons.domain.allocation.EquitySectorAllocationType;
import ca.tangerine.wm.commons.domain.currency.Currency;
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class EquitySector extends BaseCalculationData {

  private Map<EquitySectorAllocationType, BigDecimal> allocations;
  private Currency currency;

}
