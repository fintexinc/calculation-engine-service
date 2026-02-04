package com.fintex.ce.domain.model;

import java.util.List;

/**
 * Common interface for all domain models that can be returned from GraphQL queries. This interface defines the common
 * fields needed by the repository layer.
 */
public interface DomainModel {

  String getHoldingId();
  void setHoldingId(String holdingId);

  String getProvider();
  void setProvider(String provider);

  String getProviders();
  void setProviders(String providers);

  List<ValidationError> getErrors();
  boolean hasErrors();

}
