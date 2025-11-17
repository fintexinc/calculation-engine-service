package api.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class RestPropertyModel {

    @JsonProperty("application-url")
    private String baseUrl;
    @JsonProperty("application-1x5-url")
    private String ce1x5URL;
    @JsonProperty("application-1x3-url")
    private String ce1x3URL;
    @JsonProperty("content-type")
    private String contentType;
    @JsonProperty("security_enable")
    private Boolean isSecurityEnabled;
    @JsonProperty("client_id")
    private String clientId;
    @JsonProperty("client_secret")
    private String clientSecret;
    @JsonProperty("token-url")
    private String tokenUrl;
    @JsonProperty("api-v1x5")
    private ApiPropertyModelV1x5 apiV1x5;

}
