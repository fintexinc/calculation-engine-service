package com.fintex.ce.framework.httpclient.clients.restassured;

import com.fintex.ce.framework.security.SessionHeaderProvider;
import com.fintex.ce.framework.httpclient.HttpClientProvider;
import com.fintex.ce.framework.utils.Logger;
import io.restassured.RestAssured;
import io.restassured.authentication.PreemptiveBasicAuthScheme;

import java.util.StringTokenizer;

public class RestAssuredConfiguration {
  private static final String proxyData;
  private static final String proxyUrl;
  private static final int port;
  private static final String proxyAuthToken;

  static {
    proxyData = System.getProperty("ProxyData");
    if (proxyData != null) {
      StringTokenizer data = new StringTokenizer(proxyData, ";");
      proxyUrl = data.nextToken();
      port = Integer.parseInt(data.nextToken());
      PreemptiveBasicAuthScheme auth = new PreemptiveBasicAuthScheme();
      auth.setUserName(data.nextToken());
      auth.setPassword(data.nextToken());
      proxyAuthToken = auth.generateAuthToken();
    } else {
      proxyUrl = null;
      port = 0;
      proxyAuthToken = null;

    }
    if (System.getProperty("httpInsecure") != null && System.getProperty("httpInsecure").equalsIgnoreCase("true"))
      RestAssured.useRelaxedHTTPSValidation();
  }

  public static HttpClientProvider httpClient(SessionHeaderProvider headerProvider) {
    if (System.getProperty("ProxyData") != null) {
      try {
        return RestAssuredHttpClientProvider.getDynamicAuthInstance(headerProvider, proxyUrl, port, proxyAuthToken);
      } catch (Exception e) {
        Logger.error(
            "'ProxyData' env value should be <proxyURL>;<port>;<userName>;<password> use of char ';,:' should be avoided in passed value ");
        return null;
      }

    } else {
      return RestAssuredHttpClientProvider.getDynamicAuthInstance(headerProvider);
    }
  }

  public static HttpClientProvider httpClient() {
    if (System.getProperty("ProxyData") != null) {
      try {
        return RestAssuredHttpClientProvider.getInstance(proxyUrl, port, proxyAuthToken);
      } catch (Exception e) {
        Logger.error(
            "'ProxyData' env value should be <proxyURL>;<port>;<userName>;<password> use of char ';,:' should be avoided in passed value ");
        return null;
      }

    } else {
      return RestAssuredHttpClientProvider.getInstance();
    }
  }

  public static HttpClientProvider httpClient(String baseUrl) {
    if (System.getProperty("ProxyData") != null) {
      try {
        return httpClient(baseUrl, proxyUrl, port, proxyAuthToken);
      } catch (Exception e) {
        Logger.error(
            "'ProxyData' env value should be <proxyURL>;<port>;<userName>;<password> use of char ';,:' should be avoided in passed value ");
        return null;
      }

    } else {
      return RestAssuredHttpClientProvider.getInstance(baseUrl);
    }
  }

  public static HttpClientProvider httpClient(String baseUrl, String proxyUrl, int port, String proxyAuthToken) {

    return RestAssuredHttpClientProvider.getInstance(baseUrl, proxyUrl, port, proxyAuthToken);
  }

}
