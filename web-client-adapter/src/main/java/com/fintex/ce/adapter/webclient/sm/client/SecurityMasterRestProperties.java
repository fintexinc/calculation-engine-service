package com.fintex.ce.adapter.webclient.sm.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "external-services.security-master.rest")
public class SecurityMasterRestProperties {

  private String baseUrl;
  private int timeout = 90000;
  private boolean logBody = false;
  private boolean logRequests = false;
}
