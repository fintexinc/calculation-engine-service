package ca.tangerine.pce.calculation;

import java.util.List;

import ca.tangerine.pce.model.domain.enumeration.CalculationMetric;
import ca.tangerine.pce.model.domain.result.BaseCalculationResult;
import ca.tangerine.pce.model.domain.security.SecurityData;
import ca.tangerine.pce.model.dto.command.CalculationCommand;
import ca.tangerine.wm.commons.domain.enumeration.CompositeSecurityAttribute;

/**
 * Base interface for all calculation services. A service is a pure consumer of Market Investment Catalogue data: it
 * declares the attributes it depends on via {@link #requiredAttributes()} and receives its data as one strongly typed
 * parameter. The orchestrator fetches the declared attributes and converts them into the service's data type through
 * {@link #prepareData(SecurityData)} before invoking {@link #perform}; calculation logic never sees the raw fetched
 * attributes. Services depending on exactly one attribute should implement {@link SingleAttributeCalculationService},
 * which defaults both declaration and data preparation.
 *
 * @param <C>
 *          The command type extending CalculationCommand
 * @param <D>
 *          The data type the service consumes (e.g. a per-holding map of one attribute's domain objects)
 * @param <R>
 *          The result type
 */
public interface CalculationService<C extends CalculationCommand, D, R extends BaseCalculationResult> {

  R perform(C command, D data);

  CalculationMetric getMetric();

  List<CompositeSecurityAttribute> requiredAttributes();

  D prepareData(SecurityData securityData);

}
