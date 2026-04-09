package com.fintex.ce.domain.model;

import com.fintex.sm.model.DataProvider;

import java.math.BigDecimal;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * Combined fee data including management expense ratio, expense ratios, and management fee. Maps from /fees response.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@Accessors(chain = true)
public class FeeData extends BaseCalculationData<FeeData> {

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
