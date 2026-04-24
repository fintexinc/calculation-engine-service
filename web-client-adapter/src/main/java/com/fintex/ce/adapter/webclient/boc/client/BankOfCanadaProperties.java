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
@ConfigurationProperties(prefix = "external-services.bank-of-canada")
public class BankOfCanadaProperties {

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