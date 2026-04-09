package com.fintex.ce.domain.model.holding;

import com.fintex.sm.model.domain.EquitySecurityIdentifier;
import com.fintex.sm.model.domain.SecurityIdentifier;
import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.Objects;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import static com.fintex.ce.domain.constant.ErrorMessage.NOT_NULL_MSG;
import static com.fintex.ce.domain.util.BigDecimalUtils.bigDecimalEquals;
import static com.fintex.ce.domain.util.BigDecimalUtils.bigDecimalHashCode;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "holdingType", visible = true, defaultImpl = Holding.class)
@JsonSubTypes({
    @JsonSubTypes.Type(value = CashHolding.class, name = "CASH"),
    @JsonSubTypes.Type(value = GicHolding.class, name = "GIC")
})
@Getter
@ToString
@SuperBuilder(toBuilder = true)
public class Holding {

  public static final String DELIMITER = "-";

  private final BigDecimal value;

  @NotNull(message = NOT_NULL_MSG)
  private final FinancialInstrumentType holdingType;

  @NotNull(message = NOT_NULL_MSG)
  @Valid
  private final SecurityIdentifier securityIdentifier;

  @JsonCreator
  public Holding(
      @JsonProperty("value") BigDecimal value,
      @JsonProperty("holdingType") FinancialInstrumentType holdingType,
      @JsonProperty("securityIdentifier") SecurityIdentifier securityIdentifier) {
    this.value = value;
    this.holdingType = holdingType;
    this.securityIdentifier = securityIdentifier;
  }

  public String generateUserIdentifier() {
    if (securityIdentifier == null) {
      return holdingType + DELIMITER + value;
    }
    if (securityIdentifier instanceof EquitySecurityIdentifier eq) {
      return holdingType + DELIMITER + securityIdentifier.getId() + DELIMITER + eq.getExchangeId();
    }
    return holdingType + DELIMITER + securityIdentifier.getId();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Holding holding = (Holding) o;
    return bigDecimalEquals(value, holding.value)
        && Objects.equals(holdingType, holding.holdingType)
        && Objects.equals(securityIdentifier, holding.securityIdentifier);
  }

  @Override
  public int hashCode() {
    int result = Objects.hashCode(holdingType);
    result = 31 * result + Objects.hashCode(securityIdentifier);
    result = 31 * result + bigDecimalHashCode(value);
    return result;
  }

}
