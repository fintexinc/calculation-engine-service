package com.fintex.ce.domain.enumeration;

public enum HoldingIdentifierType {

  FUNDSERV,
  TICKER,
  SYMBOL,
  MORNINGSTAR_ID,
  BROADRIDGE_ADP_NUMBER,
  ENVESTNET_ID;

  public static HoldingIdentifierType of(final String type) {
    for (HoldingIdentifierType identifierType : values()) {
      if (identifierType.name().equalsIgnoreCase(type)) {
        return identifierType;
      }
    }
    return null;
  }
}
