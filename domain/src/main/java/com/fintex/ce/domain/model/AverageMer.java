package com.fintex.ce.domain.model;

import com.fintex.ce.domain.model.core.ProviderAware;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class AverageMer implements ProviderAware {

  private BigDecimal mer;
  private BigDecimal actualManagementFee;
  private String merProvider;
  private String actualManagementFeeProvider;

  private BigDecimal netExpenseRatio;
  private BigDecimal grossExpenseRatio;
  private String netExpenseRatioProvider;
  private String grossExpenseRatioProvider;

  // Common fields
  private String holdingId;
  private String provider;
  private String providers;
  private List<ValidationError> errors = new ArrayList<>();

  public boolean hasErrors() {
    return errors != null && !errors.isEmpty();
  }

}
