package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.calculation.metric.SalesChargeCalculation;
import com.fintex.ce.model.domain.result.fee.SalesChargeResult;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class SalesChargeTypeServiceImplTest {

  @Test
  void shouldPerform_whenVerifyLoad() {
    // SETUP
    final var fetcher = mock(SecurityDataFetcher.class);
    final var service = mock(SalesChargeServiceImpl.class, withSettings().useConstructor(fetcher));
    final var command = mock(PortfolioHoldingsCommand.class);

    final var salesCharge = mock(Map.class);
    when(fetcher.fetch(any(), any())).thenReturn(salesCharge);
    when(service.getSalesChargeCalculation(salesCharge)).thenReturn(mock(SalesChargeCalculation.class));

    doCallRealMethod().when(service).perform(any());
    // ACT
    service.perform(command);

    // VERIFY
    verify(fetcher).fetch(any(), any());
  }

  @Test
  void shouldPerform_whenVerifyGetSalesChargeCalculation() {
    // SETUP
    final var fetcher = mock(SecurityDataFetcher.class);
    final var service = mock(SalesChargeServiceImpl.class, withSettings().useConstructor(fetcher));
    final var command = mock(PortfolioHoldingsCommand.class);

    final var salesCharge = mock(Map.class);
    when(fetcher.fetch(any(), any())).thenReturn(salesCharge);
    when(service.getSalesChargeCalculation(salesCharge)).thenReturn(mock(SalesChargeCalculation.class));

    doCallRealMethod().when(service).perform(any());
    // ACT
    service.perform(command);

    // VERIFY
    verify(service).getSalesChargeCalculation(salesCharge);
  }

  @Test
  void shouldPerform_whenCheckResult() {
    // SETUP
    final var fetcher = mock(SecurityDataFetcher.class);
    final var service = mock(SalesChargeServiceImpl.class, withSettings().useConstructor(fetcher));
    final var command = mock(PortfolioHoldingsCommand.class);
    final SalesChargeCalculation calculation = mock(SalesChargeCalculation.class);
    final SalesChargeResult expected = mock(SalesChargeResult.class);

    final var salesCharge = mock(Map.class);
    when(fetcher.fetch(any(), any())).thenReturn(salesCharge);
    when(service.getSalesChargeCalculation(salesCharge)).thenReturn(calculation);
    when(calculation.calculate()).thenReturn(expected);

    doCallRealMethod().when(service).perform(any());
    // ACT
    final SalesChargeResult actual = service.perform(command);

    // VERIFY
    assertSame(expected, actual);
  }

}
