package com.fintex.ce.adapter.webclient.sm.fetcher;

import com.fintex.ce.adapter.webclient.sm.client.SecurityMasterWebClient;
import com.fintex.wm.commons.domain.attribute.SecurityAttributeBundle;
import com.fintex.wm.commons.domain.attribute.SecurityAttributeResult;
import com.fintex.wm.commons.domain.financial.Geography;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;

import java.util.List;

@Configuration
@ConditionalOnProperty(name = "external-services.security-master.api-type", havingValue = "rest", matchIfMissing = true)
public class GeographyFetchers {

  @Bean
  AbstractSecurityMasterFetcher<Geography, Geography> geographyFetcher(
      SecurityMasterWebClient client,
      @Value("${external-services.security-master.rest.endpoints.geography}") String endpointPath) {
    return new AbstractSecurityMasterFetcher<>(client, endpointPath, (response, holding) -> response,
        new ParameterizedTypeReference<List<SecurityAttributeResult<Geography>>>() {},
        SecurityAttributeBundle.GEOGRAPHY, Geography.class) {};
  }
}
