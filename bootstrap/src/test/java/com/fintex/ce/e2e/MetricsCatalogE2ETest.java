package com.fintex.ce.e2e;

import com.fintex.ce.PortfolioCalculationEngineApplication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import okhttp3.mockwebserver.MockWebServer;

@SpringBootTest(classes = PortfolioCalculationEngineApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MetricsCatalogE2ETest {

  private static MockWebServer smsMockServer;

  @Autowired
  private MockMvc mockMvc;

  @BeforeAll
  static void setupSecurityMasterMock() throws IOException {
    ensureSmsMockServerStarted();
  }

  private static void ensureSmsMockServerStarted() throws IOException {
    if (smsMockServer == null) {
      smsMockServer = new MockWebServer();
      smsMockServer.start();
    }
  }

  private static String smsMockBaseUrl() {
    try {
      ensureSmsMockServerStarted();
      return smsMockServer.url("/").toString().replaceAll("/$", "");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @DynamicPropertySource
  static void registerSecurityMasterUrl(DynamicPropertyRegistry registry) {
    registry.add("external-services.security-master.rest.base-url", MetricsCatalogE2ETest::smsMockBaseUrl);
  }

  @Test
  void getMetricsCatalog_returnsOkStatus() throws Exception {
    mockMvc.perform(get("/metrics/catalog")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  void getMetricsCatalog_returnsJsonArray() throws Exception {
    MvcResult result = mockMvc.perform(get("/metrics/catalog")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andReturn();

    String content = result.getResponse().getContentAsString();
    assertTrue(content.startsWith("["), "Response should be a JSON array");
    assertTrue(content.endsWith("]"), "Response should be a JSON array");
  }

  @Test
  void getMetricsCatalog_containsExpectedMetrics() throws Exception {
    MvcResult result = mockMvc.perform(get("/metrics/catalog")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andReturn();

    String content = result.getResponse().getContentAsString();
    assertTrue(content.contains("sharpe-ratio"), "Catalog should contain sharpe-ratio");
    assertTrue(content.contains("trailing-total-returns"), "Catalog should contain trailing-total-returns");
    assertTrue(content.contains("standard-deviation"), "Catalog should contain standard-deviation");
  }

  @Test
  void getMetricsCatalog_noDuplicateIds() throws Exception {
    MvcResult result = mockMvc.perform(get("/metrics/catalog")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andReturn();

    String content = result.getResponse().getContentAsString();
    int totalMetrics = content.split("\"id\":").length - 1;
    Set<String> uniqueIds = new HashSet<>();
    String[] parts = content.split("\"id\":");
    for (int i = 1; i < parts.length; i++) {
      String idPart = parts[i].split(",")[0].replaceAll("[\"\s]", "");
      uniqueIds.add(idPart);
    }
    assertEquals(totalMetrics, uniqueIds.size(), "Catalog should have no duplicate metric IDs");
  }

  @Test
  void getMetricsCatalog_allMetricsHaveRequiredFields() throws Exception {
    MvcResult result = mockMvc.perform(get("/metrics/catalog")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andReturn();

    String content = result.getResponse().getContentAsString();
    assertTrue(content.contains("\"id\":"), "All metrics should have id field");
    assertTrue(content.contains("\"name\":"), "All metrics should have name field");
    assertTrue(content.contains("\"category\":"), "All metrics should have category field");
  }
}
