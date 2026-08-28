package ca.tangerine.pce.calculation;

import ca.tangerine.pce.model.domain.result.BaseCalculationResult;
import ca.tangerine.pce.model.domain.result.composite.CompositeCalculationResult;
import ca.tangerine.pce.model.dto.command.CalculationCommand;

import java.util.List;

/**
 * Entry point for executing portfolio calculations on already-validated commands. The orchestrator resolves the
 * metric's {@link CalculationService} from {@code command.getMetric()}, fetches the Market Investment Catalogue
 * attributes the service declares, prepares the service's typed data and dispatches — for a single command as well as
 * for a composite request calculating several metrics at once. Request validation and observability are owned by the
 * REST adapter.
 */
public interface CalculationOrchestrator {

  BaseCalculationResult calculate(CalculationCommand command);

  CompositeCalculationResult calculateAll(List<CalculationCommand> commands);

}
