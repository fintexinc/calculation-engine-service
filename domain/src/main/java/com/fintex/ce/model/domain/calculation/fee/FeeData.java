package com.fintex.ce.model.domain.calculation.fee;

import com.fintex.ce.model.domain.calculation.BaseCalculationData;
import com.fintex.wm.commons.domain.DataProvider;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
/**
 * Combined fee data including management expense ratio, expense ratios, and management fee. Maps from /fees response.
 */
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FeeData extends BaseCalculationData {

  private BigDecimal managementFee;
  private DataProvider managementFeeProvider;

  private BigDecimal managementExpenseRatio;
  private DataProvider managementExpenseRatioProvider;

  private BigDecimal netExpenseRatio;
  private DataProvider netExpenseRatioProvider;

  private BigDecimal grossExpenseRatio;
  private DataProvider grossExpenseRatioProvider;

  private BigDecimal actual12B1Fee;
  private DataProvider actual12B1FeeProvider;
}
