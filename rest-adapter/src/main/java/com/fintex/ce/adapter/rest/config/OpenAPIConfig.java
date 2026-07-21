package com.fintex.ce.adapter.rest.config;

import com.fintex.ce.calculation.CalculationService;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import org.springdoc.core.customizers.OpenApiCustomizer;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
public class OpenAPIConfig {

  private static final String CALCULATION_METRIC_SCHEMA = "CalculationMetric";
  private static final String METRIC_NAME_PARAMETER = "metricName";
  private static final String METRIC_PROPERTY = "metric";

  @Bean
  public OpenAPI pcsOpenApi() {
    return new OpenAPI()
        .info(new Info()
            .title("Portfolio Calculation Engine API")
            .description("Portfolio analytics and risk measurement engine. "
                + "Calculates returns, risk metrics, risk-adjusted ratios, "
                + "portfolio composition, fees, and forecasts.")
            .version("1.0.0")
            .contact(new Contact()
                .name("Digital Wealth Team")
                .email("kparamsothy@tangerine.ca"))
            .license(new License().name("Portfolio Calculation Engine")));
  }

  /**
   * Restricts the metric enum exposed in the OpenAPI/Swagger document to the metrics that are actually registered as
   * {@link CalculationService} beans. Unsupported metrics have no bean (their service classes are not annotated with
   * {@code @Service}), so they are removed from both the {@code metricName} path parameter and the request-body
   * {@code metric} field — keeping Swagger in sync with the endpoints that can actually be called.
   */
  @Bean
  public OpenApiCustomizer enabledMetricsOpenApiCustomizer(List<CalculationService<?, ?, ?>> calculationServices) {
    Set<String> enabledMetrics = calculationServices.stream()
        .map(CalculationService::getMetric)
        .map(CalculationMetric::getValue)
        .collect(Collectors.toUnmodifiableSet());
    return openApi -> {
      retainEnabledMetricsInSchemas(openApi, enabledMetrics);
      retainEnabledMetricsInParameters(openApi, enabledMetrics);
    };
  }

  @SuppressWarnings("rawtypes")
  private static void retainEnabledMetricsInSchemas(OpenAPI openApi, Set<String> enabledMetrics) {
    Map<String, Schema> schemas = Optional.ofNullable(openApi.getComponents())
        .map(Components::getSchemas)
        .orElseGet(Map::of);

    // Defensive: a standalone CalculationMetric enum component, if springdoc emits one via $ref.
    Schema metricSchema = schemas.get(CALCULATION_METRIC_SCHEMA);
    if (metricSchema != null && !CollectionUtils.isEmpty(metricSchema.getEnum())) {
      retainEnabledValues(metricSchema, enabledMetrics);
    }

    // The inline `metric` enum carried by every request-body command schema.
    schemas.values().stream()
        .map(Schema::getProperties)
        .filter(Objects::nonNull)
        .map(properties -> properties.get(METRIC_PROPERTY))
        .filter(Schema.class::isInstance)
        .map(Schema.class::cast)
        .filter(schema -> !CollectionUtils.isEmpty(schema.getEnum()))
        .forEach(schema -> retainEnabledValues(schema, enabledMetrics));
  }

  private static void retainEnabledMetricsInParameters(OpenAPI openApi, Set<String> enabledMetrics) {
    if (openApi.getPaths() == null) {
      return;
    }
    openApi.getPaths().values().stream()
        .flatMap(pathItem -> pathItem.readOperations().stream())
        .map(Operation::getParameters)
        .filter(Objects::nonNull)
        .flatMap(List::stream)
        .filter(parameter -> METRIC_NAME_PARAMETER.equals(parameter.getName()))
        .map(Parameter::getSchema)
        .filter(schema -> schema != null && !CollectionUtils.isEmpty(schema.getEnum()))
        .forEach(schema -> retainEnabledValues(schema, enabledMetrics));
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private static void retainEnabledValues(Schema schema, Set<String> enabledMetrics) {
    List<Object> filtered = ((List<Object>) schema.getEnum()).stream()
        .filter(value -> enabledMetrics.contains(String.valueOf(value)))
        .collect(Collectors.toList());
    schema.setEnum(filtered);
  }
}
