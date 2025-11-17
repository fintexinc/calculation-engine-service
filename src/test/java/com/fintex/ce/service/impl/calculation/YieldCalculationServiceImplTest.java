package com.fintex.ce.service.impl.calculation;

import com.fintex.ce.config.enumeration.HoldingType;
import com.fintex.ce.dto.YieldDTO;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.request.YieldReqDTO;
import com.fintex.ce.dto.response.YieldResDto;
import com.fintex.ce.model.redis.RYield;
import com.fintex.ce.service.impl.cache.YieldCacheStorage;
import com.fintex.ce.util.validation.request.PortfolioHoldingsReqDtoValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class YieldCalculationServiceImplTest {

    @Mock
    private YieldCacheStorage yieldCacheStorage;
    @Mock
    private PortfolioHoldingsReqDtoValidator requestValidator;
    @InjectMocks
    private YieldCalculationServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    static Map<Holding, RYield> createMockData() {
        Map<Holding, RYield> mockData = new HashMap<>();
        RYield rYield = new RYield();
        rYield.setDividendYield(new BigDecimal("0.05"));
        mockData.put(new Holding(new BigDecimal("100"), HoldingType.CANADA_MUTUAL_FUNDS), rYield);
        rYield.setDividendYield(new BigDecimal("0.10"));
        mockData.put(new Holding(new BigDecimal("200"), HoldingType.CANADA_HEDGE_FUNDS), rYield);
        rYield.setDividendYield(new BigDecimal("0.06"));
        mockData.put(new Holding(new BigDecimal("150"), HoldingType.GIC), rYield);
        return mockData;
    }

    @Test
    void testPerform() {
        YieldReqDTO reqDTO = mock(YieldReqDTO.class);
        Map<Holding, RYield> mockData = createMockData(); // Implement createMockData to generate test data
        when(yieldCacheStorage.load(any(), any(), any(), any())).thenReturn(mockData);
        YieldResDto result = service.perform(reqDTO);
        verify(requestValidator).validate(reqDTO);
        assertNotNull(result);
        // Further assertions based on expected behavior
    }

    @Test
    void testCalculate() {
        Map<Holding, RYield> mockData = createMockData();
        YieldResDto result = service.calculate(mockData);
        assertNotNull(result);
    }

    @Test
    void testGetYieldDto() {
        Holding holding = new Holding(new BigDecimal("150"), HoldingType.GIC);
        RYield rYield = new RYield();
        rYield.setDividendYield(new BigDecimal("6"));

        YieldDTO yieldDTO = service.getYieldDto(holding, rYield);

        assertEquals(new BigDecimal("0.06"), yieldDTO.getYield().stripTrailingZeros());
        assertEquals(new BigDecimal("150"), yieldDTO.getValue());
    }

    @Test
    void testCalculateWeightedAverageYield() {
        List<YieldDTO> yieldDTOList = List.of(
                new YieldDTO(new BigDecimal("0.05"), new BigDecimal("100")),
                new YieldDTO(new BigDecimal("0.10"), new BigDecimal("200"))
        );

        BigDecimal weightedAverage = service.calculateWeightedAverageYield(yieldDTOList);
        BigDecimal expectedWeightedAverage = new BigDecimal("0.083333333333333"); // Calculate the expected value manually
        assertEquals(0, expectedWeightedAverage.compareTo(weightedAverage.stripTrailingZeros()));
    }

}