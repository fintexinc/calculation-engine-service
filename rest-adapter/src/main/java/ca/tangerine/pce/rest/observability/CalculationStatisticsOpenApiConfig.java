package ca.tangerine.pce.rest.observability;

import ca.tangerine.pce.port.observability.CalculationStatisticsReport;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springdoc.core.customizers.OpenApiCustomizer;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.converter.ResolvedSchema;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;

import java.util.Map;

/**
 * Describes the calculation-statistics actuator endpoint in the OpenAPI document. {@code springdoc.show-actuator} alone
 * lists the endpoint with an untyped {@code object} response, so this customizer attaches the real
 * {@link CalculationStatisticsReport} schema and a description of what to look at, making the endpoint usable straight
 * from Swagger UI.
 */
@Configuration
public class CalculationStatisticsOpenApiConfig {

  private static final String ENDPOINT_PATH = "/actuator/calculation-stats";
  private static final String SCHEMA_NAME = "CalculationStatisticsReport";
  private static final String SCHEMA_REF = "#/components/schemas/" + SCHEMA_NAME;
  private static final String OK = "200";

  private static final String SUMMARY = "Per-metric calculation statistics";
  private static final String DESCRIPTION = """
      Ranked statistics for every calculation metric that has been executed since the service started, plus an
      aggregate across all of them.

      Rows in `metrics` are ordered most-problematic first - by absolute failure count, then by failure ratio - so the
      head of the list is where to look. Per metric: execution counts split by outcome, `failureRatePercent`,
      calculation latency of successful runs (mean / max / p50 / p95 / p99, in milliseconds), the warning count
      distribution (total / min / mean / max), and the most frequent error and warning codes.

      A composite request is decomposed into its member metrics, so every row describes one metric regardless of which
      endpoint the client called. A request rejected before dispatch - unknown metric, metric mismatch, a failed
      validation rule - never reached a calculator and is counted nowhere here, so `failureRatePercent` measures the
      service rather than its callers.

      The code rankings under `overall` are merged from the complete per-metric tallies, so a code that sits just below
      the cut-off for every metric individually still shows up when it is the most frequent code service-wide.

      Everything here is held in memory and lost when the instance restarts. Counts, totals and `meanMillis` are
      cumulative since startup; `maxMillis` and the percentiles come from the registry's rolling distribution window, so
      after a quiet period they read low or zero while `samples` stays where it was.
      """;

  @Bean
  public OpenApiCustomizer calculationStatisticsOpenApiCustomizer() {
    return openApi -> {
      Operation operation = readOperation(openApi);
      if (operation == null) {
        return;
      }
      registerSchemas(openApi);
      operation.setSummary(SUMMARY);
      operation.setDescription(DESCRIPTION);
      applyResponseSchema(operation);
    };
  }

  private static Operation readOperation(OpenAPI openApi) {
    if (openApi.getPaths() == null) {
      return null;
    }
    PathItem pathItem = openApi.getPaths().get(ENDPOINT_PATH);
    return pathItem == null ? null : pathItem.getGet();
  }

  @SuppressWarnings("rawtypes")
  private static void registerSchemas(OpenAPI openApi) {
    if (openApi.getComponents() == null) {
      openApi.setComponents(new Components());
    }
    ResolvedSchema resolved = ModelConverters.getInstance()
        .resolveAsResolvedSchema(new AnnotatedType(CalculationStatisticsReport.class).resolveAsRef(false));
    if (resolved == null || resolved.schema == null) {
      return;
    }
    Map<String, Schema> referenced = resolved.referencedSchemas;
    if (referenced != null) {
      referenced.forEach(openApi.getComponents()::addSchemas);
    }
    openApi.getComponents().addSchemas(SCHEMA_NAME, resolved.schema);
  }

  private static void applyResponseSchema(Operation operation) {
    if (operation.getResponses() == null) {
      return;
    }
    ApiResponse response = operation.getResponses().get(OK);
    if (response == null) {
      return;
    }
    Content content = response.getContent();
    if (content == null) {
      return;
    }
    content.values().forEach(mediaType -> pointAtReport(mediaType));
  }

  private static void pointAtReport(MediaType mediaType) {
    mediaType.setSchema(new Schema<>().$ref(SCHEMA_REF));
  }
}
