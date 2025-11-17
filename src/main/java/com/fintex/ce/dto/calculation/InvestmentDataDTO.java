package com.fintex.ce.dto.calculation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fintex.ce.config.enumeration.Frequency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Objects;

import static com.fintex.ce.config.constant.BigDecimalConstants.TEN_THOUSAND;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvestmentDataDTO {

    public static BigDecimal DEFAULT_PURCHASE_AMOUNT = TEN_THOUSAND;
    public static Frequency DEFAULT_PAC_FREQUENCY = Frequency.ANNUAL;
    public static Frequency DEFAULT_WITHDRAWAL_FREQUENCY = Frequency.ANNUAL;
    public static BigDecimal DEFAULT_PAC = BigDecimal.ZERO;
    public static BigDecimal DEFAULT_WITHDRAWAL = BigDecimal.ZERO;

    @JsonProperty("purchaseAmount")
    private BigDecimal purchaseAmount = DEFAULT_PURCHASE_AMOUNT;

    /*
        pac is pre-authorized contribution. Simple put it is contribution.
    */
    @JsonProperty("pac")
    private BigDecimal pac = DEFAULT_PAC;

    @JsonProperty("pacFrequency")
    private Frequency pacFrequency = DEFAULT_PAC_FREQUENCY;

    @JsonProperty("pacIndex")
    private BigDecimal pacIndex = BigDecimal.ZERO;

    @JsonProperty("withdrawal")
    private BigDecimal withdrawal = DEFAULT_WITHDRAWAL;

    @JsonProperty("withdrawalFrequency")
    private Frequency withdrawalFrequency = DEFAULT_WITHDRAWAL_FREQUENCY;

    public static void validateAndUpdateInvestmentDataDTO(InvestmentDataDTO reqDTO) {
        if (Objects.isNull(reqDTO.getPurchaseAmount())) {
            reqDTO.setPurchaseAmount(InvestmentDataDTO.DEFAULT_PURCHASE_AMOUNT);
        }
        if (Objects.isNull(reqDTO.getPac())) {
            reqDTO.setPac(InvestmentDataDTO.DEFAULT_PAC);
        }
        if (Objects.isNull(reqDTO.getPacFrequency())) {
            reqDTO.setPacFrequency(InvestmentDataDTO.DEFAULT_PAC_FREQUENCY);
        }
        if (Objects.isNull(reqDTO.getWithdrawal())) {
            reqDTO.setWithdrawal(InvestmentDataDTO.DEFAULT_WITHDRAWAL);
        }
        if (Objects.isNull(reqDTO.getWithdrawalFrequency())) {
            reqDTO.setWithdrawalFrequency(InvestmentDataDTO.DEFAULT_WITHDRAWAL_FREQUENCY);
        }
    }
}
