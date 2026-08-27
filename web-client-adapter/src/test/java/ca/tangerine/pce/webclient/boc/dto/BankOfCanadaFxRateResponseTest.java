package ca.tangerine.pce.webclient.boc.dto;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

class BankOfCanadaFxRateResponseTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void shouldDeserializeBankOfCanadaJson() throws Exception {
    String json = """
        {
          "terms": {
            "url": "https://www.bankofcanada.ca/terms/"
          },
          "seriesDetail": {
            "FXUSDCAD": {
              "label": "USD/CAD",
              "description": "US dollar to Canadian dollar daily exchange rate"
            }
          },
          "observations": [
            {
              "d": "2024-01-02",
              "FXUSDCAD": {
                "v": "1.3242"
              }
            },
            {
              "d": "2024-01-03",
              "FXUSDCAD": {
                "v": "1.3350"
              }
            }
          ]
        }
        """;

    BankOfCanadaFxRateResponse response = objectMapper.readValue(json, BankOfCanadaFxRateResponse.class);

    assertThat(response.getObservations()).hasSize(2);

    BankOfCanadaFxRateResponse.Observation first = response.getObservations().get(0);
    assertThat(first.getDate()).isEqualTo("2024-01-02");
    assertThat(first.getSeriesValues()).containsKey("FXUSDCAD");
    assertThat(first.getSeriesValues().get("FXUSDCAD").getValue()).isEqualTo("1.3242");

    BankOfCanadaFxRateResponse.Observation second = response.getObservations().get(1);
    assertThat(second.getDate()).isEqualTo("2024-01-03");
    assertThat(second.getSeriesValues().get("FXUSDCAD").getValue()).isEqualTo("1.3350");
  }

  @Test
  void shouldIgnoreUnknownFieldsInResponse() throws Exception {
    String json = """
        {
          "terms": { "url": "https://example.com" },
          "seriesDetail": {},
          "observations": [
            {
              "d": "2024-01-02",
              "FXUSDCAD": { "v": "1.3242" }
            }
          ]
        }
        """;

    BankOfCanadaFxRateResponse response = objectMapper.readValue(json, BankOfCanadaFxRateResponse.class);

    assertThat(response.getObservations()).hasSize(1);
    assertThat(response.getObservations().get(0).getSeriesValues().get("FXUSDCAD").getValue())
        .isEqualTo("1.3242");
  }
}