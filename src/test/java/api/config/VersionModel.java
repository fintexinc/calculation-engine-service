package api.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class VersionModel {

    @JsonProperty("enable-ce-2.0")
    public boolean enableCE2x0;
    @JsonProperty("enable-ce-1.5")
    public boolean enableCE1x5;
    @JsonProperty("enable-ce-1.3")
    public boolean enableCE1x3;

}
