package ca.tangerine.pce.webclient.boc.dto;

import ca.tangerine.pce.webclient.observability.CountedResponse;

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
public class BankOfCanadaFxRateResponse implements CountedResponse {

  private List<Observation> observations;

  @Override
  public int itemCount() {
    return observations == null ? 0 : observations.size();
  }

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