package api.testcases.v1x5.dto.request;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
public class PortfolioHoldingDTO {

  private String holdingType;
  private String holdingCode;
  private BigDecimal value;
  private String exchangeId;
  private String fundHoldingIdentifier;
  private String equitySearchIdentifierType;
  private String performanceCurrencyEnum;

}
