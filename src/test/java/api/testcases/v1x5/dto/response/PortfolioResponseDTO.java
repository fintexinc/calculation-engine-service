package api.testcases.v1x5.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PortfolioResponseDTO {

    @JsonProperty("arrayValue")
    private List<ResDTO> response;

    @Data
    @NoArgsConstructor
    public static class ResDTO {
        @JsonProperty("key")
        private String key;
        @JsonProperty("value")
        private BigDecimal value;
        @JsonProperty("drawdownStartDate")
        private String drawdownStartDate;
        @JsonProperty("drawdownTroughDate")
        private String  drawdownTroughDate;
        @JsonProperty("recoveryTime")
        private int recoveryTime;
    }
}
