package com.fintex.ce.calculation;

import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.BaseCalculationResult;
import com.fintex.ce.model.domain.security.SecurityData;
import com.fintex.ce.model.dto.command.CalculationCommand;
import com.fintex.wm.commons.domain.enumeration.CompositeSecurityAttribute;

import java.util.List;
import java.util.Map;

/**
 * Calculation service depending on exactly one Security Master attribute. Implementations declare the attribute via
 * {@link #requiredAttribute()} and receive its per-holding domain data directly in
 * {@link #perform(CalculationCommand, Object)}; attribute declaration and data preparation are defaulted.
 *
 * @param <C>
 *          The command type extending CalculationCommand
 * @param <T>
 *          The domain type of the attribute's per-holding values
 * @param <R>
 *          The result type
 */
public interface SingleAttributeCalculationService<C extends CalculationCommand, T, R extends BaseCalculationResult>
    extends
      CalculationService<C, Map<PortfolioHolding, T>, R> {

  CompositeSecurityAttribute requiredAttribute();

  @Override
  default List<CompositeSecurityAttribute> requiredAttributes() {
    return List.of(requiredAttribute());
  }

  @Override
  default Map<PortfolioHolding, T> prepareData(SecurityData securityData) {
    return securityData.get(requiredAttribute());
  }

}
