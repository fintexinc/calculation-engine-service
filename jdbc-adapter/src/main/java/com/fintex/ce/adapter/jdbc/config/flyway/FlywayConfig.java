package com.fintex.ce.adapter.jdbc.config.flyway;

import com.fintex.ce.adapter.jdbc.config.DataSourceProperties;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FlywayConfig {

  private final List<? extends BaseJavaMigration> javaMigrations;
  private final FlywayConfigProperties flywayConfigProperties;

  @Value("${spring.datasource.url}")
  private String dbUrl;

  @EventListener(ApplicationStartedEvent.class)
  public void runFlywayMigrations() {
    final DataSourceProperties dataSourceProperties = flywayConfigProperties.getDatasource();
    Flyway.configure()
        .dataSource(dbUrl, dataSourceProperties.getUsername(), dataSourceProperties.getPassword())
        .baselineOnMigrate(true)
        .schemas(flywayConfigProperties.getSchemas())
        .sqlMigrationPrefix(flywayConfigProperties.getSqlMigrationPrefix())
        .sqlMigrationSuffixes(flywayConfigProperties.getSqlMigrationSuffixes())
        .placeholderReplacement(false)
        .table(flywayConfigProperties.getTable())
        .locations(flywayConfigProperties.getLocations().toArray(String[]::new))
        .javaMigrations(javaMigrations.toArray(BaseJavaMigration[]::new))
        .load()
        .migrate();
  }

}
