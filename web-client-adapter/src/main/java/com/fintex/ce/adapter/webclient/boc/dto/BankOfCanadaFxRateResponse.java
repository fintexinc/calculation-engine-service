package com.fintex.ce.adapter.webclient.boc.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BankOfCanadaFxRateResponse {

  private List<Observation> observations;

  @Data
  @NoArgsConstructor
  public static class Observation {

    @JsonProperty("d")
    private String date;

    private final Map<String, SeriesValue> seriesValues = new LinkedHashMap<>();

    @JsonAnySetter
    public void setDynamicProperty(String key, Object value) {
      if (value instanceof Map<?, ?> map && map.containsKey("v")) {
        var seriesValue = new SeriesValue();
        seriesValue.setValue(String.valueOf(map.get("v")));
        seriesValues.put(key, seriesValue);
      }
    }
  }

  @Data
  @NoArgsConstructor
  public static class SeriesValue {

    @JsonProperty("v")
    private String value;
  }
}