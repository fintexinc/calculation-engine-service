package com.fintex.ce.framework.httpclient.clients.restassured;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.restassured.OpenApiValidationFilter;
import com.fintex.ce.framework.model.Pair;
import com.fintex.ce.framework.security.SessionHeaderProvider;
import com.fintex.ce.framework.exceptions.InternalTestGeneralException;
import com.fintex.ce.framework.httpclient.HttpClientProvider;
import com.fintex.ce.framework.httpclient.assertion.AssertionProvider;
import com.fintex.ce.framework.httpclient.assertion.implementation.RestAssuredAssertionProvider;
import io.restassured.RestAssured;
import io.restassured.config.ConnectionConfig;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.Header;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import joptsimple.internal.Strings;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;

import java.io.InputStream;

import static com.fintex.ce.framework.common.properties.PropertiesHolder.getPropertyAsString;

public class RestAssuredHttpClientProvider implements HttpClientProvider {
//    static {
//        RestAssured.config = getRestAssuredConfig();
//    }

    private final RequestSpecification baseConf;

    private RestAssuredHttpClientProvider(RequestSpecification baseConf) {
        this.baseConf = baseConf;
    }

    public static RestAssuredHttpClientProvider getInstance() {
        return new RestAssuredHttpClientProvider(RestAssured.given()
                .config(getRestAssuredConfig())
                .baseUri(getPropertyAsString("manual.rest.base-url"))
                .contentType("application/json"));
    }

    public static RestAssuredHttpClientProvider getInstance(String proxyUrl, int port, String proxyAuthToken) {

        return new RestAssuredHttpClientProvider(RestAssured.given()
                .config(getRestAssuredConfig())
                .header("Proxy-Authorization", proxyAuthToken)
                .proxy(proxyUrl, port)
                .baseUri(getPropertyAsString("manual.rest.base-url"))
                .contentType("application/json"));
    }

    public static RestAssuredHttpClientProvider getInstance(String baseUrl, String proxyUrl, int port, String proxyAuthToken) {

        return new RestAssuredHttpClientProvider(
                RestAssured.given()
                        .config(getRestAssuredConfig())
                        .header("Proxy-Authorization", proxyAuthToken)
                        .proxy(proxyUrl, port)
                        .baseUri(baseUrl)
                        .contentType("application/json")
        );
    }

    public static RestAssuredHttpClientProvider getInstance(String baseUrl) {

        return new RestAssuredHttpClientProvider(
                RestAssured.given()
                        .config(getRestAssuredConfig())
                        .baseUri(baseUrl)
                        .contentType("application/json")
        );
    }

    public static RestAssuredHttpClientProvider getDynamicAuthInstance(SessionHeaderProvider headerProvider) {
        return new RestAssuredHttpClientProvider(
                DefaultConfiguration.obtainBaseConfiguration()
                        .header(DefaultConfiguration.obtainAuthHeaders(headerProvider))
        );
    }

    public static RestAssuredHttpClientProvider getDynamicAuthInstance(SessionHeaderProvider headerProvider, String proxyUrl, int port, String proxyAuthToken) {
        return new RestAssuredHttpClientProvider(
                DefaultConfiguration.obtainBaseConfiguration()
                        .header(DefaultConfiguration.obtainAuthHeaders(headerProvider))
                        .header("Proxy-Authorization", proxyAuthToken)
                        .proxy(proxyUrl, port)
        );
    }

    private static RestAssuredConfig getRestAssuredConfig() {
        ConnectionKeepAliveStrategy myStrategy = (response, context) -> {
            HeaderElementIterator it = new BasicHeaderElementIterator
                    (response.headerIterator(HTTP.CONN_KEEP_ALIVE));
            while (it.hasNext()) {
                HeaderElement he = it.nextElement();
                String param = he.getName();
                String value = he.getValue();
                if (value != null && param.equalsIgnoreCase
                        ("timeout")) {
                    return Long.parseLong(value) * 1000;
                }
            }
            return 120 * 1000;
        };

        PoolingClientConnectionManager connManager
                = new PoolingClientConnectionManager();
        connManager.setMaxTotal(120);
        connManager.setDefaultMaxPerRoute(120);
        DefaultHttpClient defaultHttpClient = new DefaultHttpClient(connManager);

        defaultHttpClient.setKeepAliveStrategy(myStrategy);

        return RestAssured
                .config()
                .httpClient(HttpClientConfig.httpClientConfig().httpClientFactory(() -> defaultHttpClient))
                .connectionConfig(new ConnectionConfig());
    }

    @Override
    public HttpClientProvider useSwaggerHubSchema() {
        final RequestSpecification conf = baseConf
                .filter(ValidationFilter.OPEN_API_VALIDATION_FILTER);
        return new RestAssuredHttpClientProvider(conf);
    }

    @Override
    public HttpClientProvider param(Pair<String, ?> pair) {
        final RequestSpecification conf = baseConf
                .queryParam(pair.getKey(), pair.getValue());
        return new RestAssuredHttpClientProvider(conf);
    }

    @Override
    public HttpClientProvider param(Pair<String, ?>... pairs) {
        for (Pair<String, ?> pair : pairs) {
            baseConf.queryParam(pair.getKey(), pair.getValue());
        }
        return new RestAssuredHttpClientProvider(baseConf);
    }

    @Override
    public HttpClientProvider param(String name, Object value) {
        final RequestSpecification conf = baseConf
                .queryParam(name, value);
        return new RestAssuredHttpClientProvider(conf);
    }

    @Override
    public HttpClientProvider header(String name, Object value) {
        final RequestSpecification conf = baseConf
                .header(name, value);
        return new RestAssuredHttpClientProvider(conf);
    }

    @Override
    public AssertionProvider post(String json, String url) {
        final Response post = baseConf
                .when()
                .body(json)
                .post(url);
        return new RestAssuredAssertionProvider(post);
    }

    @Override
    public AssertionProvider post(InputStream json, String url) {
        final Response post = baseConf
                .when()
                .body(json)
                .post(url);
        return new RestAssuredAssertionProvider(post);
    }

    @Override
    public AssertionProvider post(Object json, String url) {
        final Response post = baseConf
                .when()
                .body(json)
                .post(url);
        return new RestAssuredAssertionProvider(post);
    }

    @Override
    public AssertionProvider get(String url) {
        final Response get = baseConf
                .when()
                .get(url);
        return new RestAssuredAssertionProvider(get);
    }

    @Override
    public AssertionProvider put(String url) {
        final Response get = baseConf
                .when()
                .put(url);
        return new RestAssuredAssertionProvider(get);
    }

    @Override
    public AssertionProvider put(String json, String url) {
        final Response get = baseConf
                .when()
                .body(json)
                .put(url);
        return new RestAssuredAssertionProvider(get);
    }

    private static class DefaultConfiguration {
        static RequestSpecification obtainBaseConfiguration() {
            return RestAssured.given()
                    .config(getRestAssuredConfig())
                    .baseUri(getPropertyAsString("manual.rest.base-url"))
                    .contentType(getPropertyAsString("manual.rest.content-type"));
        }

        static Header obtainAuthHeaders(SessionHeaderProvider headerProvider) {
            // just to update JWT token
            return new Header(
                    String.valueOf(headerProvider.getSessionToken().getKey()),
                    String.valueOf(headerProvider.getSessionToken().getValue())
            );
        }
    }

    private static class ValidationFilter {
        static final OpenApiValidationFilter OPEN_API_VALIDATION_FILTER;

        static {
            OPEN_API_VALIDATION_FILTER = loadSwaggerFilter();
        }

        private static OpenApiValidationFilter loadSwaggerFilter() {
            final String pathInFile = "manual.swagger.schema-url";
            final String swaggerURL = getPropertyAsString(pathInFile);

            if (Strings.isNullOrEmpty(swaggerURL)) {
                throw new InternalTestGeneralException("Swagger URL is invalid by path: " + pathInFile);
            }

            final String apiKey = getPropertyAsString("manual.swagger.api-key");
            if (Strings.isNullOrEmpty(apiKey)) {
                // if key is not defined then do not use authentication
                return new OpenApiValidationFilter(swaggerURL);
            }

            final OpenApiInteractionValidator build = OpenApiInteractionValidator
                    .createFor(swaggerURL)
                    .withAuthHeaderData("Authorization", apiKey)
                    .build();
            return new OpenApiValidationFilter(build);
        }


    }

}
