package ca.tangerine.pce.model.domain.calculation.exposure;

import java.math.BigDecimal;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import ca.tangerine.pce.model.domain.calculation.BaseCalculationData;
import ca.tangerine.wm.commons.domain.enumeration.Country;
import ca.tangerine.wm.commons.domain.enumeration.FinancialInstrumentType;
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CountryExposure extends BaseCalculationData {

  private FinancialInstrumentType holdingType;
  private Map<Country, BigDecimal> allocations;

}
