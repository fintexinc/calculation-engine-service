package com.fintex.ce.service.impl.calculation;

import com.fintex.ce.domain.calculation.DailyPerformanceCalculation;
import com.fintex.ce.domain.monthlyreturns.Returns;
import com.fintex.ce.dto.calculation.HoldingForDailyCalculationDTO;
import com.fintex.ce.dto.calculation.ReturnsAnsDistributionReceived;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.request.DailyPerformanceReqDTO;
import com.fintex.ce.dto.response.DailyPerformanceResDTO;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.exception.DataErrorException;
import com.fintex.ce.model.redis.RHistoricalDistributions;
import com.fintex.ce.model.redis.RHistoricalNavPrices;
import com.fintex.ce.service.impl.cache.HistoricalDistributionsCacheStorage;
import com.fintex.ce.service.impl.cache.HistoryNavPricesCacheStorage;
import com.fintex.ce.util.validation.request.DailyPerformanceRequestValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.config.enumeration.ExceptionCode.ERR_NAV_PRICES_001;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class DailyPerformanceCalculationServiceImplTest {

    @Test
    void validateRequestAndGetHoldings() {
        //SETUP
        final var historicalDistributionsCacheStorage = mock(HistoricalDistributionsCacheStorage.class);
        final var historyNavPricesCacheStorage = mock(HistoryNavPricesCacheStorage.class);
        final var dailyPerformanceRequestValidator = mock(DailyPerformanceRequestValidator.class);
        final var sut = mock(DailyPerformanceCalculationServiceImpl.class, withSettings()
                .useConstructor(historyNavPricesCacheStorage, historicalDistributionsCacheStorage, dailyPerformanceRequestValidator));

        final DailyPerformanceReqDTO req = mock(DailyPerformanceReqDTO.class);
        final HoldingForDailyCalculationDTO dailyCalculationDTO = mock(HoldingForDailyCalculationDTO.class);
        final Holding holding = mock(Holding.class);

        when(req.getDailyHoldings()).thenReturn(List.of(dailyCalculationDTO));
        when(dailyCalculationDTO.getHolding()).thenReturn(holding);

        doCallRealMethod().when(sut).validateRequestAndGetHoldings(any());

        //ACT
        final List<Holding> result = sut.validateRequestAndGetHoldings(req);

        //VERIFY
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(holding, result.get(0));
    }

    @Test
    void validateRequestAndGetNAVData() {
        //SETUP
        final var historicalDistributionsCacheStorage = mock(HistoricalDistributionsCacheStorage.class);
        final var historyNavPricesCacheStorage = mock(HistoryNavPricesCacheStorage.class);
        final var dailyPerformanceRequestValidator = mock(DailyPerformanceRequestValidator.class);
        final var sut = mock(DailyPerformanceCalculationServiceImpl.class, withSettings()
                .useConstructor(historyNavPricesCacheStorage, historicalDistributionsCacheStorage, dailyPerformanceRequestValidator));

        final RHistoricalNavPrices rHistoricalNavPrices = mock(RHistoricalNavPrices.class);
        final Holding holding = mock(Holding.class);
        final DataErrorException errorException = mock(DataErrorException.class);
        final List<Warning> warnings = new ArrayList<>();

        Mockito.when(historyNavPricesCacheStorage.load(anyList(), anyList(), anyList(), any()))
                .thenReturn(Map.of(holding, rHistoricalNavPrices));
        Mockito.when(rHistoricalNavPrices.getErrors()).thenReturn(List.of(errorException));
        Mockito.when(errorException.getCode()).thenReturn(ERR_NAV_PRICES_001);

        doCallRealMethod().when(sut).validateRequestAndGetNAVData(anyList(), anyList());

        //ACT
        final Map<Holding, RHistoricalNavPrices> result = sut.validateRequestAndGetNAVData(warnings, List.of(holding));

        //VERIFY
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        final Map.Entry<Holding, RHistoricalNavPrices> entry = result.entrySet().stream().findFirst().orElseThrow();
        Assertions.assertEquals(holding, entry.getKey());
        Assertions.assertEquals(rHistoricalNavPrices, entry.getValue());
    }

    @Test
    void perform_verifyResult() {
        try (var util = Mockito.mockStatic(Returns.class)) {
            //SETUP
            final var historicalDistributionsCacheStorage = mock(HistoricalDistributionsCacheStorage.class);
            final var historyNavPricesCacheStorage = mock(HistoryNavPricesCacheStorage.class);
            final var dailyPerformanceRequestValidator = mock(DailyPerformanceRequestValidator.class);
            final var sut = mock(DailyPerformanceCalculationServiceImpl.class, withSettings()
                    .useConstructor(historyNavPricesCacheStorage, historicalDistributionsCacheStorage, dailyPerformanceRequestValidator));

            final DailyPerformanceReqDTO dailyPerformanceReqDTO = mock(DailyPerformanceReqDTO.class);
            final HoldingForDailyCalculationDTO holdingForDailyCalculationDTO = mock(HoldingForDailyCalculationDTO.class);
            final Holding holding = mock(Holding.class);
            final Returns returns = mock(Returns.class);
            final RHistoricalNavPrices rHistoricalNavPrices = mock(RHistoricalNavPrices.class);
            final RHistoricalDistributions rHistoricalDistributions = mock(RHistoricalDistributions.class);
            final DailyPerformanceCalculation calculation = mock(DailyPerformanceCalculation.class);
            final ReturnsAnsDistributionReceived returnsAnsDistributionReceived = mock(ReturnsAnsDistributionReceived.class);

            util.when(() -> Returns.initForNavPrices(Mockito.anyMap())).thenReturn(returns);

            Mockito.when(dailyPerformanceReqDTO.getDailyHoldings())
                    .thenReturn(List.of(holdingForDailyCalculationDTO));
            Mockito.when(sut.validateRequestAndGetHoldings(dailyPerformanceReqDTO))
                    .thenReturn(List.of(holding));
            Mockito.when(sut.validateRequestAndGetNAVData(Mockito.anyList(), Mockito.anyList()))
                    .thenReturn(Map.of(holding, rHistoricalNavPrices));
            Mockito.when(sut.getDailyPerformanceCalculation(Mockito.eq(dailyPerformanceReqDTO), Mockito.anyMap(), Mockito.anyMap()))
                    .thenReturn(calculation);
            Mockito.when(historicalDistributionsCacheStorage.load(Mockito.anyList(), Mockito.anyList(), Mockito.anyList(), Mockito.any()))
                    .thenReturn(Map.of(holding, rHistoricalDistributions));
            Mockito.when(calculation.calculate(true, true))
                    .thenReturn(new HashMap<>() {{
                        put("test1", returnsAnsDistributionReceived);
                    }});
            Mockito.when(calculation.calculate(false, true))
                    .thenReturn(new HashMap<>() {{
                        put("test2", returnsAnsDistributionReceived);
                    }});
            Mockito.when(calculation.calculate(true, false))
                    .thenReturn(new HashMap<>() {{
                        put("test3", returnsAnsDistributionReceived);
                    }});
            Mockito.when(calculation.calculate(false, false))
                    .thenReturn(new HashMap<>() {{
                        put("test4", returnsAnsDistributionReceived);
                    }});
            Mockito.when(returnsAnsDistributionReceived.getDistributionReceived()).thenReturn(BigDecimal.ONE);
            Mockito.when(returnsAnsDistributionReceived.getTotalContribution()).thenReturn(BigDecimal.ONE);
            Mockito.when(returnsAnsDistributionReceived.getTotalWithdrawal()).thenReturn(BigDecimal.ONE);
            Mockito.when(returnsAnsDistributionReceived.getSubsequentContribution()).thenReturn(BigDecimal.ONE);

            doCallRealMethod().when(sut).perform(dailyPerformanceReqDTO);

            //ACT
            final DailyPerformanceResDTO result = sut.perform(dailyPerformanceReqDTO);

            //VERIFY
            Assertions.assertNotNull(result);
        }
    }

    @Test
    void getDailyPerformanceCalculation() {
        try (var util = Mockito.mockStatic(Returns.class)) {
            //SETUP
            final var historicalDistributionsCacheStorage = mock(HistoricalDistributionsCacheStorage.class);
            final var historyNavPricesCacheStorage = mock(HistoryNavPricesCacheStorage.class);
            final var dailyPerformanceRequestValidator = mock(DailyPerformanceRequestValidator.class);
            final var sut = mock(DailyPerformanceCalculationServiceImpl.class, withSettings()
                    .useConstructor(historyNavPricesCacheStorage, historicalDistributionsCacheStorage, dailyPerformanceRequestValidator));

            final Returns returns = mock(Returns.class);
            final Map navData = mock(Map.class);
            final Map distributionsData = mock(Map.class);
            final DailyPerformanceReqDTO req = mock(DailyPerformanceReqDTO.class);
            final HoldingForDailyCalculationDTO dailyCalculationDTO = mock(HoldingForDailyCalculationDTO.class);
            final Holding holding = mock(Holding.class);
            final HoldingForDailyCalculationDTO holdingForDailyCalculationDTO = mock(HoldingForDailyCalculationDTO.class);

            util.when(() -> Returns.initForNavPrices(Mockito.anyMap())).thenReturn(returns);

            Mockito.when(req.getDailyHoldings())
                    .thenReturn(List.of(holdingForDailyCalculationDTO));
            when(req.getDailyHoldings()).thenReturn(List.of(dailyCalculationDTO));
            when(dailyCalculationDTO.getHolding()).thenReturn(holding);

            when(returns.validateAndUpdateCpsdAndCped(navData, req)).thenReturn(returns);
            when(returns.validateMonthlyDataMissing(navData, req)).thenReturn(returns);
            when(returns.ifAnyErrorsThrowException()).thenReturn(returns);
            when(returns.cutByCpsdIfCpsdEmptyCutByPsd(req.getStartDate())).thenReturn(returns);
            when(returns.cutByCpedIfCpedEmptyCutByPed(req.getEndDate())).thenReturn(returns);
            when(returns.getOriginalReturns()).thenReturn(navData);

            doCallRealMethod().when(sut).getDailyPerformanceCalculation(any(), anyMap(), anyMap());

            //ACT
            final DailyPerformanceCalculation result = sut.getDailyPerformanceCalculation(req, navData, distributionsData);

            //VERIFY
            Assertions.assertNotNull(result);
        }
    }

}
