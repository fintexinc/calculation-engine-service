package com.fintex.ce.adapter.rest.dto.response.correlation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fintex.ce.adapter.rest.dto.response.commonholdings.ParentHoldingDTO;
import com.fintex.ce.domain.model.enumeration.Currency;
import com.fintex.ce.domain.model.holding.CashHolding;
import com.fintex.ce.domain.model.holding.GicHolding;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.util.FilterUtils;
import com.fintex.sm.model.domain.SecurityIdentifier;
import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import static com.fintex.ce.util.PortfolioUtils.createKey;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HoldingsKeyDTO {

  private FinancialInstrumentType type;
  private SecurityIdentifier securityIdentifier;
  private String key;
  private BigDecimal allocation;
  private String name;
  private Currency currency;

  public static HoldingsKeyDTO buildHoldingsKeyDTO(final Holding holding) {
    return buildDTO(holding, new HoldingsKeyDTO());
  }

  public static HoldingsKeyDTO buildParentKeyDTO(final Holding holding) {
    return buildDTO(holding, new ParentHoldingDTO());
  }

  private static HoldingsKeyDTO buildDTO(final Holding holding, final HoldingsKeyDTO holdingsKeyDTO) {
    holdingsKeyDTO.setType(holding.getHoldingType());
    holdingsKeyDTO.setSecurityIdentifier(holding.getSecurityIdentifier());

    if (FilterUtils.CASH_PREDICATE.test(holding)) {
      holdingsKeyDTO.setCurrency(((CashHolding) holding).getCurrency());
    } else if (FilterUtils.GIC_PREDICATE.test(holding)) {
      holdingsKeyDTO.setName(((GicHolding) holding).getName());
    }

    holdingsKeyDTO.setKey(createKey(holding));
    return holdingsKeyDTO;
  }

}
