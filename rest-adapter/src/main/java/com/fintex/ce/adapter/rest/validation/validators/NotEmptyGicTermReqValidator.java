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
    super(GicHolding::getTerm, ErrorCode.GIC_HOLDING_MISSING_TERM);
  }

  @Override
  public List<CalculationMetric> supportedMetrics() {
    return List.of(
        ASSET_ALLOCATIONS, ASSET_ALLOCATIONS_EM, EQUITY_SECTOR, SECTOR_EXPOSURE, EQUITY_COUNTRY_EXPOSURE,
        EQUITY_GEOGRAPHIC_EXPOSURE, FIXED_INCOME_COUNTRY_EXPOSURE, FIXED_INCOME_GEOGRAPHIC_EXPOSURE,
        FIXED_INCOME_BOND_SECTOR);
  }
}
