package com.fintex.ce.e2e;

import com.fintex.ce.PortfolioCalculationEngineApplication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = PortfolioCalculationEngineApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VersionEndpointE2ETest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void getVersion_returns200WithValidVersionResponse() throws Exception {
    MvcResult result = mockMvc.perform(get("/version"))
        .andExpect(status().isOk())
        .andReturn();

    String responseBody = result.getResponse().getContentAsString();
    JsonNode jsonNode = objectMapper.readTree(responseBody);

    // Verify exactly three fields exist
    assertThat(jsonNode.fieldNames()).toIterable()
        .containsExactlyInAnyOrder("name", "version", "uptimeSeconds");

    // Verify field types and values
    assertThat(jsonNode.get("name")).isNotNull();
    assertThat(jsonNode.get("name").isTextual()).isTrue();
    assertThat(jsonNode.get("name").asText()).isEqualTo("ce");

    assertThat(jsonNode.get("version")).isNotNull();
    assertThat(jsonNode.get("version").isTextual()).isTrue();
    assertThat(jsonNode.get("version").asText()).isNotBlank();

    assertThat(jsonNode.get("uptimeSeconds")).isNotNull();
    assertThat(jsonNode.get("uptimeSeconds").isIntegralNumber()).isTrue();
    long uptimeSeconds = jsonNode.get("uptimeSeconds").asLong();
    assertThat(uptimeSeconds).isGreaterThanOrEqualTo(0L);
  }

  @Test
  void getVersion_requiresNoAuthentication() throws Exception {
    // Call without any credentials or authentication headers
    mockMvc.perform(get("/version"))
        .andExpect(status().isOk());
  }

  @Test
  void getVersion_returnsJsonContentType() throws Exception {
    mockMvc.perform(get("/version"))
        .andExpect(status().isOk())
        .andReturn();

    String contentType = mockMvc.perform(get("/version"))
        .andReturn()
        .getResponse()
        .getContentType();

    assertThat(contentType).contains("application/json");
  }
}
