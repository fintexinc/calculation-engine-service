package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.mapping.response.YieldResponseMapper;
import com.fintex.ce.model.domain.calculation.yield.Yield;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.income.YieldResult;
import com.fintex.ce.model.dto.command.YieldCommand;
import com.fintex.wm.commons.domain.enumeration.CompositeSecurityAttribute;
import com.fintex.wm.commons.domain.enumeration.Country;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Disabled("metric unsupported")
class YieldCalculationServiceImplTest {

  @Mock
  private YieldResponseMapper responseMapper;

  private YieldCalculationServiceImpl service;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    service = new YieldCalculationServiceImpl(responseMapper);
  }

  static Map<PortfolioHolding, Yield> createMockData() {
    Map<PortfolioHolding, Yield> mockData = new HashMap<>();
    Yield yield1 = new Yield();
    yield1.setDividendYield(new BigDecimal("0.05"));
    mockData.put(new PortfolioHolding(new BigDecimal("100"), FinancialInstrumentType.MUTUAL_FUND, Country.CANADA, null),
        yield1);

    Yield yield2 = new Yield();
    yield2.setDividendYield(new BigDecimal("0.10"));
    mockData.put(new PortfolioHolding(new BigDecimal("200"), FinancialInstrumentType.HEDGE_FUND, Country.CANADA, null),
        yield2);

    Yield yield3 = new Yield();
    yield3.setDividendYield(new BigDecimal("0.06"));
    mockData.put(new PortfolioHolding(new BigDecimal("150"), FinancialInstrumentType.GIC, null), yield3);
    return mockData;
  }

  @Test
  void shouldTestPerform_whenConditionIsMet() {
    // SETUP
    YieldCommand command = mock(YieldCommand.class);
    Map<PortfolioHolding, Yield> mockData = createMockData();
    YieldResult expectedResponse = new YieldResult();

    when(command.getHoldings()).thenReturn(List.copyOf(mockData.keySet()));
    when(responseMapper.toResponse(any(Map.class), any())).thenReturn(expectedResponse);

    // ACT
    YieldResult result = service.perform(command, mockData);

    // VERIFY
    verify(responseMapper).toResponse(any(Map.class), any());
    assertNotNull(result);
    assertEquals(expectedResponse, result);
  }

  @Test
  void shouldRequireIncomeAttribute_whenRequiredAttributesInvoked() {
    assertEquals(List.of(CompositeSecurityAttribute.INCOME), service.requiredAttributes());
    assertEquals(CompositeSecurityAttribute.INCOME, service.requiredAttribute());
  }

  @Test
  void shouldTestPerform_whenVerifyResponseMapperCalled() {
    // SETUP
    YieldCommand command = mock(YieldCommand.class);
    Map<PortfolioHolding, Yield> mockData = createMockData();
    when(command.getHoldings()).thenReturn(List.copyOf(mockData.keySet()));
    when(responseMapper.toResponse(any(Map.class), any())).thenReturn(new YieldResult());

    // ACT
    service.perform(command, mockData);

    // VERIFY
    verify(responseMapper).toResponse(eq(mockData), any());
  }

}
