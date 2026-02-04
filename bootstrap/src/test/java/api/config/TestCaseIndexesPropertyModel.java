package api.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@JsonIgnoreProperties(ignoreUnknown = true)
public class TestCaseIndexesPropertyModel {

  @JsonProperty("smoke-test-case-index-from")
  private Integer smokeTestCaseIndexFrom;
  @JsonProperty("smoke-test-case-index-to")
  private Integer smokeTestCaseIndexTo;
  @JsonProperty("default-test-case-index-from")
  private Integer defaultCaseIndexFrom;
  @JsonProperty("default-test-case-index-to")
  private Integer defaultCaseIndexTo;

}
