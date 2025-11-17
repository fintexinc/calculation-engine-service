package com.fintex.ce.service.impl.calculation;

import com.fintex.ce.domain.calculation.DailyPerformanceCalculation;
import com.fintex.ce.dto.calculation.DistributionData;
import com.fintex.ce.dto.calculation.InvestmentDataDTO;
import com.fintex.ce.dto.request.DailyPerformanceReqDTO;
import com.fintex.ce.dto.response.DistributionResDTO;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.service.impl.cache.HistoricalDistributionsCacheStorage;
import com.fintex.ce.service.interfaces.DistributionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static com.fintex.ce.config.enumeration.DailyResultType.NO_REINVEST_WITHOUT_PAC_AND_WITHDRAWAL;
import static com.fintex.ce.config.enumeration.DailyResultType.NO_REINVEST_WITH_PAC_AND_WITHDRAWAL;
import static com.fintex.ce.config.enumeration.DailyResultType.REINVEST_WITHOUT_PAC_AND_WITHDRAWAL;
import static com.fintex.ce.config.enumeration.DailyResultType.REINVEST_WITH_PAC_AND_WITHDRAWAL;

@Service
public class DistributionServiceImpl implements DistributionService {

    private final DailyPerformanceCalculationServiceImpl dailyPerformanceCalculationService;
    private final HistoricalDistributionsCacheStorage historicalDistributionsCacheStorage;

    @Autowired
    public DistributionServiceImpl(final DailyPerformanceCalculationServiceImpl dailyPerformanceCalculationService,
                                   final HistoricalDistributionsCacheStorage historicalDistributionsCacheStorage) {
        this.dailyPerformanceCalculationService = dailyPerformanceCalculationService;
        this.historicalDistributionsCacheStorage = historicalDistributionsCacheStorage;
    }

    @Override
    public DistributionResDTO perform(final DailyPerformanceReqDTO reqDTO) {
        reqDTO.getDailyHoldings().forEach(InvestmentDataDTO::validateAndUpdateInvestmentDataDTO);
        var warnings = new ArrayList<Warning>();
        var holdings = dailyPerformanceCalculationService.validateRequestAndGetHoldings(reqDTO);
        var navData = dailyPerformanceCalculationService.validateRequestAndGetNAVData(warnings, holdings);
        var distributionsData = historicalDistributionsCacheStorage.load(holdings, List.of(), List.of(), null);

        if (navData.size() <= 1 && !warnings.isEmpty()) {
            var result = new DistributionResDTO();
            result.getWarnings().addAll(warnings);
            return result;
        }

        DailyPerformanceCalculation calculation = dailyPerformanceCalculationService.getDailyPerformanceCalculation(reqDTO, navData, distributionsData);
        return calculateDistribution(warnings, calculation);
    }

    private DistributionResDTO calculateDistribution(final List<Warning> warnings, final DailyPerformanceCalculation calculation) {
        var result = new DistributionResDTO();
        result.getDistribution().put(REINVEST_WITH_PAC_AND_WITHDRAWAL, calculateTotalDistribution(calculation.calculateDistribution(true, true)));
        result.getDistribution().put(NO_REINVEST_WITH_PAC_AND_WITHDRAWAL, calculateTotalDistribution(calculation.calculateDistribution(false, true)));
        result.getDistribution().put(REINVEST_WITHOUT_PAC_AND_WITHDRAWAL, calculateTotalDistribution(calculation.calculateDistribution(true, false)));
        result.getDistribution().put(NO_REINVEST_WITHOUT_PAC_AND_WITHDRAWAL, calculateTotalDistribution(calculation.calculateDistribution(false, false)));
        result.getWarnings().addAll(warnings);
        return result;
    }

    private TreeMap<LocalDate, DistributionData> calculateTotalDistribution(Map<String, TreeMap<LocalDate, DistributionData>> distributionMap) {
        final Set<LocalDate> dates = getAllDatesWhenDistributionIsNotZero(distributionMap);
        var result = new TreeMap<LocalDate, DistributionData>();

        for (LocalDate date : dates) {
            final List<DistributionData> distributionDataList = distributionMap.values().stream()
                    .map(m -> m.get(date))
                    .collect(Collectors.toList());

            final DistributionData distributionData = getDistributionData(distributionDataList);
            result.put(date, distributionData);
        }
        return result;
    }

    private static DistributionData getDistributionData(List<DistributionData> distributionDataList) {
        final BigDecimal totalDistribution = distributionDataList.stream().map(DistributionData::getFundDistribution).filter(Objects::nonNull).reduce(BigDecimal::add).get();
        final BigDecimal totalInterest = distributionDataList.stream().map(DistributionData::getInterest).filter(Objects::nonNull).reduce(BigDecimal::add).get();
        final BigDecimal totalCanadianDividend = distributionDataList.stream().map(DistributionData::getCanadianDividend).filter(Objects::nonNull).reduce(BigDecimal::add).get();
        final BigDecimal totalForeignDividend = distributionDataList.stream().map(DistributionData::getForeignDividend).filter(Objects::nonNull).reduce(BigDecimal::add).get();
        final BigDecimal totalCapitalGains = distributionDataList.stream().map(DistributionData::getCapitalGains).filter(Objects::nonNull).reduce(BigDecimal::add).get();
        final BigDecimal totalReturnOfCapital = distributionDataList.stream().map(DistributionData::getReturnOfCapital).filter(Objects::nonNull).reduce(BigDecimal::add).get();
        final BigDecimal marketValue = distributionDataList.stream().map(DistributionData::getFundValue).reduce(BigDecimal::add).get();

        return DistributionData.builder()
                .totalDistribution(totalDistribution)
                .marketValue(marketValue)
                .interest(totalInterest)
                .canadianDividend(totalCanadianDividend)
                .foreignDividend(totalForeignDividend)
                .capitalGains(totalCapitalGains)
                .returnOfCapital(totalReturnOfCapital)
                .build();
    }

    private Set<LocalDate> getAllDatesWhenDistributionIsNotZero(Map<String, TreeMap<LocalDate, DistributionData>> distributionMap) {
        final Set<LocalDate> dates = new TreeSet<>();
        distributionMap.forEach((k, v) -> v.forEach((date, distribution) -> {
            if (Objects.nonNull(distribution.getFundDistribution())) {
                dates.add(date);
            }
        }));
        return dates;
    }

}
