package com.fintex.ce.adapter.rest.validation.chainofresponsibility;

import com.fintex.ce.domain.model.enumeration.ExceptionCode;
import lombok.EqualsAndHashCode;

import static java.util.Objects.nonNull;

@EqualsAndHashCode(callSuper = true)
public class CustomNumberOfBinsLessThan5DistributionOfReturnsReqValidation extends ReqValidation {

  private final Integer customNumberOfBins;

  public CustomNumberOfBinsLessThan5DistributionOfReturnsReqValidation(final Integer customNumberOfBins) {
    this.customNumberOfBins = customNumberOfBins;
  }

  @Override
  protected void check() {
    if (nonNull(customNumberOfBins) && customNumberOfBins < 5) {
      throw ExceptionCode.ERR_RRC_CNOB_001.reqValidationError();
    }
  }
}
