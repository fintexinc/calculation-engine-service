package com.fintex.ce.service.interfaces.calculation;

import com.fintex.ce.domain.calculation.DailyPerformanceCalculation;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.request.DailyPerformanceReqDTO;
import com.fintex.ce.dto.response.DailyPerformanceResDTO;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.model.redis.RHistoricalDistributions;
import com.fintex.ce.model.redis.RHistoricalNavPrices;

import java.util.List;
import java.util.Map;

public interface DailyPerformanceCalculationService {
    DailyPerformanceResDTO perform(DailyPerformanceReqDTO reqDTO);

    List<Holding> validateRequestAndGetHoldings(DailyPerformanceReqDTO reqDTO);

    Map<Holding, RHistoricalNavPrices> validateRequestAndGetNAVData(List<Warning> warnings, List<Holding> holdings);

    DailyPerformanceCalculation getDailyPerformanceCalculation(DailyPerformanceReqDTO reqDTO, Map<Holding, RHistoricalNavPrices> navData, Map<Holding, RHistoricalDistributions> distributionsData);
}
