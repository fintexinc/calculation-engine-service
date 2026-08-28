package ca.tangerine.pce.calculation;

import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.pce.model.domain.result.BaseCalculationResult;
import ca.tangerine.pce.model.domain.security.SecurityData;
import ca.tangerine.pce.model.dto.command.CalculationCommand;
import ca.tangerine.wm.commons.domain.enumeration.CompositeSecurityAttribute;

import java.util.List;
import java.util.Map;

/**
 * Calculation service depending on exactly one Market Investment Catalogue attribute. Implementations declare the
 * attribute via {@link #requiredAttribute()} and receive its per-holding domain data directly in
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
