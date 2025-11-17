package com.fintex.ce.service.impl.calculation;

import com.fintex.ce.domain.calculation.DailyPerformanceCalculation;
import com.fintex.ce.dto.calculation.HoldingForDailyCalculationDTO;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.request.DailyPerformanceReqDTO;
import com.fintex.ce.dto.response.DistributionResDTO;
import com.fintex.ce.model.redis.RHistoricalDistributions;
import com.fintex.ce.model.redis.RHistoricalNavPrices;
import com.fintex.ce.service.impl.cache.HistoricalDistributionsCacheStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

class DistributionServiceImplTest {

    @Test
    void perform_verify() {
        final DailyPerformanceCalculationServiceImpl dailyPerformanceCalculationService = Mockito.mock(DailyPerformanceCalculationServiceImpl.class);
        final HistoricalDistributionsCacheStorage historicalDistributionsCacheStorage = Mockito.mock(HistoricalDistributionsCacheStorage.class);
        final var sut = mock(DistributionServiceImpl.class, withSettings().useConstructor(dailyPerformanceCalculationService, historicalDistributionsCacheStorage));

        final var dailyPerformanceReqDTO = Mockito.mock(DailyPerformanceReqDTO.class);
        final var holdingForDailyCalculationDTO = Mockito.mock(HoldingForDailyCalculationDTO.class);
        final var holding = Mockito.mock(Holding.class);
        final var rHistoricalNavPrices = Mockito.mock(RHistoricalNavPrices.class);
        final var rHistoricalDistributions = Mockito.mock(RHistoricalDistributions.class);
        final var dailyPerformanceCalculation = Mockito.mock(DailyPerformanceCalculation.class);
        final var distributionMap = Mockito.mock(Map.class);

        Mockito.when(dailyPerformanceReqDTO.getDailyHoldings())
                .thenReturn(List.of(holdingForDailyCalculationDTO));
        Mockito.when(dailyPerformanceCalculationService.validateRequestAndGetHoldings(dailyPerformanceReqDTO))
                .thenReturn(List.of(holding));
        Mockito.when(dailyPerformanceCalculationService.validateRequestAndGetNAVData(Mockito.anyList(), Mockito.anyList()))
                .thenReturn(Map.of(holding, rHistoricalNavPrices));
        Mockito.when(historicalDistributionsCacheStorage.load(Mockito.anyList(), Mockito.anyList(), Mockito.anyList(), Mockito.any()))
                .thenReturn(Map.of(holding, rHistoricalDistributions));
        Mockito.when(dailyPerformanceCalculationService.getDailyPerformanceCalculation(dailyPerformanceReqDTO, Map.of(holding, rHistoricalNavPrices), Map.of(holding, rHistoricalDistributions)))
                .thenReturn(dailyPerformanceCalculation);
        doCallRealMethod().when(sut).perform(dailyPerformanceReqDTO);
        Mockito.when(dailyPerformanceCalculation.calculateDistribution(Mockito.anyBoolean(), Mockito.anyBoolean()))
                .thenReturn(distributionMap);


        //ACT
        final DistributionResDTO result = sut.perform(dailyPerformanceReqDTO);

        //VERIFY
        Assertions.assertNotNull(result);
        Assertions.assertNotNull(result.getDistribution());

    }

}
