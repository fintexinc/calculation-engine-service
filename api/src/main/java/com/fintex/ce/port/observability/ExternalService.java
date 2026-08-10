package com.fintex.ce.port.observability;

/**
 * The external data providers this service calls out to, and the identifier each one contributes to the observability
 * of those calls.
 *
 * <p>
 * Naming them here rather than in each client keeps the values enumerable and removes the chance of two call sites
 * spelling the same provider differently and splitting its series in two.
 */
public enum ExternalService {

  SECURITY_MASTER("security-master"),
  BANK_OF_CANADA("bank-of-canada");

  private final String id;

  ExternalService(String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }
}
