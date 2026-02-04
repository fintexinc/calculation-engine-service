package com.fintex.ce.adapter.rest.validation;

import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.adapter.rest.dto.request.DistributionOfReturnsReqDTO;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.CipsdGreaterThanCpedReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.CipsdLastDayOfMonthReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.CpedLastDayOfMonthReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.CpsdGreaterThanCpedReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.CpsdLastDayOfMonthReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.CustomNumberOfBinsGreaterThan30DistributionOfReturnsReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.CustomNumberOfBinsLessThan5DistributionOfReturnsReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.HoldingReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.HoldingValueReqValidator;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.HoldingsCouldNotBeEmptyReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.NotEmptyCurrencyReqValidator;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.NotNullReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.PeriodReqValidation;
import com.fintex.ce.adapter.rest.validation.chainofresponsibility.ReqValidation;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class DistributionOfReturnsReqValidatorTest {

  @Test
  void build_checkResult() {
    // SETUP
    final var sut = new DistributionOfReturnsReqValidator();

    final DistributionOfReturnsReqDTO reqDTO = getDistributionOfReturnsReqDTO();

    final ReqValidation expected = ReqValidation.create()
        .linkWith(new NotNullReqValidation(reqDTO))
        .linkWith(new NotEmptyCurrencyReqValidator(reqDTO.getCurrency()))
        .linkWith(new CpedLastDayOfMonthReqValidation(reqDTO.getCustomPed()))
        .linkWith(new CpsdLastDayOfMonthReqValidation(reqDTO.getCustomPsd()))
        .linkWith(new CipsdLastDayOfMonthReqValidation(reqDTO.getCustomIntervalPsd()))
        .linkWith(new CpsdGreaterThanCpedReqValidation(reqDTO.getCustomPsd(), reqDTO.getCustomPed()))
        .linkWith(new CipsdGreaterThanCpedReqValidation(reqDTO.getCustomIntervalPsd(), reqDTO.getCustomPed()))
        .linkWith(new CustomNumberOfBinsLessThan5DistributionOfReturnsReqValidation(reqDTO.getCustomNumberOfBins()))
        .linkWith(new CustomNumberOfBinsGreaterThan30DistributionOfReturnsReqValidation(reqDTO.getCustomNumberOfBins()))
        .linkWith(new PeriodReqValidation(reqDTO.getPeriods()))
        .linkWith(new HoldingsCouldNotBeEmptyReqValidation(reqDTO.getHoldings()))
        .linkWith(new HoldingReqValidation(reqDTO.getHoldings()))
        .linkWith(new HoldingValueReqValidator(reqDTO.getHoldings()));

    // ACT
    final ReqValidation actual = sut.build(reqDTO);

    // VERIFY
    assertEquals(expected, actual);
  }

  DistributionOfReturnsReqDTO getDistributionOfReturnsReqDTO() {
    final var reqDTO = new DistributionOfReturnsReqDTO();
    reqDTO.setCustomPed(LocalDate.of(2020, 5, 31));
    reqDTO.setCustomPsd(LocalDate.of(2000, 4, 30));
    reqDTO.setCustomIntervalPsd(LocalDate.of(2005, 7, 31));
    reqDTO.setCustomNumberOfBins(4);
    reqDTO.setPeriods(Set.of("1", "2", "3"));
    reqDTO.setHoldings(List.of(mock(Holding.class)));
    return reqDTO;
  }

}