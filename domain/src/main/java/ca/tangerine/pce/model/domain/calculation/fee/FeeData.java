package ca.tangerine.pce.model.domain.calculation.fee;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import ca.tangerine.pce.model.domain.calculation.BaseCalculationData;
import ca.tangerine.pce.model.util.BigDecimalUtils;
import ca.tangerine.wm.commons.domain.DataProvider;
import ca.tangerine.wm.commons.domain.currency.Currency;
/**
 * Combined fee data: management expense ratio, expense ratios, and management fee. Maps from the MIC /fees response.
 *
 * <p>
 * <b>Units.</b> All fee fields here are stored in <i>ratio</i> form (e.g. {@code 0.0151} for 1.51%), not the percentage
 * form Market Investment Catalogue sends over the wire ({@code 1.51}). The conversion happens once in
 * {@code FeesMapper#ratio} at the adapter boundary (which delegates to {@link BigDecimalUtils#percentageToRatio});
 * every consumer of {@code FeeData} (the resolver, weighted average, dollar-fee sum, FUNDS_ONLY_STRICT null check) can
 * rely on this and does not need to scale.
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

  private Currency currency;
}
