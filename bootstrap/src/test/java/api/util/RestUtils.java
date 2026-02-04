package api.util;

import api.config.ApigeeTokenService;
import api.config.RestPropertyModel;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fintex.ce.util.JacksonUtil;
import com.fintex.ce.framework.httpclient.assertion.AssertionProvider;
import com.fintex.ce.framework.httpclient.clients.restassured.RestAssuredConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.util.Objects;

@Slf4j
public class RestUtils {

  private RestUtils() {
  }

  public static <T> T executeAppRequest(final String resourcePath, final Object reqDTO, final HttpStatus httpStatus,
      final TypeReference<T> resType) {
    final String responseStr = executeAppRequest(resourcePath, reqDTO, httpStatus);
    return JacksonUtil.deserialize(responseStr, resType);
  }

  public static String executeAppRequest(final String resourcePath, final Object reqDTO, final HttpStatus httpStatus) {
    final RestPropertyModel restProperties = TestProperties.getRest();
    final String requestJson = JacksonUtil.serialize(reqDTO);
    final String baseUrl = restProperties.getBaseUrl();
    final Boolean isSecurityEnabled = restProperties.getIsSecurityEnabled();

    log.info("actual request to ce, url: {}, body: {}", (baseUrl + resourcePath), requestJson);

    AssertionProvider post;
    if (Objects.nonNull(isSecurityEnabled) && isSecurityEnabled) {
      post = RestAssuredConfiguration.httpClient(baseUrl)
          .header(HttpHeaders.AUTHORIZATION, "Bearer " + ApigeeTokenService.getApigeeToken())
          .post(requestJson, resourcePath);
    } else {
      post = RestAssuredConfiguration.httpClient(baseUrl)
          .post(requestJson, resourcePath);
    }

    return post
        .expectedStatusCode(httpStatus.value())
        .getContentAsString();
  }

}
