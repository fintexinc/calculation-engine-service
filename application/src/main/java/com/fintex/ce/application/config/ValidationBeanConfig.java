package com.fintex.ce.application.config;

import com.fintex.ce.util.validation.data.AssetAllocationDataValidator;
import com.fintex.ce.util.validation.data.DataProviderChecker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for validation beans from the api module.
 */
@Configuration
public class ValidationBeanConfig {

  @Bean
  public AssetAllocationDataValidator assetAllocationDataValidator() {
    return new AssetAllocationDataValidator();
  }

  @Bean
  public DataProviderChecker dataProviderChecker() {
    return new DataProviderChecker();
  }
}
