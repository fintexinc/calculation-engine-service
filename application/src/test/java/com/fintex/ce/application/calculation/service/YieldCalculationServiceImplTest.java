package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.mapping.response.YieldResponseMapper;
import com.fintex.ce.domain.dto.command.YieldCommand;
import com.fintex.ce.domain.model.Yield;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.result.YieldResult;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class YieldCalculationServiceImplTest {

  @Mock
  private SecurityDataFetcher yieldFetcher;
  @Mock
  private YieldResponseMapper responseMapper;

  private YieldCalculationServiceImpl service;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    service = new YieldCalculationServiceImpl(yieldFetcher, responseMapper);
  }

  static Map<Holding, Yield> createMockData() {
    Map<Holding, Yield> mockData = new HashMap<>();
    Yield yield1 = new Yield();
    yield1.setDividendYield(new BigDecimal("0.05"));
    mockData.put(new Holding().setValue(new BigDecimal("100")).setHoldingType(FinancialInstrumentType.MUTUAL_FUND_CANADA), yield1);

    Yield yield2 = new Yield();
    yield2.setDividendYield(new BigDecimal("0.10"));
    mockData.put(new Holding().setValue(new BigDecimal("200")).setHoldingType(FinancialInstrumentType.HEDGE_FUND_CANADA), yield2);

    Yield yield3 = new Yield();
    yield3.setDividendYield(new BigDecimal("0.06"));
    mockData.put(new Holding().setValue(new BigDecimal("150")).setHoldingType(FinancialInstrumentType.GIC), yield3);
    return mockData;
  }

  @Test
  void shouldTestPerform_whenConditionIsMet() {
    // SETUP
    YieldCommand reqDTO = mock(YieldCommand.class);
    Map<Holding, Yield> mockData = createMockData();
    YieldResult expectedResponse = new YieldResult();

    when(yieldFetcher.fetch(any(), any())).thenReturn(mockData);
    when(responseMapper.toResponse(any(Map.class), any())).thenReturn(expectedResponse);

    // ACT
    YieldResult result = service.perform(reqDTO);

    // VERIFY
    verify(yieldFetcher).fetch(any(), any());
    verify(responseMapper).toResponse(any(Map.class), any());
    assertNotNull(result);
    assertEquals(expectedResponse, result);
  }

  @Test
  void shouldTestPerform_whenVerifyFetcherLoad() {
    // SETUP
    YieldCommand reqDTO = mock(YieldCommand.class);
    when(yieldFetcher.fetch(any(), any())).thenReturn(new HashMap<>());
    when(responseMapper.toResponse(any(Map.class), any())).thenReturn(new YieldResult());

    // ACT
    service.perform(reqDTO);

    // VERIFY
    verify(yieldFetcher).fetch(any(), any());
  }

  @Test
  void shouldTestPerform_whenVerifyResponseMapperCalled() {
    // SETUP
    YieldCommand reqDTO = mock(YieldCommand.class);
    Map<Holding, Yield> mockData = createMockData();
    when(yieldFetcher.fetch(any(), any())).thenReturn(mockData);
    when(responseMapper.toResponse(any(Map.class), any())).thenReturn(new YieldResult());

    // ACT
    service.perform(reqDTO);

    // VERIFY
    verify(responseMapper).toResponse(any(Map.class), any());
  }

}