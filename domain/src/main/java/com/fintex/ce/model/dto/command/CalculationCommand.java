package com.fintex.ce.model.dto.command;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.wm.commons.domain.DataProvider;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Base of every calculation command: the {@code metric} discriminator and the inputs shared by all metrics. The
 * metric-specific inputs live on the subtypes, which are declared here as the {@code oneOf} alternatives so a client
 * can see - and validate against - the payload the chosen metric actually requires. The {@code discriminator.mapping}
 * from metric value to subtype schema is built from {@link CalculationMetric} by {@code OpenAPIConfig}, so the document
 * maps exactly the metrics the service can execute.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "metric", visible = true, defaultImpl = Void.class)
@JsonTypeIdResolver(MetricTypeIdResolver.class)
@Schema(description = "Calculation command: the 'metric' discriminator selects which of the alternatives below the "
    + "rest of the payload has to satisfy", discriminatorProperty = "metric", oneOf = {
        PeriodCommand.class, ReturnCommand.class, PortfolioHoldingsCommand.class, TopCommonHoldingsCommand.class,
        AverageMerCommand.class, MerComparisonCommand.class, MultiplePortfoliosCommand.class})
public abstract class CalculationCommand {

  @Schema(description = "Calculation metric type that determines which calculation to execute")
  private CalculationMetric metric;

  @ArraySchema(arraySchema = @Schema(description = "Data providers to use for fetching security data; configured defaults apply when absent", example = "[\"MORNINGSTAR\"]"), schema = @Schema(implementation = DataProvider.class))
  private List<DataProvider> dataProviders;
}
