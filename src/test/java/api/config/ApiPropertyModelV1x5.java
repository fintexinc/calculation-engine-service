package api.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ApiPropertyModelV1x5 {
    @JsonProperty("maxdrawdown")
    private String maxdrawdown;
    @JsonProperty("leading-returns")
    private String leadingReturns;
}
