package com.fintex.ce.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.lang.NonNull;

import com.azure.monitor.opentelemetry.autoconfigure.AzureMonitorAutoConfigure;
import com.azure.monitor.opentelemetry.autoconfigure.AzureMonitorAutoConfigureOptions;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;

import java.util.Map;

@Configuration
@ConditionalOnClass({AzureMonitorAutoConfigure.class, AutoConfiguredOpenTelemetrySdk.class})
public class AzureMonitorOpenTelemetryConfiguration {

  @Bean(destroyMethod = "close")
  @ConditionalOnMissingBean(OpenTelemetry.class)
  @Conditional(AzureMonitorConnectionStringCondition.class)
  public OpenTelemetrySdk azureMonitorOpenTelemetry(Environment environment) {
    String connectionString = AzureMonitorProperties.azureMonitorConnectionString(environment);
    boolean liveMetricsEnabled = AzureMonitorProperties.liveMetricsEnabled(environment);
    var builder = AutoConfiguredOpenTelemetrySdk.builder()
        .addPropertiesSupplier(() -> azureMonitorProperties(environment, liveMetricsEnabled));
    AzureMonitorAutoConfigure.customize(
        builder,
        new AzureMonitorAutoConfigureOptions().connectionString(connectionString));
    return builder.build().getOpenTelemetrySdk();
  }

  private static Map<String, String> azureMonitorProperties(
      Environment environment,
      boolean liveMetricsEnabled) {
    return Map.of(
        AzureMonitorProperties.OTEL_SERVICE_NAME, AzureMonitorProperties.serviceName(environment),
        AzureMonitorProperties.OTEL_PROPAGATORS, AzureMonitorProperties.propagators(environment),
        AzureMonitorProperties.APPLICATION_INSIGHTS_LIVE_METRICS_ENABLED, Boolean.toString(liveMetricsEnabled));
  }

  static class AzureMonitorConnectionStringCondition implements Condition {

    @Override
    public boolean matches(@NonNull ConditionContext context, @NonNull AnnotatedTypeMetadata metadata) {
      Environment environment = context.getEnvironment();
      return AzureMonitorProperties.azureMonitorEnabled(environment)
          && AzureMonitorProperties.azureMonitorConnectionString(environment) != null;
    }
  }
}
