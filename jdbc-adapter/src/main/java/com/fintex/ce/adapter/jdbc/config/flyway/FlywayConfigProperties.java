package com.fintex.ce.adapter.jdbc.config.flyway;

import com.fintex.ce.adapter.jdbc.config.DataSourceProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "flyway")
public class FlywayConfigProperties {

  private String schemas;
  private String table;
  private String sqlMigrationSuffixes;
  private DataSourceProperties datasource;
  private String sqlMigrationPrefix;
  private List<String> locations;

}
