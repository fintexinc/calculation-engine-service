package ca.tangerine.pce.model.domain.calculation.fee;

import ca.tangerine.pce.model.domain.calculation.BaseCalculationData;

import java.math.BigDecimal;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class AverageMer extends BaseCalculationData {

  private BigDecimal mer;
  private BigDecimal actualManagementFee;

  private BigDecimal netExpenseRatio;
  private BigDecimal grossExpenseRatio;

}
