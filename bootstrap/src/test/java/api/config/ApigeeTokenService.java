package api.config;

import api.dto.config.OAuth2Token;
import api.util.TestProperties;
import com.google.common.base.Strings;
import com.fintex.ce.domain.exception.SystemException;
import com.fintex.ce.domain.exception.code.ErrorCode;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

public class ApigeeTokenService {
  private static final RestTemplate REST_TEMPLATE = new RestTemplate();

  public final static String APIGEE_OAUTH2_TOKEN = "apigee.oAuth2.token";
  public final static String APIGEE_EXPIRES = "apigee.expires";
  public static final String CLIENT_CREDENTIALS = "client_credentials";

  private static String queryOAuthToken() {
    final RestPropertyModel restProperties = TestProperties.getRest();
    final String url = restProperties.getTokenUrl();
    final HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    final MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("client_id", restProperties.getClientId());
    map.add("client_secret", restProperties.getClientSecret());
    map.add("grant_type", CLIENT_CREDENTIALS);

    final HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);
    try {
      final ResponseEntity<OAuth2Token> responseEntity = REST_TEMPLATE.exchange(url, HttpMethod.POST, entity,
          OAuth2Token.class);
      final String token = responseEntity.getBody().getAccessToken();
      final Long expires = responseEntity.getBody().getExpiresIn() * 1000 + System.currentTimeMillis();
      System.setProperty(APIGEE_OAUTH2_TOKEN, token);
      System.setProperty(APIGEE_EXPIRES, Long.toString(expires));

      return token;
    } catch (final RestClientResponseException ex) {
      throw new SystemException(String.format("Apigee error response status %s and body %s", ex.getStatusText(), ex
          .getResponseBodyAsString()),
          ErrorCode.INTERNAL_SERVER_ERROR);
    }
  }

  public static synchronized String getApigeeToken() {
    if (Strings.isNullOrEmpty(System.getProperty(APIGEE_OAUTH2_TOKEN)) || System.currentTimeMillis() > Long.parseLong(
        System.getProperty(APIGEE_EXPIRES))) {
      return queryOAuthToken();
    } else {
      return System.getProperty(APIGEE_OAUTH2_TOKEN);
    }
  }
}
