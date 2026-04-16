package com.fintex.ce.adapter.rest.validation.validators;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.GicHolding;
import com.fintex.ce.model.error.ErrorCode;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

import static com.fintex.ce.model.domain.enumeration.CalculationMetric.*;

@Component
@Order(420)
public class NotEmptyGicTermReqValidator extends AbstractGicFieldReqValidator<BigDecimal> {

  public NotEmptyGicTermReqValidator() {
    super(GicHolding::getTerm, ErrorCode.ERR_GIC_MC_002);
  }

  @Override
  public List<CalculationMetric> supportedMetrics() {
    return List.of(
        ASSET_ALLOCATIONS,
        ASSET_ALLOCATIONS_EM,
        EQUITY_SECTOR,
        EQUITY_COUNTRY_EXPOSURE,
        EQUITY_STYLEBOX_EXPOSURE,
        EQUITY_GEOGRAPHIC_EXPOSURE,
        EQUITY_MARKET_CAPITALIZATION,
        FIXED_INCOME_COUNTRY_EXPOSURE,
        FIXED_INCOME_GEOGRAPHIC_EXPOSURE,
        FIXED_INCOME_BOND_SECTOR,
        FIXED_INCOME_STYLEBOX_EXPOSURE,
        MATURITY_ALLOCATION,
        SALES_CHARGE,
        FIXED_INCOME_CREDIT_QUALITY,
        YIELD,
        CLASSIFICATION_ALLOCATION,
        INCOME_FORECAST);
  }
}
