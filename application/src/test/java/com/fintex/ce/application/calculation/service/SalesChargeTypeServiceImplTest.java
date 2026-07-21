package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.calculation.metric.SalesChargeCalculation;
import com.fintex.ce.model.domain.calculation.fee.SalesCharge;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.fee.SalesChargeResult;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SalesChargeTypeServiceImplTest {

  @Test
  void shouldPerform_whenVerifyGetSalesChargeCalculation() {
    // SETUP
    var service = mock(SalesChargeServiceImpl.class);
    PortfolioHolding holding = mock(PortfolioHolding.class);
    Map<PortfolioHolding, SalesCharge> salesCharge = Map.of(holding, mock(SalesCharge.class));
    var command = PortfolioHoldingsCommand.builder().holdings(List.of(holding)).build();

    when(service.getSalesChargeCalculation(salesCharge)).thenReturn(mock(SalesChargeCalculation.class));

    doCallRealMethod().when(service).perform(any(), any());
    // ACT
    service.perform(command, salesCharge);

    // VERIFY
    verify(service).getSalesChargeCalculation(salesCharge);
  }

  @Test
  void shouldPerform_whenCheckResult() {
    // SETUP
    var service = mock(SalesChargeServiceImpl.class);
    PortfolioHolding holding = mock(PortfolioHolding.class);
    Map<PortfolioHolding, SalesCharge> salesCharge = Map.of(holding, mock(SalesCharge.class));
    var command = PortfolioHoldingsCommand.builder().holdings(List.of(holding)).build();
    SalesChargeCalculation calculation = mock(SalesChargeCalculation.class);
    SalesChargeResult expected = mock(SalesChargeResult.class);

    when(service.getSalesChargeCalculation(salesCharge)).thenReturn(calculation);
    when(calculation.calculate()).thenReturn(expected);

    doCallRealMethod().when(service).perform(any(), any());
    // ACT
    SalesChargeResult actual = service.perform(command, salesCharge);

    // VERIFY
    assertSame(expected, actual);
  }

}
