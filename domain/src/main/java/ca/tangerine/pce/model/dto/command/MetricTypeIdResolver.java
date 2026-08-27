package ca.tangerine.pce.model.dto.command;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;

import ca.tangerine.pce.model.domain.enumeration.CalculationMetric;

/**
 * Resolves the concrete {@link CalculationCommand} subtype from the {@code metric} discriminator using the canonical
 * {@link CalculationMetric#getValue()} on both read and write. Unlike name-based subtype resolution, this preserves the
 * exact metric value on serialization even when several metrics share a single command type, so a round-tripped command
 * always reports the metric it was built with.
 */
public class MetricTypeIdResolver extends TypeIdResolverBase {

  @Override
  public String idFromValue(Object value) {
    return idFromValueAndType(value, value == null ? null : value.getClass());
  }

  @Override
  public String idFromValueAndType(Object value, Class<?> suggestedType) {
    if (value instanceof CalculationCommand command && command.getMetric() != null) {
      return command.getMetric().getValue();
    }
    return null;
  }

  @Override
  public JavaType typeFromId(DatabindContext context, String id) {
    CalculationMetric metric = CalculationMetric.from(id);
    return context.constructType(metric.getCommandType());
  }

  @Override
  public JsonTypeInfo.Id getMechanism() {
    return JsonTypeInfo.Id.CUSTOM;
  }
}
