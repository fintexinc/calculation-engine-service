package com.fintex.ce.domain.model;

import java.math.BigDecimal;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@Accessors(chain = true)
public class AverageMer extends BaseCalculationData<AverageMer> {

  private BigDecimal mer;
  private BigDecimal actualManagementFee;

  private BigDecimal netExpenseRatio;
  private BigDecimal grossExpenseRatio;

}
