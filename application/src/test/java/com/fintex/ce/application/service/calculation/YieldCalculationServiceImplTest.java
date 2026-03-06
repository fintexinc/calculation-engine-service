package com.fintex.ce.application.service.calculation;

import com.fintex.ce.port.output.cache.HoldingDataLoader;
import com.fintex.ce.application.mapper.response.YieldResponseMapper;
import com.fintex.ce.application.service.calculation.YieldCalculationServiceImpl;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.model.Yield;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.port.input.command.YieldCommand;
import com.fintex.ce.port.input.result.YieldResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class YieldCalculationServiceImplTest {

  @Mock
  private HoldingDataLoader yieldCacheStorage;
  @Mock
  private YieldResponseMapper responseMapper;

  private YieldCalculationServiceImpl service;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    service = new YieldCalculationServiceImpl(yieldCacheStorage, responseMapper);
  }

  static Map<Holding, Yield> createMockData() {
    Map<Holding, Yield> mockData = new HashMap<>();
    Yield yield1 = new Yield();
    yield1.setDividendYield(new BigDecimal("0.05"));
    mockData.put(new Holding().setValue(new BigDecimal("100")).setType(HoldingType.CANADA_MUTUAL_FUNDS), yield1);

    Yield yield2 = new Yield();
    yield2.setDividendYield(new BigDecimal("0.10"));
    mockData.put(new Holding().setValue(new BigDecimal("200")).setType(HoldingType.CANADA_HEDGE_FUNDS), yield2);

    Yield yield3 = new Yield();
    yield3.setDividendYield(new BigDecimal("0.06"));
    mockData.put(new Holding().setValue(new BigDecimal("150")).setType(HoldingType.GIC), yield3);
    return mockData;
  }

  @Test
  void testPerform() {
    // SETUP
    YieldCommand reqDTO = mock(YieldCommand.class);
    Map<Holding, Yield> mockData = createMockData();
    YieldResult expectedResponse = new YieldResult();

    when(yieldCacheStorage.load(any(), any(), any(), any())).thenReturn(mockData);
    when(responseMapper.toResponse(any(Map.class), any())).thenReturn(expectedResponse);

    // ACT
    YieldResult result = service.perform(reqDTO);

    // VERIFY
    verify(yieldCacheStorage).load(any(), any(), any(), any());
    verify(responseMapper).toResponse(any(Map.class), any());
    assertNotNull(result);
    assertEquals(expectedResponse, result);
  }

  @Test
  void testPerform_verifyCacheStorageLoad() {
    // SETUP
    YieldCommand reqDTO = mock(YieldCommand.class);
    when(yieldCacheStorage.load(any(), any(), any(), any())).thenReturn(new HashMap<>());
    when(responseMapper.toResponse(any(Map.class), any())).thenReturn(new YieldResult());

    // ACT
    service.perform(reqDTO);

    // VERIFY
    verify(yieldCacheStorage).load(any(), any(), any(), any());
  }

  @Test
  void testPerform_verifyResponseMapperCalled() {
    // SETUP
    YieldCommand reqDTO = mock(YieldCommand.class);
    Map<Holding, Yield> mockData = createMockData();
    when(yieldCacheStorage.load(any(), any(), any(), any())).thenReturn(mockData);
    when(responseMapper.toResponse(any(Map.class), any())).thenReturn(new YieldResult());

    // ACT
    service.perform(reqDTO);

    // VERIFY
    verify(responseMapper).toResponse(any(Map.class), any());
  }

}