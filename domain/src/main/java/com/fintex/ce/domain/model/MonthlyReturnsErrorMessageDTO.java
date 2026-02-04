package com.fintex.ce.domain.model;

import com.fintex.ce.domain.enumeration.ExceptionCode;
import lombok.Data;

import static com.fintex.ce.domain.enumeration.ExceptionCode.ERR_RRC_CPED_002;
import static com.fintex.ce.domain.enumeration.ExceptionCode.ERR_RRC_CPED_003;

@Data
public class MonthlyReturnsErrorMessageDTO {

  // CPED before PED
  private ExceptionCode cpedBeforePedCode;
  // CPED after PED
  private ExceptionCode cpedAfterPsdCode;

  private boolean errorWhenCpedAfterPped;

  public MonthlyReturnsErrorMessageDTO() {
    this.cpedBeforePedCode = ERR_RRC_CPED_003;
    this.cpedAfterPsdCode = ERR_RRC_CPED_002;
    this.errorWhenCpedAfterPped = true;
  }

  public MonthlyReturnsErrorMessageDTO(final ExceptionCode cpedBeforePedCode, final ExceptionCode cpedAfterPsdCode) {
    this.cpedBeforePedCode = cpedBeforePedCode;
    this.cpedAfterPsdCode = cpedAfterPsdCode;
    this.errorWhenCpedAfterPped = true;
  }

  public MonthlyReturnsErrorMessageDTO(final boolean errorWhenCpedAfterPped) {

    this.cpedBeforePedCode = ERR_RRC_CPED_003;
    this.cpedAfterPsdCode = ERR_RRC_CPED_002;
    this.errorWhenCpedAfterPped = errorWhenCpedAfterPped;
  }

}
