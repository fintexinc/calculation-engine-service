package com.fintex.ce.application.service.calculation;

import com.fintex.ce.port.output.HoldingDataLoader;
import com.fintex.ce.application.calculation.SalesChargeCalculation;
import com.fintex.ce.port.input.command.PortfolioHoldingsCommand;
import com.fintex.ce.port.input.result.SalesChargeResult;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SalesChargeServiceImplTest {

  @Test
  void perform_verifyLoad() {
    // SETUP
    final var cacheStorage = mock(HoldingDataLoader.class);
    final var sut = mock(SalesChargeServiceImpl.class, withSettings().useConstructor(cacheStorage));
    final var reqDTO = mock(PortfolioHoldingsCommand.class);

    final var salesCharge = mock(Map.class);
    when(cacheStorage.load(anyList(), anyList(), anyList(), any())).thenReturn(salesCharge);
    when(sut.getSalesChargeCalculation(salesCharge)).thenReturn(mock(SalesChargeCalculation.class));

    doCallRealMethod().when(sut).perform(any());
    // ACT
    sut.perform(reqDTO);

    // VERIFY
    verify(cacheStorage).load(anyList(), anyList(), anyList(), any());
  }

  @Test
  void perform_verifyGetSalesChargeCalculation() {
    // SETUP
    final var cacheStorage = mock(HoldingDataLoader.class);
    final var sut = mock(SalesChargeServiceImpl.class, withSettings().useConstructor(cacheStorage));
    final var reqDTO = mock(PortfolioHoldingsCommand.class);

    final var salesCharge = mock(Map.class);
    when(cacheStorage.load(anyList(), anyList(), anyList(), any())).thenReturn(salesCharge);
    when(sut.getSalesChargeCalculation(salesCharge)).thenReturn(mock(SalesChargeCalculation.class));

    doCallRealMethod().when(sut).perform(any());
    // ACT
    sut.perform(reqDTO);

    // VERIFY
    verify(sut).getSalesChargeCalculation(salesCharge);
  }

  @Test
  void perform_checkResult() {
    // SETUP
    final var cacheStorage = mock(HoldingDataLoader.class);
    final var sut = mock(SalesChargeServiceImpl.class, withSettings().useConstructor(cacheStorage));
    final var reqDTO = mock(PortfolioHoldingsCommand.class);
    final SalesChargeCalculation calculation = mock(SalesChargeCalculation.class);
    final SalesChargeResult expected = mock(SalesChargeResult.class);

    final var salesCharge = mock(Map.class);
    when(cacheStorage.load(anyList(), anyList(), anyList(), any())).thenReturn(salesCharge);
    when(sut.getSalesChargeCalculation(salesCharge)).thenReturn(calculation);
    when(calculation.calculate()).thenReturn(expected);

    doCallRealMethod().when(sut).perform(any());
    // ACT
    final SalesChargeResult actual = sut.perform(reqDTO);

    // VERIFY
    assertSame(expected, actual);
  }

}
