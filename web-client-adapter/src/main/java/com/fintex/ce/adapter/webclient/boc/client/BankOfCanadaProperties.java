package com.fintex.ce.adapter.webclient.boc.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = BankOfCanadaProperties.PREFIX)
public class BankOfCanadaProperties {

  /** Prefix this service's configuration is bound from, and the root of {@link #BASE_URL_PROPERTY}. */
  public static final String PREFIX = "external-services.bank-of-canada";

  /**
   * Full name of the endpoint property, named here beside the field it binds to because every test that stands a mock
   * server in for the vendor has to register its URL under exactly this key. Spelled out in each of those tests, the
   * key is one rename away from silently pointing the client back at the live Bank of Canada.
   */
  public static final String BASE_URL_PROPERTY = PREFIX + ".base-url";

  private String baseUrl = "https://www.bankofcanada.ca/valet";
  private int timeout = 30000;
  private boolean logBody = false;
  private boolean logRequests = false;
  private String healthCheckPath;
  private Map<String, CurrencyPairConfig> currencyPairs = new HashMap<>();

  @Data
  public static class CurrencyPairConfig {
    private List<FxRateSource> rateSources;
  }
}