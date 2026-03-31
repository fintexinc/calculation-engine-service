package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.calculation.metric.SalesChargeCalculation;
import com.fintex.ce.domain.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.domain.model.result.SalesChargeResult;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import java.util.Map;
import org.junit.jupiter.api.Test;

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
    final var sut = mock(SalesChargeServiceImpl.class, withSettings().useConstructor(fetcher));
    final var reqDTO = mock(PortfolioHoldingsCommand.class);

    final var salesCharge = mock(Map.class);
    when(fetcher.fetch(any(), any())).thenReturn(salesCharge);
    when(sut.getSalesChargeCalculation(salesCharge)).thenReturn(mock(SalesChargeCalculation.class));

    doCallRealMethod().when(sut).perform(any());
    // ACT
    sut.perform(reqDTO);

    // VERIFY
    verify(fetcher).fetch(any(), any());
  }

  @Test
  void shouldPerform_whenVerifyGetSalesChargeCalculation() {
    // SETUP
    final var fetcher = mock(SecurityDataFetcher.class);
    final var sut = mock(SalesChargeServiceImpl.class, withSettings().useConstructor(fetcher));
    final var reqDTO = mock(PortfolioHoldingsCommand.class);

    final var salesCharge = mock(Map.class);
    when(fetcher.fetch(any(), any())).thenReturn(salesCharge);
    when(sut.getSalesChargeCalculation(salesCharge)).thenReturn(mock(SalesChargeCalculation.class));

    doCallRealMethod().when(sut).perform(any());
    // ACT
    sut.perform(reqDTO);

    // VERIFY
    verify(sut).getSalesChargeCalculation(salesCharge);
  }

  @Test
  void shouldPerform_whenCheckResult() {
    // SETUP
    final var fetcher = mock(SecurityDataFetcher.class);
    final var sut = mock(SalesChargeServiceImpl.class, withSettings().useConstructor(fetcher));
    final var reqDTO = mock(PortfolioHoldingsCommand.class);
    final SalesChargeCalculation calculation = mock(SalesChargeCalculation.class);
    final SalesChargeResult expected = mock(SalesChargeResult.class);

    final var salesCharge = mock(Map.class);
    when(fetcher.fetch(any(), any())).thenReturn(salesCharge);
    when(sut.getSalesChargeCalculation(salesCharge)).thenReturn(calculation);
    when(calculation.calculate()).thenReturn(expected);

    doCallRealMethod().when(sut).perform(any());
    // ACT
    final SalesChargeResult actual = sut.perform(reqDTO);

    // VERIFY
    assertSame(expected, actual);
  }

}
