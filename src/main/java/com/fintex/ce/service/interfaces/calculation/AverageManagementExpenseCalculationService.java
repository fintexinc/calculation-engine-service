package com.fintex.ce.service.interfaces.calculation;

import com.fintex.ce.config.enumeration.HoldingType;
import com.fintex.ce.config.enumeration.ParameterType;
import com.fintex.ce.dto.AverageManagementExpenseCalculationDTO;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.request.AverageMerRequestDTO;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.dto.response.core.WarningDTO;
import com.fintex.ce.util.validation.request.AverageMerRequestValidator;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.fintex.ce.config.constant.HoldingTypeGroup.FUNDS;
import static com.fintex.ce.config.enumeration.ParameterType.*;
import static com.fintex.ce.util.DecimalUtils.divide;
import static com.fintex.ce.util.DecimalUtils.toUserScale;
import static com.fintex.ce.util.FilterUtils.getSpecifiedIfEmpty;
import static java.math.BigDecimal.ZERO;

/**
 * @param <R> responseDTO type. Return type of 'perform' method.
 */
public abstract class AverageManagementExpenseCalculationService<R extends WarningDTO> {

    protected final AverageMerRequestValidator requestValidator;

    protected AverageManagementExpenseCalculationService(final AverageMerRequestValidator requestValidator) {
        this.requestValidator = requestValidator;
    }

    protected abstract Map<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> loadDataFromCacheStorage(final AverageMerRequestDTO req);

    protected abstract List<Warning> setInitialFeeAndModifiedFeeValues(final Map<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> groupOfMers);

    protected abstract R calculateAverageValue(final List<ParameterType> parameterTypes,
                                               final Map<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> averageMerCalculationDtos);

    protected abstract void setNullForScaledAndForcedReportFeeIfHoldingContainsNoFunds(final R response,
                                                                                       final AverageMerRequestDTO reqDTO);

    public R perform(final AverageMerRequestDTO req) {
        validateRequest(req);
        final Map<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> averageMerCalculationDTOs = loadDataFromCacheStorage(req);

        final List<Warning> warnings = setInitialFeeAndModifiedFeeValues(averageMerCalculationDTOs);
        final var resultDTO = calculateAverageValue(getSpecifiedIfEmpty(req.getParameterTypes(), SCALED, ABSOLUTE),
                averageMerCalculationDTOs);

        resultDTO.setWarnings(warnings);
        setNullForScaledAndForcedReportFeeIfHoldingContainsNoFunds(resultDTO, req);
        return resultDTO;
    }

    public void validateRequest(final AverageMerRequestDTO req) {
        requestValidator.validate(req);
    }

    public void setNullForScaledAndForcedReportFeeIfHoldingContainsNoFunds(final Map<ParameterType, BigDecimal> responseMap,
                                                                           final AverageMerRequestDTO reqDTO) {
        setNullForScaledIfHoldingContainsNoFunds(responseMap, reqDTO);
        setNullForForcedReportFeeIfHoldingContainsNoFunds(responseMap, reqDTO);
    }

    void setNullForForcedReportFeeIfHoldingContainsNoFunds(final Map<ParameterType, BigDecimal> responseMap,
                                                           final AverageMerRequestDTO reqDTO) {
        if (reqDTO.getHoldings().stream().map(Holding::getType).noneMatch(FUNDS::contains)
                && responseMap.containsKey(FORCE_REPORT_FEE)) {
            responseMap.put(FORCE_REPORT_FEE, null);
        }
    }

    public void setNullForScaledIfHoldingContainsNoFunds(final Map<ParameterType, BigDecimal> responseMap,
                                                         final AverageMerRequestDTO reqDTO) {
        if (reqDTO.getHoldings().stream().map(Holding::getType).noneMatch(FUNDS::contains)
                && responseMap.containsKey(SCALED)) {
            responseMap.put(SCALED, null);
        }
    }

    public void setFeeValues(final AverageManagementExpenseCalculationDTO averageManagementExpenseCalculationDTO, final BigDecimal managementExpenseRatio) {
        averageManagementExpenseCalculationDTO.setInitialFee(managementExpenseRatio);
        averageManagementExpenseCalculationDTO.setModifiedFee(managementExpenseRatio);
    }

    public BigDecimal getScaledAverageMer(final Map<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> averageMerCalculationDtos) {
        final List<AverageManagementExpenseCalculationDTO> scaledAverageManagementExpenseCalculationDTOList = getAbsoluteAndForceReportFeeHoldingList(averageMerCalculationDtos);
        return getAverageMerByParameterType(scaledAverageManagementExpenseCalculationDTOList);
    }

    public BigDecimal getAbsoluteAverageMer(final Map<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> averageMerCalculationDtos) {
        final List<AverageManagementExpenseCalculationDTO> scaledAverageManagementExpenseCalculationDTOList = Stream.of(
                        averageMerCalculationDtos.get(HoldingType.CANADA_MUTUAL_FUNDS),
                        averageMerCalculationDtos.get(HoldingType.SEGREGATED_FUND_CANADA),
                        averageMerCalculationDtos.get(HoldingType.US_ETF),
                        averageMerCalculationDtos.get(HoldingType.CANADA_ETF),
                        averageMerCalculationDtos.get(HoldingType.US_STOCKS),
                        averageMerCalculationDtos.get(HoldingType.CANADA_STOCKS),
                        averageMerCalculationDtos.get(HoldingType.CASH),
                        averageMerCalculationDtos.get(HoldingType.GIC),
                        averageMerCalculationDtos.get(HoldingType.CANADA_POOLED_FUNDS),
                        averageMerCalculationDtos.get(HoldingType.CANADA_HEDGE_FUNDS),
                        averageMerCalculationDtos.get(HoldingType.US_MUTUAL_FUNDS)
                )
                .filter(Objects::nonNull)
                .map(Map::values)
                .flatMap(Collection::stream)
                .toList();
        return getAverageMerByParameterType(scaledAverageManagementExpenseCalculationDTOList);
    }

    List<AverageManagementExpenseCalculationDTO> getAbsoluteAndForceReportFeeHoldingList(final Map<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> resultMap) {
        return Stream.of(
                        resultMap.get(HoldingType.CANADA_MUTUAL_FUNDS),
                        resultMap.get(HoldingType.SEGREGATED_FUND_CANADA),
                        resultMap.get(HoldingType.US_ETF),
                        resultMap.get(HoldingType.CANADA_ETF),
                        resultMap.get(HoldingType.CANADA_HEDGE_FUNDS),
                        resultMap.get(HoldingType.US_MUTUAL_FUNDS)
                )
                .filter(Objects::nonNull)
                .map(Map::values)
                .flatMap(Collection::stream)
                .toList();
    }

    BigDecimal getAverageMerByParameterType(final List<AverageManagementExpenseCalculationDTO> merCalculationDTOList) {
        final BigDecimal amountOfMarketValues = getAmountOfMarketValues(merCalculationDTOList);
        final BigDecimal[] averageManagementExpenseRatio = {BigDecimal.ZERO};
        merCalculationDTOList.forEach(merCalculationDTO -> {
            calculateMarketValueQualified(merCalculationDTO);
            calculatePercentageQualified(merCalculationDTO, amountOfMarketValues);
            averageManagementExpenseRatio[0] = calculateAverageManagementExpenseRatio(averageManagementExpenseRatio[0], merCalculationDTO);
        });
        return toUserScale(averageManagementExpenseRatio[0]);
    }

    BigDecimal getAmountOfMarketValues(final List<AverageManagementExpenseCalculationDTO> averageManagementExpenseCalculationDTOList) {
        return averageManagementExpenseCalculationDTOList.stream().map(AverageManagementExpenseCalculationDTO::getMarketValue).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    void calculateMarketValueQualified(final AverageManagementExpenseCalculationDTO averageManagementExpenseCalculationDTO) {
        if (Objects.isNull(averageManagementExpenseCalculationDTO.getModifiedFee())) {
            averageManagementExpenseCalculationDTO.setMarketValueQualified(ZERO);
        } else {
            averageManagementExpenseCalculationDTO.setMarketValueQualified(averageManagementExpenseCalculationDTO.getMarketValue());
        }
    }

    void calculatePercentageQualified(final AverageManagementExpenseCalculationDTO averageManagementExpenseCalculationDTO, final BigDecimal amountOfMarketValues) {
        if (Objects.nonNull(averageManagementExpenseCalculationDTO.getMarketValueQualified()) && !averageManagementExpenseCalculationDTO.getMarketValueQualified().equals(BigDecimal.ZERO)) {
            averageManagementExpenseCalculationDTO.setPercentageQualified(divide(averageManagementExpenseCalculationDTO.getMarketValueQualified(), amountOfMarketValues));
        }
    }

    BigDecimal calculateAverageManagementExpenseRatio(final BigDecimal currentAverageManagementExpenseRatio, final AverageManagementExpenseCalculationDTO merCalculationDTO) {
        if (Objects.nonNull(merCalculationDTO.getModifiedFee()) && Objects.nonNull(merCalculationDTO.getPercentageQualified())) {
            return currentAverageManagementExpenseRatio.add(merCalculationDTO.getModifiedFee().multiply(merCalculationDTO.getPercentageQualified()));
        }
        return currentAverageManagementExpenseRatio;
    }

    public BigDecimal getForceReportFeeAverageMer(final Map<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> averageMerCalculationDtos) {
        if (isMerPresentForHolding(averageMerCalculationDtos.get(HoldingType.CANADA_MUTUAL_FUNDS), AverageManagementExpenseCalculationDTO::getManagementExpenseRatio) ||
                isMerPresentForHolding(averageMerCalculationDtos.get(HoldingType.SEGREGATED_FUND_CANADA), AverageManagementExpenseCalculationDTO::getManagementExpenseRatio) ||
                isMerPresentForHolding(averageMerCalculationDtos.get(HoldingType.CANADA_ETF), AverageManagementExpenseCalculationDTO::getManagementExpenseRatio) ||
                isMerPresentForHolding(averageMerCalculationDtos.get(HoldingType.US_ETF), AverageManagementExpenseCalculationDTO::getNetExpenseRatio) ||
                isMerPresentForHolding(averageMerCalculationDtos.get(HoldingType.CANADA_HEDGE_FUNDS), AverageManagementExpenseCalculationDTO::getManagementExpenseRatio) ||
                isMerPresentForHolding(averageMerCalculationDtos.get(HoldingType.US_MUTUAL_FUNDS), AverageManagementExpenseCalculationDTO::getNetExpenseRatio)) {
            return null;
        }
        final List<AverageManagementExpenseCalculationDTO> scaledAverageManagementExpenseCalculationDTOList = getAbsoluteAndForceReportFeeHoldingList(averageMerCalculationDtos);
        return getAverageMerByParameterType(scaledAverageManagementExpenseCalculationDTOList);
    }

    public boolean isMerPresentForHolding(final Map<Holding, AverageManagementExpenseCalculationDTO> merList, final Function<AverageManagementExpenseCalculationDTO, BigDecimal> function) {
        return Optional.ofNullable(merList).orElse(Map.of()).values().stream().anyMatch(r -> Objects.isNull(function.apply(r)));
    }

}
