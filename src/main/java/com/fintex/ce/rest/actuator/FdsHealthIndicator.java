package com.fintex.ce.rest.actuator;

import com.fintex.smclient.config.properties.GraphqlTransportProperties;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Component
public class FdsHealthIndicator implements HealthIndicator {

    private final RestTemplate restTemplate;
    private final GraphqlTransportProperties graphqlTransportProperties;

    public FdsHealthIndicator(final RestTemplate restTemplate,
                              final GraphqlTransportProperties graphqlTransportProperties) {
        this.restTemplate = restTemplate;
        this.graphqlTransportProperties = graphqlTransportProperties;
    }

    @Override
    public Health health() {
        final String fdsActuatorUrl = getFdsBaseUrl() + "/actuator/health";
        final ResponseEntity<String> actuatorResponse = restTemplate.getForEntity(fdsActuatorUrl, String.class);

        return getHealthBuilder(actuatorResponse.getStatusCode())
                .withDetail("STATUS_CODE", actuatorResponse.getStatusCode().value())
                .build();
    }

    protected Health.Builder getHealthBuilder(final HttpStatusCode httpStatus) {
        return httpStatus.is2xxSuccessful() ? Health.up(): Health.down();
    }

    protected String getFdsBaseUrl() {
        final URI fdsGraphqlUrl = URI.create(graphqlTransportProperties.getLocation());
        return fdsGraphqlUrl.getScheme() + "://" + fdsGraphqlUrl.getHost();
    }

}
