package api.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ManualPropertyModel {

    @JsonProperty("fds-url")
    private String fdsUrl;
    @JsonProperty("rest")
    private RestPropertyModel rest;
    @JsonProperty("versions")
    private VersionModel versions;

}
