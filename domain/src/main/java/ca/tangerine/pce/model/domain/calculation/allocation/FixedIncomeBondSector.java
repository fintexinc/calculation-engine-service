package ca.tangerine.pce.model.domain.calculation.allocation;

import ca.tangerine.pce.model.domain.calculation.BaseCalculationData;
import ca.tangerine.wm.commons.domain.allocation.FixedIncomeSectorAllocationType;
import ca.tangerine.wm.commons.domain.currency.Currency;
import ca.tangerine.wm.commons.domain.enumeration.FinancialInstrumentType;

import java.math.BigDecimal;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FixedIncomeBondSector extends BaseCalculationData {

  private FinancialInstrumentType holdingType;
  private Map<FixedIncomeSectorAllocationType, BigDecimal> fixedIncomeBondSectors;
  private Currency currency;

}
