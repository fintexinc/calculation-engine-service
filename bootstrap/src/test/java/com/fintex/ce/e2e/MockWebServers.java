package com.fintex.ce.e2e;

import java.io.IOException;
import lombok.experimental.UtilityClass;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockWebServer;

/**
 * Starts and addresses the mock servers the e2e tests stand in for the external services they call.
 *
 * <p>
 * A test class holds its server in a {@code static final} field initialized from here rather than starting it in
 * {@code @BeforeAll}, because Spring resolves a {@code @DynamicPropertySource} while it builds the application context
 * and nothing orders that against the class's {@code @BeforeAll}: a server created there can still be null when the
 * property supplier reads its URL, and one created unconditionally in both places would leave the application pointed
 * at the first server while the test set dispatchers on and counted requests against the second. Class initialization,
 * by contrast, is guaranteed to have completed before any static member of the class can be touched — the property
 * supplier and every test method included.
 */
@UtilityClass
final class MockWebServers {

  /** A started server carrying {@code dispatcher} from the first request. */
  static MockWebServer started(Dispatcher dispatcher) {
    MockWebServer server = started();
    server.setDispatcher(dispatcher);
    return server;
  }

  /**
   * A started server, keeping MockWebServer's own queue dispatcher — for a class that enqueues responses, or sets its
   * dispatcher per test rather than once.
   */
  static MockWebServer started() {
    MockWebServer server = new MockWebServer();
    try {
      server.start();
    } catch (IOException e) {
      throw new IllegalStateException("could not start a mock web server", e);
    }
    return server;
  }

  /**
   * The server's URL as a base url: no trailing slash, which the clients under test append their paths to and would
   * otherwise double.
   */
  static String baseUrl(MockWebServer server) {
    return server.url("/").toString().replaceAll("/$", "");
  }
}
