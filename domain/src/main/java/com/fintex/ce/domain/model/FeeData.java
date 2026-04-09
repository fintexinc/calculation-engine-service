package com.fintex.ce.domain.model;

import com.fintex.ce.domain.model.core.ProviderAware;
import com.fintex.sm.model.DataProvider;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Combined fee data including management expense ratio, expense ratios, and management fee. Maps from /fees response.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class FeeData implements ProviderAware {

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

  // Common fields
  private String holdingId;
  private String provider;
  private String providers;
  private List<ValidationError> errors = new ArrayList<>();

  public boolean hasErrors() {
    return errors != null && !errors.isEmpty();
  }
}
