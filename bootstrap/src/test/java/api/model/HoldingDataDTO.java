package api.model;

import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.enumeration.InterestFreq;
import com.fintex.sm.model.domain.SecurityIdentifier;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class HoldingDataDTO {

  private String holdingCode;
  private HoldingType holdingType;
  private String exchangeCode;
  private SecurityIdentifier securityIdentifier;
  private LocalDate gicInvestmentDate;
  private BigDecimal gicClientIntRate;
  private InterestFreq gicInterestFreq;
  private BigDecimal gicTerm;
  private String gicName;

  public HoldingDataDTO(String holdingCode, HoldingType holdingType, String exchangeCode) {
    this.holdingCode = holdingCode;
    this.holdingType = holdingType;
    this.exchangeCode = exchangeCode;
  }

}
