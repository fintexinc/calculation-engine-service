package com.fintex.ce.service.impl.calculation;

import com.fintex.ce.config.enumeration.ExceptionCode;
import com.fintex.ce.domain.calculation.DailyPerformanceCalculation;
import com.fintex.ce.domain.calculation.Growth10KCalculation;
import com.fintex.ce.domain.monthlyreturns.PortfolioCpedDataValidation;
import com.fintex.ce.domain.monthlyreturns.PortfolioCpsdDataValidation;
import com.fintex.ce.domain.monthlyreturns.Returns;
import com.fintex.ce.domain.monthlyreturns.WeightedAverageComponent;
import com.fintex.ce.dto.CommonDates;
import com.fintex.ce.dto.calculation.CalculationDTO;
import com.fintex.ce.dto.calculation.HoldingForDailyCalculationDTO;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.request.DailyPerformanceReqDTO;
import com.fintex.ce.dto.request.GrowthOf10KReqDTO;
import com.fintex.ce.dto.request.ReturnReqDTO;
import com.fintex.ce.dto.response.DailyGrowthOf10KDTO;
import com.fintex.ce.dto.response.Growth10KResDTO;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.exception.DataErrorException;
import com.fintex.ce.exception.FdsDataValidationException;
import com.fintex.ce.model.redis.RHistoricalDistributions;
import com.fintex.ce.model.redis.RHistoricalNavPrices;
import com.fintex.ce.model.redis.RMonthlyReturns;
import com.fintex.ce.service.impl.cache.HistoricalDistributionsCacheStorage;
import com.fintex.ce.service.interfaces.calculation.GrowthOf10KCalculationService;
import com.fintex.ce.util.validation.request.ReturnReqDtoValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static com.fintex.ce.config.constant.BigDecimalConstants.TEN_THOUSAND;
import static com.fintex.ce.util.DateTimeUtils.toFirstDayOfMonth;
import static com.fintex.ce.util.ReturnFactorScale.AS_IS;
import static com.fintex.ce.util.ReturnFactorScale.SCALE_OF_TWO;

@Service
public class GrowthOf10KCalculationServiceImpl implements GrowthOf10KCalculationService {

    private final MonthlyReturnsService monthlyReturnsService;
    private final ReturnReqDtoValidator requestValidator;
    private final DailyPerformanceCalculationServiceImpl dailyPerformanceCalculationService;
    private final HistoricalDistributionsCacheStorage historicalDistributionsCacheStorage;

    @Autowired
    public GrowthOf10KCalculationServiceImpl(final MonthlyReturnsService monthlyReturnsService,
                                             final ReturnReqDtoValidator requestValidator,
                                             final DailyPerformanceCalculationServiceImpl dailyPerformanceCalculationService,
                                             final HistoricalDistributionsCacheStorage historicalDistributionsCacheStorage) {
        this.monthlyReturnsService = monthlyReturnsService;
        this.requestValidator = requestValidator;
        this.dailyPerformanceCalculationService = dailyPerformanceCalculationService;
        this.historicalDistributionsCacheStorage = historicalDistributionsCacheStorage;
    }

    @Override
    public Growth10KResDTO perform(final GrowthOf10KReqDTO reqDTO) {
        requestValidator.validate(reqDTO);
        if (reqDTO.isUseNAV()) {
            return calculateNAVGrowthOf10K(reqDTO);
        }
        return calculateDefaultGrowthOf10K(reqDTO);
    }

    private Growth10KResDTO calculateDefaultGrowthOf10K(final GrowthOf10KReqDTO reqDTO) {
        final CalculationDTO inputDTO = buildCalculationDto(reqDTO);
        Growth10KCalculation growth10KCalculation = buildGrowth10kCalculation(reqDTO, inputDTO);
        return growth10KCalculation.calculate();
    }

    private Growth10KResDTO calculateNAVGrowthOf10K(final GrowthOf10KReqDTO reqDTO) {
        final CalculationDTO inputDTO = buildCalculationDtoFromDailyData(reqDTO);
        Growth10KCalculation growth10KCalculation = buildGrowth10kCalculationForNAV(reqDTO, inputDTO);
        return growth10KCalculation.calculate();
    }

    Growth10KCalculation buildGrowth10kCalculation(ReturnReqDTO reqDTO, CalculationDTO inputDTO) {
        return new Growth10KCalculation(
                inputDTO.getWeightedAveragePortfolioReturns(),
                new CommonDates(reqDTO.getCustomPerformanceStartDate(), reqDTO.getCustomPerformanceEndDate()),
                false,
                inputDTO.getWarnings());
    }

    Growth10KCalculation buildGrowth10kCalculationForNAV(ReturnReqDTO reqDTO, CalculationDTO inputDTO) {
        return new Growth10KCalculation(
                inputDTO.getWeightedAveragePortfolioReturns(),
                new CommonDates(reqDTO.getCustomPerformanceStartDate(), reqDTO.getCustomPerformanceEndDate()),
                true);
    }

    protected CalculationDTO buildCalculationDto(final ReturnReqDTO reqDTO) {
        final Returns<RMonthlyReturns> monthlyReturns =
                monthlyReturnsService.getPortfolioMonthlyReturns(reqDTO.getHoldings(), reqDTO.getCurrency(), SCALE_OF_TWO);

        monthlyReturns
                .setCpedDataValidation(new PortfolioCpedDataValidation())
                .setCpsdDataValidation(new PortfolioCpsdDataValidation());

        final NavigableMap<LocalDate, BigDecimal> portfolioTotalReturns = monthlyReturnsService
                .getWeightedAverageWithCpsdAndCpedValidation(monthlyReturns, reqDTO.getCustomPerformanceStartDate(), reqDTO.getCustomPerformanceEndDate());

        return new CalculationDTO().setWeightedAveragePortfolioReturns(portfolioTotalReturns).setWarnings(monthlyReturns.getErrorsAsWarnings());
    }

    private CalculationDTO buildCalculationDtoFromDailyData(final ReturnReqDTO reqDTO) {
        final DailyPerformanceReqDTO dailyPerformanceCalculationReqDTO = buildDailyPerformanceCalculationReqDTO(reqDTO);
        final DailyGrowthOf10KDTO dailyGrowthOf10KDTO = performForGrowthOf10KCalculation(dailyPerformanceCalculationReqDTO);
        final WeightedAverageComponent weightedAverageComponent = new WeightedAverageComponent(AS_IS);
        final NavigableMap<LocalDate, BigDecimal> portfolioTotalReturns = weightedAverageComponent.calculateWeightedAverage(dailyGrowthOf10KDTO.getDailyGrowthOf10K());

        return new CalculationDTO().setWeightedAveragePortfolioReturns(portfolioTotalReturns);
    }

    private DailyPerformanceReqDTO buildDailyPerformanceCalculationReqDTO(final ReturnReqDTO reqDTO) {
        final List<HoldingForDailyCalculationDTO> holdings = reqDTO.getHoldings().stream()
                .map(holding -> new HoldingForDailyCalculationDTO(holding, TEN_THOUSAND))
                .toList();
        final DailyPerformanceReqDTO dailyPerformanceCalculationReqDTO = new DailyPerformanceReqDTO();
        dailyPerformanceCalculationReqDTO.setDailyHoldings(holdings);
        dailyPerformanceCalculationReqDTO.setStartDate(toFirstDayOfMonth(reqDTO.getCustomPerformanceStartDate()));
        dailyPerformanceCalculationReqDTO.setEndDate(reqDTO.getCustomPerformanceEndDate());
        return dailyPerformanceCalculationReqDTO;
    }

    private DailyGrowthOf10KDTO performForGrowthOf10KCalculation(final DailyPerformanceReqDTO reqDTO) {
        var warnings = new ArrayList<Warning>();
        var holdings = dailyPerformanceCalculationService.validateRequestAndGetHoldings(reqDTO);
        var navData = dailyPerformanceCalculationService.validateRequestAndGetNAVData(warnings, holdings);
        var distributionsData = historicalDistributionsCacheStorage.load(holdings, List.of(), List.of(), null);

        if (!warnings.isEmpty()) {
            throw new FdsDataValidationException(fromWarningsToDataErrorException(warnings));
        }

        return calculateDailyPerformance(reqDTO, navData, distributionsData, warnings);
    }

    private DailyGrowthOf10KDTO calculateDailyPerformance(final DailyPerformanceReqDTO reqDTO, final Map<Holding, RHistoricalNavPrices> navData,
                                                          final Map<Holding, RHistoricalDistributions> distributionsData, final List<Warning> warnings) {
        final DailyPerformanceCalculation calculation = getDailyPerformanceCalculation(reqDTO, navData, distributionsData);
        final Map<Holding, TreeMap<LocalDate, BigDecimal>> result = calculation.calculateForGrowthOf10K();

        var dailyGrowthOf10KDTO = new DailyGrowthOf10KDTO();
        dailyGrowthOf10KDTO.setDailyGrowthOf10K(result);
        dailyGrowthOf10KDTO.getWarnings().addAll(warnings);
        return dailyGrowthOf10KDTO;
    }

    private DailyPerformanceCalculation getDailyPerformanceCalculation(DailyPerformanceReqDTO reqDTO, Map<Holding, RHistoricalNavPrices> navData, Map<Holding, RHistoricalDistributions> distributionsData) {
        var holdingAndDailyHolding = reqDTO.getDailyHoldings()
                .stream()
                .collect(Collectors.toMap(HoldingForDailyCalculationDTO::getHolding, e -> e));

        var dailyReturns = Returns.initForNavPrices(navData)
                .validateCpsd(reqDTO.getStartDate())
                .validateCped(reqDTO.getEndDate())
                .validateMonthlyDataMissing(navData, reqDTO)
                .ifAnyErrorsThrowException()
                .cutByCpsdIfCpsdEmptyCutByPsd(reqDTO.getStartDate())
                .cutByCpedIfCpedEmptyCutByPed(reqDTO.getEndDate())
                .getOriginalReturns();
        return new DailyPerformanceCalculation(dailyReturns, distributionsData, holdingAndDailyHolding);
    }

    private List<DataErrorException> fromWarningsToDataErrorException(final List<Warning> warnings) {
        return warnings.stream()
                .map(warn -> new DataErrorException(warn.getMessage(), warn.getId(), ExceptionCode.valueOf(warn.getCode())))
                .toList();
    }

}
