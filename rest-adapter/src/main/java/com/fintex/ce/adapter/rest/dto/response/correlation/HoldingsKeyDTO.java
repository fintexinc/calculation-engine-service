package com.fintex.ce.adapter.rest.dto.response.correlation;

import com.fintex.ce.adapter.rest.dto.response.commonholdings.ParentHoldingDTO;
import com.fintex.ce.model.domain.holding.CashHolding;
import com.fintex.ce.model.domain.holding.GicHolding;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.util.FilterUtils;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import com.fasterxml.jackson.annotation.JsonInclude;

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

  public static HoldingsKeyDTO buildHoldingsKeyDTO(final PortfolioHolding holding) {
    return buildDTO(holding, new HoldingsKeyDTO());
  }

  public static HoldingsKeyDTO buildParentKeyDTO(final PortfolioHolding holding) {
    return buildDTO(holding, new ParentHoldingDTO());
  }

  private static HoldingsKeyDTO buildDTO(final PortfolioHolding holding, final HoldingsKeyDTO holdingsKeyDTO) {
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
