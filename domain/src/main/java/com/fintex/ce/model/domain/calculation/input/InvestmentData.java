package com.fintex.ce.model.domain.calculation.input;

import com.fintex.ce.model.domain.enumeration.Frequency;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.fintex.ce.model.util.BigDecimalConstants.TEN_THOUSAND;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvestmentData {

  public static BigDecimal DEFAULT_PURCHASE_AMOUNT = TEN_THOUSAND;
  public static Frequency DEFAULT_PAC_FREQUENCY = Frequency.ANNUAL;
  public static Frequency DEFAULT_WITHDRAWAL_FREQUENCY = Frequency.ANNUAL;
  public static BigDecimal DEFAULT_PAC = BigDecimal.ZERO;
  public static BigDecimal DEFAULT_WITHDRAWAL = BigDecimal.ZERO;

  @JsonProperty("purchaseAmount")
  private BigDecimal purchaseAmount = DEFAULT_PURCHASE_AMOUNT;

  /*
   * pac is pre-authorized contribution. Simple put it is contribution.
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

  public static void validateAndUpdateInvestmentData(InvestmentData investmentData) {
    if (Objects.isNull(investmentData.getPurchaseAmount())) {
      investmentData.setPurchaseAmount(InvestmentData.DEFAULT_PURCHASE_AMOUNT);
    }
    if (Objects.isNull(investmentData.getPac())) {
      investmentData.setPac(InvestmentData.DEFAULT_PAC);
    }
    if (Objects.isNull(investmentData.getPacFrequency())) {
      investmentData.setPacFrequency(InvestmentData.DEFAULT_PAC_FREQUENCY);
    }
    if (Objects.isNull(investmentData.getWithdrawal())) {
      investmentData.setWithdrawal(InvestmentData.DEFAULT_WITHDRAWAL);
    }
    if (Objects.isNull(investmentData.getWithdrawalFrequency())) {
      investmentData.setWithdrawalFrequency(InvestmentData.DEFAULT_WITHDRAWAL_FREQUENCY);
    }
  }
}
