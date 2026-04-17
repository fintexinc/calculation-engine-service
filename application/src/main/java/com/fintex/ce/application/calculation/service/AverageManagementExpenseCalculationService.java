package com.fintex.ce.application.calculation.service;

import com.fintex.ce.calculation.CalculationService;
import com.fintex.ce.model.domain.calculation.fee.AverageManagementExpenseCalculation;
import com.fintex.ce.model.domain.calculation.fee.FeeData;
import com.fintex.ce.model.domain.enumeration.ParameterType;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.WarningResult;
import com.fintex.ce.model.dto.command.AverageMerCommand;
import com.fintex.ce.model.error.Warning;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.fintex.ce.constant.HoldingTypeGroup.FUNDS;
import static com.fintex.ce.model.domain.enumeration.ParameterType.ABSOLUTE;
import static com.fintex.ce.model.domain.enumeration.ParameterType.FORCE_REPORT_FEE;
import static com.fintex.ce.model.domain.enumeration.ParameterType.SCALED;
import static com.fintex.ce.util.DecimalUtils.divide;
import static com.fintex.ce.util.DecimalUtils.toUserScale;
import static com.fintex.ce.util.FilterUtils.getSpecifiedIfEmpty;
import static java.math.BigDecimal.ZERO;

/**
 * @param <R>
 *          result type. Return type of 'perform' method.
 */
public abstract class AverageManagementExpenseCalculationService<R extends WarningResult>
    implements
      CalculationService<R, AverageMerCommand> {

  protected AverageManagementExpenseCalculationService() {
  }

  /**
   * Maps FeeData to AverageManagementExpenseCalculation. Subclasses implement this to define which fee fields to
   * extract.
   */
  protected abstract AverageManagementExpenseCalculation mapFeeDataToDto(PortfolioHolding holding, FeeData fees);

  protected abstract Map<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> fetchData(
      final AverageMerCommand command);

  /**
   * Groups raw fee data by holding type and maps to calculation DTOs. Holdings without fee data get a default DTO with
   * zero fees.
   */
  protected Map<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> groupAndMap(
      Map<PortfolioHolding, FeeData> rawData, List<? extends PortfolioHolding> holdings) {

    final Stream<Map.Entry<PortfolioHolding, AverageManagementExpenseCalculation>> fetched = rawData.entrySet().stream()
        .map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), mapFeeDataToDto(e.getKey(), e.getValue())));

    final Stream<Map.Entry<PortfolioHolding, AverageManagementExpenseCalculation>> defaults = holdings.stream()
        .filter(holding -> !rawData.containsKey(holding))
        .map(holding -> new AbstractMap.SimpleEntry<>(holding, createDefaultDto(holding)));

    return Stream.concat(fetched, defaults)
        .collect(Collectors.groupingBy(
            e -> e.getKey().getHoldingType(),
            () -> new EnumMap<>(FinancialInstrumentType.class),
            Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
  }

  private AverageManagementExpenseCalculation createDefaultDto(PortfolioHolding holding) {
    return AverageManagementExpenseCalculation.builder()
        .marketValue(holding.getValue())
        .holdingType(holding.getHoldingType())
        .initialFee(BigDecimal.ZERO)
        .modifiedFee(BigDecimal.ZERO)
        .build();
  }

  protected abstract List<Warning> setInitialFeeAndModifiedFeeValues(
      final Map<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> groupOfMers);

  protected abstract R calculateAverageValue(final List<ParameterType> parameterTypes,
      final Map<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> averageMerCalculationDtos);

  protected abstract void setNullForScaledAndForcedReportFeeIfHoldingContainsNoFunds(final R response,
      final AverageMerCommand command);

  @Override
  public R perform(final AverageMerCommand command) {
    final Map<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> averageMerCalculationDTOs = fetchData(
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
    if (command.getHoldings().stream().map(PortfolioHolding::getHoldingType).noneMatch(FUNDS::contains)
        && responseMap.containsKey(FORCE_REPORT_FEE)) {
      responseMap.put(FORCE_REPORT_FEE, null);
    }
  }

  public void setNullForScaledIfHoldingContainsNoFunds(final Map<ParameterType, BigDecimal> responseMap,
      final AverageMerCommand command) {
    if (command.getHoldings().stream().map(PortfolioHolding::getHoldingType).noneMatch(FUNDS::contains)
        && responseMap.containsKey(SCALED)) {
      responseMap.put(SCALED, null);
    }
  }

  public void setFeeValues(final AverageManagementExpenseCalculation averageManagementExpenseCalculationDTO,
      final BigDecimal managementExpenseRatio) {
    averageManagementExpenseCalculationDTO.setInitialFee(managementExpenseRatio);
    averageManagementExpenseCalculationDTO.setModifiedFee(managementExpenseRatio);
  }

  public BigDecimal getScaledAverageMer(
      final Map<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> averageMerCalculationDtos) {
    final List<AverageManagementExpenseCalculation> scaledAverageManagementExpenseCalculationList = getAbsoluteAndForceReportFeeHoldingList(
        averageMerCalculationDtos);
    return getAverageMerByParameterType(scaledAverageManagementExpenseCalculationList);
  }

  public BigDecimal getAbsoluteAverageMer(
      final Map<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> averageMerCalculationDtos) {
    final List<AverageManagementExpenseCalculation> scaledAverageManagementExpenseCalculationList = Stream.of(
        averageMerCalculationDtos.get(FinancialInstrumentType.MUTUAL_FUND_CANADA),
        averageMerCalculationDtos.get(FinancialInstrumentType.SEGREGATED_FUND_CANADA),
        averageMerCalculationDtos.get(FinancialInstrumentType.ETF_US),
        averageMerCalculationDtos.get(FinancialInstrumentType.ETF_CANADA),
        averageMerCalculationDtos.get(FinancialInstrumentType.STOCK_US),
        averageMerCalculationDtos.get(FinancialInstrumentType.STOCK_CANADA),
        averageMerCalculationDtos.get(FinancialInstrumentType.CASH),
        averageMerCalculationDtos.get(FinancialInstrumentType.GIC),
        averageMerCalculationDtos.get(FinancialInstrumentType.POOLED_FUND_CANADA),
        averageMerCalculationDtos.get(FinancialInstrumentType.HEDGE_FUND_CANADA),
        averageMerCalculationDtos.get(FinancialInstrumentType.MUTUAL_FUND_US))
        .filter(Objects::nonNull)
        .map(Map::values)
        .flatMap(Collection::stream)
        .toList();
    return getAverageMerByParameterType(scaledAverageManagementExpenseCalculationList);
  }

  public List<AverageManagementExpenseCalculation> getAbsoluteAndForceReportFeeHoldingList(
      final Map<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> resultMap) {
    return Stream.of(
        resultMap.get(FinancialInstrumentType.MUTUAL_FUND_CANADA),
        resultMap.get(FinancialInstrumentType.SEGREGATED_FUND_CANADA),
        resultMap.get(FinancialInstrumentType.ETF_US),
        resultMap.get(FinancialInstrumentType.ETF_CANADA),
        resultMap.get(FinancialInstrumentType.HEDGE_FUND_CANADA),
        resultMap.get(FinancialInstrumentType.MUTUAL_FUND_US))
        .filter(Objects::nonNull)
        .map(Map::values)
        .flatMap(Collection::stream)
        .toList();
  }

  public BigDecimal getAverageMerByParameterType(
      final List<AverageManagementExpenseCalculation> merCalculationDTOList) {
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
      final List<AverageManagementExpenseCalculation> averageManagementExpenseCalculationDTOList) {
    return averageManagementExpenseCalculationDTOList.stream().map(
        AverageManagementExpenseCalculation::getMarketValue).reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  public void calculateMarketValueQualified(
      final AverageManagementExpenseCalculation averageManagementExpenseCalculationDTO) {
    if (Objects.isNull(averageManagementExpenseCalculationDTO.getModifiedFee())) {
      averageManagementExpenseCalculationDTO.setMarketValueQualified(ZERO);
    } else {
      averageManagementExpenseCalculationDTO.setMarketValueQualified(averageManagementExpenseCalculationDTO
          .getMarketValue());
    }
  }

  public void calculatePercentageQualified(
      final AverageManagementExpenseCalculation averageManagementExpenseCalculationDTO,
      final BigDecimal amountOfMarketValues) {
    if (Objects.nonNull(averageManagementExpenseCalculationDTO.getMarketValueQualified())
        && !averageManagementExpenseCalculationDTO.getMarketValueQualified().equals(BigDecimal.ZERO)) {
      averageManagementExpenseCalculationDTO.setPercentageQualified(divide(averageManagementExpenseCalculationDTO
          .getMarketValueQualified(), amountOfMarketValues));
    }
  }

  public BigDecimal calculateAverageManagementExpenseRatio(final BigDecimal currentAverageManagementExpenseRatio,
      final AverageManagementExpenseCalculation merCalculationDTO) {
    if (Objects.nonNull(merCalculationDTO.getModifiedFee()) && Objects.nonNull(merCalculationDTO
        .getPercentageQualified())) {
      return currentAverageManagementExpenseRatio.add(merCalculationDTO.getModifiedFee().multiply(merCalculationDTO
          .getPercentageQualified()));
    }
    return currentAverageManagementExpenseRatio;
  }

  public BigDecimal getForceReportFeeAverageMer(
      final Map<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> averageMerCalculationDtos) {
    if (isMerPresentForHolding(averageMerCalculationDtos.get(FinancialInstrumentType.MUTUAL_FUND_CANADA),
        AverageManagementExpenseCalculation::getManagementExpenseRatio) ||
        isMerPresentForHolding(averageMerCalculationDtos.get(FinancialInstrumentType.SEGREGATED_FUND_CANADA),
            AverageManagementExpenseCalculation::getManagementExpenseRatio) ||
        isMerPresentForHolding(averageMerCalculationDtos.get(FinancialInstrumentType.ETF_CANADA),
            AverageManagementExpenseCalculation::getManagementExpenseRatio) ||
        isMerPresentForHolding(averageMerCalculationDtos.get(FinancialInstrumentType.ETF_US),
            AverageManagementExpenseCalculation::getNetExpenseRatio) ||
        isMerPresentForHolding(averageMerCalculationDtos.get(FinancialInstrumentType.HEDGE_FUND_CANADA),
            AverageManagementExpenseCalculation::getManagementExpenseRatio) ||
        isMerPresentForHolding(averageMerCalculationDtos.get(FinancialInstrumentType.MUTUAL_FUND_US),
            AverageManagementExpenseCalculation::getNetExpenseRatio)) {
      return null;
    }
    final List<AverageManagementExpenseCalculation> scaledAverageManagementExpenseCalculationList = getAbsoluteAndForceReportFeeHoldingList(
        averageMerCalculationDtos);
    return getAverageMerByParameterType(scaledAverageManagementExpenseCalculationList);
  }

  public boolean isMerPresentForHolding(final Map<PortfolioHolding, AverageManagementExpenseCalculation> merList,
      final Function<AverageManagementExpenseCalculation, BigDecimal> function) {
    return Optional.ofNullable(merList).orElse(Map.of()).values().stream().anyMatch(r -> Objects.isNull(function.apply(
        r)));
  }

}
