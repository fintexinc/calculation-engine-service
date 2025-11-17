package com.fintex.ce.domain.calculation;

import com.fintex.ce.dto.calculation.DistributionData;
import com.fintex.ce.dto.calculation.HoldingForDailyCalculationDTO;
import com.fintex.ce.dto.calculation.ReturnsAnsDistributionReceived;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.model.redis.RHistoricalDistributions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class DailyPerformanceCalculationTest {


    private DailyPerformanceCalculation dailyPerformanceCalculation;
    private Map<Holding, TreeMap<LocalDate, BigDecimal>> holdingNavPrices;
    private Map<Holding, RHistoricalDistributions> distributionsData;
    private Map<Holding, HoldingForDailyCalculationDTO> holdingAndDailyHolding;

    @BeforeEach
    void setUp() {
        holdingNavPrices = new HashMap<>();
        distributionsData = new HashMap<>();
        holdingAndDailyHolding = new HashMap<>();

        Holding holding = Mockito.mock(Holding.class);
        when(holding.generateUserIdentifier()).thenReturn("testHolding");

        TreeMap<LocalDate, BigDecimal> navPrices = new TreeMap<>();
        navPrices.put(LocalDate.now(), BigDecimal.valueOf(100));
        holdingNavPrices.put(holding, navPrices);

        RHistoricalDistributions rHistoricalDistributions = Mockito.mock(RHistoricalDistributions.class);
        distributionsData.put(holding, rHistoricalDistributions);

        HoldingForDailyCalculationDTO holdingForDailyCalculationDTO = Mockito.mock(HoldingForDailyCalculationDTO.class);
        when(holdingForDailyCalculationDTO.getPurchaseAmount()).thenReturn(BigDecimal.valueOf(1000));
        holdingAndDailyHolding.put(holding, holdingForDailyCalculationDTO);

        dailyPerformanceCalculation = new DailyPerformanceCalculation(holdingNavPrices, distributionsData, holdingAndDailyHolding);
    }

    @Test
    void calculate_withReinvestAndUsePacAndWithdrawal_returnsExpectedResults() {
        Map<String, ReturnsAnsDistributionReceived> result = dailyPerformanceCalculation.calculate(true, true);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("testHolding"));
    }

    @Test
    void calculate_withoutReinvestAndUsePacAndWithdrawal_returnsExpectedResults() {
        Map<String, ReturnsAnsDistributionReceived> result = dailyPerformanceCalculation.calculate(false, false);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("testHolding"));
    }

    @Test
    void calculateForGrowthOf10K_returnsExpectedResults() {
        // Arrange
        Holding holding = Mockito.mock(Holding.class);
        when(holding.generateUserIdentifier()).thenReturn("testHolding");

        TreeMap<LocalDate, BigDecimal> navPrices = new TreeMap<>();
        navPrices.put(LocalDate.now(), BigDecimal.valueOf(100));
        holdingNavPrices.put(holding, navPrices);

        RHistoricalDistributions rHistoricalDistributions = Mockito.mock(RHistoricalDistributions.class);
        distributionsData.put(holding, rHistoricalDistributions);

        HoldingForDailyCalculationDTO holdingForDailyCalculationDTO = Mockito.mock(HoldingForDailyCalculationDTO.class);
        when(holdingForDailyCalculationDTO.getPurchaseAmount()).thenReturn(BigDecimal.valueOf(1000));
        holdingAndDailyHolding.put(holding, holdingForDailyCalculationDTO);

        // Act
        Map<Holding, TreeMap<LocalDate, BigDecimal>> result = dailyPerformanceCalculation.calculateForGrowthOf10K();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey(holding));
    }

    @Test
    void calculateForGrowthOf10K_returnsEmptyMap_whenNoHoldings() {
        // Arrange
        holdingNavPrices.clear();
        distributionsData.clear();
        holdingAndDailyHolding.clear();

        // Act
        Map<Holding, TreeMap<LocalDate, BigDecimal>> result = dailyPerformanceCalculation.calculateForGrowthOf10K();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void calculateDistribution_returnsExpectedResults_whenReinvestAndUsePacAndWithdrawalAreTrue() {
        Holding holding = Mockito.mock(Holding.class);
        when(holding.generateUserIdentifier()).thenReturn("testHolding");

        TreeMap<LocalDate, BigDecimal> navPrices = new TreeMap<>();
        navPrices.put(LocalDate.now(), BigDecimal.valueOf(100));
        holdingNavPrices.put(holding, navPrices);

        RHistoricalDistributions rHistoricalDistributions = Mockito.mock(RHistoricalDistributions.class);
        distributionsData.put(holding, rHistoricalDistributions);

        HoldingForDailyCalculationDTO holdingForDailyCalculationDTO = Mockito.mock(HoldingForDailyCalculationDTO.class);
        when(holdingForDailyCalculationDTO.getPurchaseAmount()).thenReturn(BigDecimal.valueOf(1000));
        holdingAndDailyHolding.put(holding, holdingForDailyCalculationDTO);

        Map<String, TreeMap<LocalDate, DistributionData>> result = dailyPerformanceCalculation.calculateDistribution(true, true);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("testHolding"));
    }

    @Test
    void calculateDistribution_returnsExpectedResults_whenReinvestAndUsePacAndWithdrawalAreFalse() {
        Holding holding = Mockito.mock(Holding.class);
        when(holding.generateUserIdentifier()).thenReturn("testHolding");

        TreeMap<LocalDate, BigDecimal> navPrices = new TreeMap<>();
        navPrices.put(LocalDate.now(), BigDecimal.valueOf(100));
        holdingNavPrices.put(holding, navPrices);

        RHistoricalDistributions rHistoricalDistributions = Mockito.mock(RHistoricalDistributions.class);
        distributionsData.put(holding, rHistoricalDistributions);

        HoldingForDailyCalculationDTO holdingForDailyCalculationDTO = Mockito.mock(HoldingForDailyCalculationDTO.class);
        when(holdingForDailyCalculationDTO.getPurchaseAmount()).thenReturn(BigDecimal.valueOf(1000));
        holdingAndDailyHolding.put(holding, holdingForDailyCalculationDTO);

        Map<String, TreeMap<LocalDate, DistributionData>> result = dailyPerformanceCalculation.calculateDistribution(false, false);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("testHolding"));
    }

    @Test
    void calculateDistribution_returnsEmptyMap_whenNoHoldings() {
        holdingNavPrices.clear();
        distributionsData.clear();
        holdingAndDailyHolding.clear();

        Map<String, TreeMap<LocalDate, DistributionData>> result = dailyPerformanceCalculation.calculateDistribution(false, false);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

}
