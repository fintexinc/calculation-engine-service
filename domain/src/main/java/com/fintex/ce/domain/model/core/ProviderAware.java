package com.fintex.ce.domain.model.core;

/**
 * Interface for domain models that have data provider information. This is used by DataProviderRequestHandlingValidator
 * to validate responses from data providers.
 */
public interface ProviderAware {

  /**
   * Gets the data provider name/identifier.
   * 
   * @return the provider string
   */
  String getProvider();

}