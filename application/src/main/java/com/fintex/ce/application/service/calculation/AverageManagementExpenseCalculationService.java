package com.fintex.ce.application.service.calculation;

import com.fintex.ce.port.input.command.AverageMerCommand;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.enumeration.ParameterType;
import com.fintex.ce.domain.model.AverageManagementExpenseCalculationDTO;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.port.input.result.WarningResult;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.fintex.ce.constant.HoldingTypeGroup.FUNDS;
import static com.fintex.ce.domain.enumeration.ParameterType.*;
import static com.fintex.ce.util.DecimalUtils.divide;
import static com.fintex.ce.util.DecimalUtils.toUserScale;
import static com.fintex.ce.util.FilterUtils.getSpecifiedIfEmpty;
import static java.math.BigDecimal.ZERO;

/**
 * @param <R>
 *          result type. Return type of 'perform' method.
 */
public abstract class AverageManagementExpenseCalculationService<R extends WarningResult> {

  protected AverageManagementExpenseCalculationService() {
  }

  protected abstract Map<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> loadDataFromCacheStorage(
      final AverageMerCommand command);

  protected abstract List<Warning> setInitialFeeAndModifiedFeeValues(
      final Map<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> groupOfMers);

  protected abstract R calculateAverageValue(final List<ParameterType> parameterTypes,
      final Map<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> averageMerCalculationDtos);

  protected abstract void setNullForScaledAndForcedReportFeeIfHoldingContainsNoFunds(final R response,
      final AverageMerCommand command);

  public R perform(final AverageMerCommand command) {
    final Map<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> averageMerCalculationDTOs = loadDataFromCacheStorage(
        command);

    final List<Warning> warnings = setInitialFeeAndModifiedFeeValues(averageMerCalculationDTOs);
    final var resultDTO = calculateAverageValue(getSpecifiedIfEmpty(command.getParameterTypes(), SCALED, ABSOLUTE),
        averageMerCalculationDTOs);

    resultDTO.setWarnings(warnings);
    setNullForScaledAndForcedReportFeeIfHoldingContainsNoFunds(resultDTO, command);
    return resultDTO;
  }

  public void setNullForScaledAndForcedReportFeeIfHoldingContainsNoFunds(
      final Map<ParameterType, BigDecimal> responseMap,
      final AverageMerCommand command) {
    setNullForScaledIfHoldingContainsNoFunds(responseMap, command);
    setNullForForcedReportFeeIfHoldingContainsNoFunds(responseMap, command);
  }

  public void setNullForForcedReportFeeIfHoldingContainsNoFunds(final Map<ParameterType, BigDecimal> responseMap,
      final AverageMerCommand command) {
    if (command.getHoldings().stream().map(Holding::getType).noneMatch(FUNDS::contains)
        && responseMap.containsKey(FORCE_REPORT_FEE)) {
      responseMap.put(FORCE_REPORT_FEE, null);
    }
  }

  public void setNullForScaledIfHoldingContainsNoFunds(final Map<ParameterType, BigDecimal> responseMap,
      final AverageMerCommand command) {
    if (command.getHoldings().stream().map(Holding::getType).noneMatch(FUNDS::contains)
        && responseMap.containsKey(SCALED)) {
      responseMap.put(SCALED, null);
    }
  }

  public void setFeeValues(final AverageManagementExpenseCalculationDTO averageManagementExpenseCalculationDTO,
      final BigDecimal managementExpenseRatio) {
    averageManagementExpenseCalculationDTO.setInitialFee(managementExpenseRatio);
    averageManagementExpenseCalculationDTO.setModifiedFee(managementExpenseRatio);
  }

  public BigDecimal getScaledAverageMer(
      final Map<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> averageMerCalculationDtos) {
    final List<AverageManagementExpenseCalculationDTO> scaledAverageManagementExpenseCalculationDTOList = getAbsoluteAndForceReportFeeHoldingList(
        averageMerCalculationDtos);
    return getAverageMerByParameterType(scaledAverageManagementExpenseCalculationDTOList);
  }

  public BigDecimal getAbsoluteAverageMer(
      final Map<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> averageMerCalculationDtos) {
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
        averageMerCalculationDtos.get(HoldingType.US_MUTUAL_FUNDS))
        .filter(Objects::nonNull)
        .map(Map::values)
        .flatMap(Collection::stream)
        .toList();
    return getAverageMerByParameterType(scaledAverageManagementExpenseCalculationDTOList);
  }

  public List<AverageManagementExpenseCalculationDTO> getAbsoluteAndForceReportFeeHoldingList(
      final Map<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> resultMap) {
    return Stream.of(
        resultMap.get(HoldingType.CANADA_MUTUAL_FUNDS),
        resultMap.get(HoldingType.SEGREGATED_FUND_CANADA),
        resultMap.get(HoldingType.US_ETF),
        resultMap.get(HoldingType.CANADA_ETF),
        resultMap.get(HoldingType.CANADA_HEDGE_FUNDS),
        resultMap.get(HoldingType.US_MUTUAL_FUNDS))
        .filter(Objects::nonNull)
        .map(Map::values)
        .flatMap(Collection::stream)
        .toList();
  }

  public BigDecimal getAverageMerByParameterType(
      final List<AverageManagementExpenseCalculationDTO> merCalculationDTOList) {
    final BigDecimal amountOfMarketValues = getAmountOfMarketValues(merCalculationDTOList);
    final BigDecimal[] averageManagementExpenseRatio = {BigDecimal.ZERO};
    merCalculationDTOList.forEach(merCalculationDTO -> {
      calculateMarketValueQualified(merCalculationDTO);
      calculatePercentageQualified(merCalculationDTO, amountOfMarketValues);
      averageManagementExpenseRatio[0] = calculateAverageManagementExpenseRatio(averageManagementExpenseRatio[0],
          merCalculationDTO);
    });
    return toUserScale(averageManagementExpenseRatio[0]);
  }

  public BigDecimal getAmountOfMarketValues(
      final List<AverageManagementExpenseCalculationDTO> averageManagementExpenseCalculationDTOList) {
    return averageManagementExpenseCalculationDTOList.stream().map(
        AverageManagementExpenseCalculationDTO::getMarketValue).reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  public void calculateMarketValueQualified(
      final AverageManagementExpenseCalculationDTO averageManagementExpenseCalculationDTO) {
    if (Objects.isNull(averageManagementExpenseCalculationDTO.getModifiedFee())) {
      averageManagementExpenseCalculationDTO.setMarketValueQualified(ZERO);
    } else {
      averageManagementExpenseCalculationDTO.setMarketValueQualified(averageManagementExpenseCalculationDTO
          .getMarketValue());
    }
  }

  public void calculatePercentageQualified(
      final AverageManagementExpenseCalculationDTO averageManagementExpenseCalculationDTO,
      final BigDecimal amountOfMarketValues) {
    if (Objects.nonNull(averageManagementExpenseCalculationDTO.getMarketValueQualified())
        && !averageManagementExpenseCalculationDTO.getMarketValueQualified().equals(BigDecimal.ZERO)) {
      averageManagementExpenseCalculationDTO.setPercentageQualified(divide(averageManagementExpenseCalculationDTO
          .getMarketValueQualified(), amountOfMarketValues));
    }
  }

  public BigDecimal calculateAverageManagementExpenseRatio(final BigDecimal currentAverageManagementExpenseRatio,
      final AverageManagementExpenseCalculationDTO merCalculationDTO) {
    if (Objects.nonNull(merCalculationDTO.getModifiedFee()) && Objects.nonNull(merCalculationDTO
        .getPercentageQualified())) {
      return currentAverageManagementExpenseRatio.add(merCalculationDTO.getModifiedFee().multiply(merCalculationDTO
          .getPercentageQualified()));
    }
    return currentAverageManagementExpenseRatio;
  }

  public BigDecimal getForceReportFeeAverageMer(
      final Map<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> averageMerCalculationDtos) {
    if (isMerPresentForHolding(averageMerCalculationDtos.get(HoldingType.CANADA_MUTUAL_FUNDS),
        AverageManagementExpenseCalculationDTO::getManagementExpenseRatio) ||
        isMerPresentForHolding(averageMerCalculationDtos.get(HoldingType.SEGREGATED_FUND_CANADA),
            AverageManagementExpenseCalculationDTO::getManagementExpenseRatio) ||
        isMerPresentForHolding(averageMerCalculationDtos.get(HoldingType.CANADA_ETF),
            AverageManagementExpenseCalculationDTO::getManagementExpenseRatio) ||
        isMerPresentForHolding(averageMerCalculationDtos.get(HoldingType.US_ETF),
            AverageManagementExpenseCalculationDTO::getNetExpenseRatio) ||
        isMerPresentForHolding(averageMerCalculationDtos.get(HoldingType.CANADA_HEDGE_FUNDS),
            AverageManagementExpenseCalculationDTO::getManagementExpenseRatio) ||
        isMerPresentForHolding(averageMerCalculationDtos.get(HoldingType.US_MUTUAL_FUNDS),
            AverageManagementExpenseCalculationDTO::getNetExpenseRatio)) {
      return null;
    }
    final List<AverageManagementExpenseCalculationDTO> scaledAverageManagementExpenseCalculationDTOList = getAbsoluteAndForceReportFeeHoldingList(
        averageMerCalculationDtos);
    return getAverageMerByParameterType(scaledAverageManagementExpenseCalculationDTOList);
  }

  public boolean isMerPresentForHolding(final Map<Holding, AverageManagementExpenseCalculationDTO> merList,
      final Function<AverageManagementExpenseCalculationDTO, BigDecimal> function) {
    return Optional.ofNullable(merList).orElse(Map.of()).values().stream().anyMatch(r -> Objects.isNull(function.apply(
        r)));
  }

}
