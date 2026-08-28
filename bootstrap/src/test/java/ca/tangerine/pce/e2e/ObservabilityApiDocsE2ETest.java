package ca.tangerine.pce.e2e;

import ca.tangerine.pce.PortfolioCalculationEngineApplication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The statistics endpoint is only usable from Swagger UI if the OpenAPI document describes its real response shape,
 * which {@code springdoc.show-actuator} alone does not do — it lists the endpoint with an untyped {@code object}.
 * Asserted against a bound model rather than the raw document text, so a passing test means the structure is right and
 * not merely that some expected word appears somewhere in the JSON.
 *
 * <p>
 * Building the document is the most expensive single request in the suite — springdoc reflects over every controller
 * and every request and response type before it can answer — so it is fetched once per class and the response timeout
 * is raised well above the {@code WebTestClient} default of five seconds, which a loaded CI agent cannot meet.
 */
@Tag("e2e")
@ActiveProfiles("test")
@AutoConfigureWebTestClient(timeout = "60s")
@SpringBootTest(classes = PortfolioCalculationEngineApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ObservabilityApiDocsE2ETest {

  private static final String API_DOCS = "/api/v1/c7f3e2a1-9b4d-4e8f-a6c2-1d5e7f9b3a2c/api-docs";
  private static final String STATISTICS_PATH = "/actuator/calculation-stats";
  private static final String HEALTH_PATH = "/actuator/health";
  private static final String METRICS_PATH = "/actuator/metrics";
  private static final String REPORT_SCHEMA = "CalculationStatisticsReport";

  private static ApiDocs cachedApiDocs;

  @Autowired
  private WebTestClient webTestClient;

  @Test
  void shouldDescribeTheStatisticsEndpointWithItsResponseSchema_whenOpenApiDocumentRequested() {
    ApiDocs docs = apiDocs();

    assertThat(docs.paths()).containsKey(STATISTICS_PATH).containsKey(HEALTH_PATH);
    assertThat(docs.paths())
        .as("an endpoint absent from the exposure list must not be advertised as callable")
        .doesNotContainKey(METRICS_PATH);

    Operation statistics = docs.paths().get(STATISTICS_PATH).get();
    assertThat(statistics).isNotNull();
    assertThat(statistics.summary()).isEqualTo("Per-metric calculation statistics");
    assertThat(statistics.description())
        .contains("most-problematic first")
        .contains("decomposed into its member metrics");
    assertThat(statistics.responses()).containsKey("200");
    assertThat(statistics.responses().get("200").content()).isNotEmpty();
    assertThat(statistics.responses().get("200").content().values())
        .as("Try it out is only meaningful when the response points at the real schema")
        .allSatisfy(mediaType -> assertThat(mediaType.schema().ref())
            .isEqualTo("#/components/schemas/" + REPORT_SCHEMA));

    assertThat(docs.components().schemas()).containsKey(REPORT_SCHEMA);
    assertThat(docs.components().schemas().get(REPORT_SCHEMA).properties())
        .containsOnlyKeys("overall", "metrics");
    assertThat(docs.components().schemas())
        .as("the nested rows carry the numbers, so their schemas have to be registered too")
        .containsKey("MetricStatistics")
        .containsKey("DurationStatistics");
    assertThat(docs.components().schemas().get("MetricStatistics").properties())
        .containsKeys("metric", "executions", "successes", "failures", "failureRatePercent",
            "duration", "warnings", "topErrorCodes", "topWarningCodes");
    assertThat(docs.components().schemas().get("DurationStatistics").properties())
        .containsKeys("samples", "meanMillis", "maxMillis", "p50Millis", "p95Millis", "p99Millis");
  }

  private ApiDocs apiDocs() {
    if (cachedApiDocs == null) {
      cachedApiDocs = webTestClient.get()
          .uri(API_DOCS)
          .exchange()
          .expectStatus().isOk()
          .expectBody(ApiDocs.class)
          .returnResult()
          .getResponseBody();
    }
    return cachedApiDocs;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record ApiDocs(Map<String, PathItem> paths, Components components) {
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record PathItem(Operation get) {
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record Operation(String summary, String description, Map<String, Response> responses) {
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record Response(Map<String, MediaTypeObject> content) {
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record MediaTypeObject(SchemaObject schema) {
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record Components(Map<String, SchemaObject> schemas) {
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record SchemaObject(
      @JsonProperty("$ref") String ref,
      String type,
      Map<String, SchemaObject> properties,
      List<String> required) {
  }
}
