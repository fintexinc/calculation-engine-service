package api.model;

public enum HoldingsDataColumnType {
  HOLDING_CODE("holdingCode"),
  HOLDING_TYPE("holdingType"),
  EXCHANGE_ID("exchangeID"),
  HOLDING_IDENTIFIER("HoldingIdentifier");

  private String value;

  HoldingsDataColumnType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
