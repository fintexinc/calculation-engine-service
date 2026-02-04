package com.fintex.ce.adapter.rest.validation.chainofresponsibility;

import com.fintex.ce.domain.enumeration.ExceptionCode;
import lombok.EqualsAndHashCode;

import static java.util.Objects.nonNull;

@EqualsAndHashCode(callSuper = true)
public class CustomNumberOfBinsGreaterThan30DistributionOfReturnsReqValidation extends ReqValidation {

  private final Integer customNumberOfBins;

  public CustomNumberOfBinsGreaterThan30DistributionOfReturnsReqValidation(final Integer customNumberOfBins) {
    this.customNumberOfBins = customNumberOfBins;
  }

  @Override
  protected void check() {
    if (nonNull(customNumberOfBins) && customNumberOfBins > 30) {
      throw ExceptionCode.ERR_RRC_CNOB_002.reqValidationError();
    }
  }
}
