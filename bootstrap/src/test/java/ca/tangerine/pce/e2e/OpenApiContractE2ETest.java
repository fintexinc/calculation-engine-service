package ca.tangerine.pce.e2e;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

import ca.tangerine.pce.PortfolioCalculationEngineApplication;

/**
 * Keeps the published contract in {@code docs/openapi} equal to the document the running service actually serves.
 *
 * <p>
 * The contract is a file in the repository, so nothing else notices when it stops describing the code: it is not an
 * input to the build and no other test reads it. Market Investment Catalogue's copy spent three months documenting
 * twenty endpoints that all answered 404 for exactly that reason. Here the document is exported by a test rather than
 * by hand, so drifting from the implementation is a failing build rather than a discovery months later.
 *
 * <p>
 * Run with {@code -Dopenapi.update=true} to rewrite the file after deliberately changing an endpoint or a payload; the
 * diff is then part of the review, which is the point — a rename that nobody meant to publish shows up there.
 */
@Tag("e2e")
@ActiveProfiles("test")
@AutoConfigureWebTestClient(timeout = "60s")
@SpringBootTest(classes = PortfolioCalculationEngineApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OpenApiContractE2ETest {

  private static final String API_DOCS_YAML = "/api/v1/c7f3e2a1-9b4d-4e8f-a6c2-1d5e7f9b3a2c/api-docs.yaml";

  private static final Path CONTRACT = repositoryRoot()
      .resolve("docs/openapi/portfolio-calculation-engine-api-generated.yaml");

  private static final String UPDATE_PROPERTY = "openapi.update";

  @Autowired
  private WebTestClient webTestClient;

  /**
   * Found by walking up from the working directory to the settings file, rather than assumed to be one level up: the
   * working directory is the module's under Gradle and the repository root under some IDE run configurations, and a
   * test that writes the contract into the wrong place would be worse than one that does not run.
   */
  private static Path repositoryRoot() {
    Path candidate = Path.of("").toAbsolutePath();
    while (candidate != null && !Files.exists(candidate.resolve("settings.gradle.kts"))) {
      candidate = candidate.getParent();
    }
    if (candidate == null) {
      throw new IllegalStateException("settings.gradle.kts not found above " + Path.of("").toAbsolutePath());
    }
    return candidate;
  }

  @Test
  void shouldMatchThePublishedContract_whenTheServiceServesItsOpenApiDocument() throws IOException {
    String served = servedDocument();

    if (Boolean.getBoolean(UPDATE_PROPERTY)) {
      Files.createDirectories(CONTRACT.getParent());
      Files.writeString(CONTRACT, served);
      return;
    }

    assertThat(CONTRACT)
        .as("%s is missing; export it with -D%s=true", CONTRACT.normalize(), UPDATE_PROPERTY)
        .exists();
    assertThat(normalized(Files.readString(CONTRACT)))
        .as("the published contract no longer describes this service; re-export it with -D%s=true and review the diff",
            UPDATE_PROPERTY)
        .isEqualTo(normalized(served));
  }

  private String servedDocument() {
    return webTestClient.get()
        .uri(API_DOCS_YAML)
        .exchange()
        .expectStatus().isOk()
        .expectBody(String.class)
        .returnResult()
        .getResponseBody();
  }

  /** Line endings are the checkout's business, not the contract's. */
  private static String normalized(String document) {
    return document == null ? "" : document.replace("\r\n", "\n").strip();
  }
}
