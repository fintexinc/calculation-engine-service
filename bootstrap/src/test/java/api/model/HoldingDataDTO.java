package api.model;

import com.fintex.ce.domain.model.enumeration.InterestFreq;
import com.fintex.sm.model.domain.SecurityIdentifier;
import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class HoldingDataDTO {

  private String holdingCode;
  private FinancialInstrumentType holdingType;
  private String exchangeCode;
  private SecurityIdentifier securityIdentifier;
  private LocalDate gicInvestmentDate;
  private BigDecimal gicClientIntRate;
  private InterestFreq gicInterestFreq;
  private BigDecimal gicTerm;
  private String gicName;

  public HoldingDataDTO(String holdingCode, FinancialInstrumentType holdingType, String exchangeCode) {
    this.holdingCode = holdingCode;
    this.holdingType = holdingType;
    this.exchangeCode = exchangeCode;
  }

}
