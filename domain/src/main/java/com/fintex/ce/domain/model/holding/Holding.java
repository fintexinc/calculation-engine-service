package com.fintex.ce.domain.model.holding;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fintex.sm.model.domain.EquitySecurityIdentifier;
import com.fintex.sm.model.domain.SecurityIdentifier;
import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import static com.fintex.ce.domain.constant.ErrorMessage.NOT_NULL_MSG;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "holdingType", visible = true, defaultImpl = Holding.class)
@JsonSubTypes({
    @JsonSubTypes.Type(value = CashHolding.class, name = "CASH"),
    @JsonSubTypes.Type(value = GicHolding.class, name = "GIC")
})
@Data
@EqualsAndHashCode
@Accessors(chain = true)
public class Holding {

  public static final String DELIMITER = "-";

  @EqualsAndHashCode.Exclude
  private BigDecimal value;

  @NotNull(message = NOT_NULL_MSG)
  private FinancialInstrumentType holdingType;

  @NotNull(message = NOT_NULL_MSG)
  @Valid
  private SecurityIdentifier securityIdentifier;

  public Holding() {
  }

  public Holding(BigDecimal value, FinancialInstrumentType holdingType) {
    this.value = value;
    this.holdingType = holdingType;
  }

  /**
   * Unique id for the end user
   *
   * @return id of the entity
   */
  public String generateUserIdentifier() {
    if (securityIdentifier == null) {
      return holdingType + DELIMITER + value;
    }
    if (securityIdentifier instanceof EquitySecurityIdentifier eq) {
      return holdingType + DELIMITER + securityIdentifier.getId() + DELIMITER + eq.getExchangeId();
    }
    return holdingType + DELIMITER + securityIdentifier.getId();
  }

}
