package com.fintex.ce.adapter.webclient.sm.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "external-services.security-master.rest")
public class SecurityMasterRestProperties {

  private String baseUrl;
  private int timeout = 90000;
  private boolean logBody = false;
  private boolean logRequests = false;
}
