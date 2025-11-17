package api.model;

import com.fintex.ce.config.enumeration.HoldingIdentifierType;
import com.fintex.ce.config.enumeration.HoldingType;
import com.fintex.ce.config.enumeration.InterestFreq;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class HoldingDataDTO {

    private String holdingCode;
    private HoldingType holdingType;
    private String exchangeCode;
    private HoldingIdentifierType holdingIdentifier;
    private LocalDate gicInvestmentDate;
    private BigDecimal gicClientIntRate;
    private InterestFreq gicInterestFreq;
    private BigDecimal gicTerm;
    private String gicName;

    public HoldingDataDTO() {
    }

    public HoldingDataDTO(String holdingCode, HoldingType holdingType, String exchangeCode) {
        this.holdingCode = holdingCode;
        this.holdingType = holdingType;
        this.exchangeCode = exchangeCode;
    }

    public HoldingDataDTO(String holdingCode, HoldingType holdingType, HoldingIdentifierType holdingIdentifier) {
        this.holdingCode = holdingCode;
        this.holdingType = holdingType;
        this.holdingIdentifier = holdingIdentifier;
    }

    public HoldingDataDTO(String holdingCode, HoldingType holdingType, String exchangeCode, HoldingIdentifierType holdingIdentifier) {
        this.holdingCode = holdingCode;
        this.holdingType = holdingType;
        this.exchangeCode = exchangeCode;
        this.holdingIdentifier = holdingIdentifier;
    }
}

