package com.fintex.ce.service.impl.calculation;

import com.fintex.ce.domain.calculation.DailyPerformanceCalculation;
import com.fintex.ce.domain.monthlyreturns.Returns;
import com.fintex.ce.dto.calculation.HoldingForDailyCalculationDTO;
import com.fintex.ce.dto.calculation.InvestmentDataDTO;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.request.DailyPerformanceReqDTO;
import com.fintex.ce.dto.response.DailyPerformanceResDTO;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.exception.DataErrorException;
import com.fintex.ce.model.redis.RHistoricalDistributions;
import com.fintex.ce.model.redis.RHistoricalNavPrices;
import com.fintex.ce.service.impl.cache.HistoricalDistributionsCacheStorage;
import com.fintex.ce.service.impl.cache.HistoryNavPricesCacheStorage;
import com.fintex.ce.service.interfaces.calculation.DailyPerformanceCalculationService;
import com.fintex.ce.util.validation.request.DailyPerformanceRequestValidator;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.fintex.ce.config.enumeration.DailyResultType.NO_REINVEST_WITHOUT_PAC_AND_WITHDRAWAL;
import static com.fintex.ce.config.enumeration.DailyResultType.NO_REINVEST_WITH_PAC_AND_WITHDRAWAL;
import static com.fintex.ce.config.enumeration.DailyResultType.REINVEST_WITHOUT_PAC_AND_WITHDRAWAL;
import static com.fintex.ce.config.enumeration.DailyResultType.REINVEST_WITH_PAC_AND_WITHDRAWAL;
import static com.fintex.ce.config.enumeration.ExceptionCode.ERR_NAV_PRICES_001;

@Service
@AllArgsConstructor
public class DailyPerformanceCalculationServiceImpl implements DailyPerformanceCalculationService {

    private HistoryNavPricesCacheStorage historyNavPricesCacheStorage;
    private HistoricalDistributionsCacheStorage historicalDistributionsCacheStorage;
    private DailyPerformanceRequestValidator requestValidator;

    @Override
    public DailyPerformanceResDTO perform(final DailyPerformanceReqDTO reqDTO) {
        reqDTO.getDailyHoldings().forEach(InvestmentDataDTO::validateAndUpdateInvestmentDataDTO);
        var warnings = new ArrayList<Warning>();
        var holdings = validateRequestAndGetHoldings(reqDTO);
        var navData = validateRequestAndGetNAVData(warnings, holdings);
        var distributionsData = historicalDistributionsCacheStorage.load(holdings, List.of(), List.of(), null);

        if (!warnings.isEmpty() && warnings.size() == navData.size()) {
            var result = new DailyPerformanceResDTO();
            result.getWarnings().addAll(warnings);
            return result;
        }

        return calculateDailyPerformance(reqDTO, navData, distributionsData, warnings);
    }

    @Override
    public List<Holding> validateRequestAndGetHoldings(final DailyPerformanceReqDTO reqDTO) {
        requestValidator.validate(reqDTO);
        return reqDTO.getDailyHoldings()
                .stream()
                .map(HoldingForDailyCalculationDTO::getHolding)
                .collect(Collectors.toList());
    }

    @Override
    public Map<Holding, RHistoricalNavPrices> validateRequestAndGetNAVData(final List<Warning> warnings, final List<Holding> holdings) {
        var navData = historyNavPricesCacheStorage.load(holdings, List.of(), List.of(), null);
        validateNavPricesExistence(navData, warnings);
        return navData;
    }

    @Override
    public DailyPerformanceCalculation getDailyPerformanceCalculation(DailyPerformanceReqDTO reqDTO,
                                                                      Map<Holding, RHistoricalNavPrices> navData,
                                                                      Map<Holding, RHistoricalDistributions> distributionsData) {
        var holdingAndDailyHolding = reqDTO.getDailyHoldings()
                .stream()
                .collect(Collectors.toMap(HoldingForDailyCalculationDTO::getHolding, e -> e));

        var dailyReturns = Returns.initForNavPrices(navData)
                .validateAndUpdateCpsdAndCped(navData, reqDTO)
                .validateMonthlyDataMissing(navData, reqDTO)
                .ifAnyErrorsThrowException()
                .cutByCpsdIfCpsdEmptyCutByPsd(reqDTO.getStartDate())
                .cutByCpedIfCpedEmptyCutByPed(reqDTO.getEndDate())
                .getOriginalReturns();
        return new DailyPerformanceCalculation(dailyReturns, distributionsData, holdingAndDailyHolding);
    }

    private void validateNavPricesExistence(final Map<Holding, RHistoricalNavPrices> holdingReturns, final List<Warning> warnings) {
        holdingReturns.forEach((k, v) -> {
            final DataErrorException errorException = v.getErrors().stream().filter(e -> e.getCode() == ERR_NAV_PRICES_001).findFirst().orElse(null);
            if (Objects.nonNull(errorException)) {
                warnings.add(ERR_NAV_PRICES_001.warning(k));
            }
        });
    }

    private DailyPerformanceResDTO calculateDailyPerformance(final DailyPerformanceReqDTO reqDTO, final Map<Holding, RHistoricalNavPrices> navData,
                                                             final Map<Holding, RHistoricalDistributions> distributionsData, final List<Warning> warnings) {
        final DailyPerformanceCalculation calculation = getDailyPerformanceCalculation(reqDTO, navData, distributionsData);
        var result = new DailyPerformanceResDTO();
        result.add(REINVEST_WITH_PAC_AND_WITHDRAWAL, calculation.calculate(true, true));
        result.add(NO_REINVEST_WITH_PAC_AND_WITHDRAWAL, calculation.calculate(false, true));
        result.add(REINVEST_WITHOUT_PAC_AND_WITHDRAWAL, calculation.calculate(true, false));
        result.add(NO_REINVEST_WITHOUT_PAC_AND_WITHDRAWAL, calculation.calculate(false, false));
        result.aggregate();
        result.getWarnings().addAll(warnings);
        populateStartAndEndDate(result, navData);
        return result;
    }

    private void populateStartAndEndDate(final DailyPerformanceResDTO result, final Map<Holding, RHistoricalNavPrices> navData) {
        final DailyPerformanceReqDTO reqDTO = new DailyPerformanceReqDTO();
        Returns.initForNavPrices(navData)
                .validateEarliestAndLatestAvailableDate(navData, reqDTO);
        result.setPerformanceStartDate(reqDTO.getStartDate());
        result.setPerformanceEndDate(reqDTO.getEndDate());
    }

}
