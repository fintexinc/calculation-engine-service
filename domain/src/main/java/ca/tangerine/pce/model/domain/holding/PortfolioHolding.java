package ca.tangerine.pce.model.domain.holding;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import static ca.tangerine.pce.model.error.ErrorCode.Codes.FIELD_NOT_NULL;

import ca.tangerine.wm.commons.domain.enumeration.Country;
import ca.tangerine.wm.commons.domain.enumeration.FinancialInstrumentType;
import ca.tangerine.wm.commons.domain.id.EquitySecurityIdentifier;
import ca.tangerine.wm.commons.domain.id.SecurityIdentifier;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "holdingType", visible = true, defaultImpl = PortfolioHolding.class)
@JsonSubTypes({
    @JsonSubTypes.Type(value = CashHolding.class, name = "CASH"),
    @JsonSubTypes.Type(value = GicHolding.class, name = "GIC")
})
@Getter
@ToString
@EqualsAndHashCode
@SuperBuilder(toBuilder = true)
public sealed class PortfolioHolding permits CashHolding, GicHolding {

  public static final String DELIMITER = "-";

  private final BigDecimal value;

  @NotNull(message = FIELD_NOT_NULL)
  private final FinancialInstrumentType holdingType;

  private final Country country;

  @NotNull(message = FIELD_NOT_NULL)
  @Valid
  private final SecurityIdentifier securityIdentifier;

  @JsonCreator
  public PortfolioHolding(
      @JsonProperty("value") BigDecimal value,
      @JsonProperty("holdingType") FinancialInstrumentType holdingType,
      @JsonProperty("country") Country country,
      @JsonProperty("securityIdentifier") SecurityIdentifier securityIdentifier) {
    this.value = value;
    this.holdingType = holdingType;
    this.country = country;
    this.securityIdentifier = securityIdentifier;
  }

  public PortfolioHolding(BigDecimal value, FinancialInstrumentType holdingType,
      SecurityIdentifier securityIdentifier) {
    this(value, holdingType, null, securityIdentifier);
  }

  public String getIdsString() {
    if (securityIdentifier == null) {
      return holdingType + DELIMITER + value;
    }
    if (securityIdentifier instanceof EquitySecurityIdentifier eq) {
      return holdingType + DELIMITER + securityIdentifier.getId() + DELIMITER + eq.getExchangeId();
    }
    return holdingType + DELIMITER + securityIdentifier.getId();
  }

}