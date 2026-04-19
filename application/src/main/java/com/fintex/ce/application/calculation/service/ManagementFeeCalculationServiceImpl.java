package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.config.DefaultDataProperties;
import com.fintex.ce.model.domain.calculation.fee.AverageManagementExpenseCalculation;
import com.fintex.ce.model.domain.calculation.fee.FeeData;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.enumeration.ParameterType;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.fee.ManagementFeeResult;
import com.fintex.ce.model.dto.command.AverageMerCommand;
import com.fintex.ce.model.error.Warning;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

import static com.fintex.ce.constant.HoldingTypeGroup.FUNDS;
import static com.fintex.ce.model.domain.enumeration.ParameterType.ABSOLUTE;
import static com.fintex.ce.model.domain.enumeration.ParameterType.SCALED;
import static com.fintex.ce.model.error.ErrorCode.MISSING_MANAGEMENT_FEE;
import static com.fintex.ce.util.FilterUtils.getSpecifiedIfEmpty;

@Service
@RequiredArgsConstructor
public class ManagementFeeCalculationServiceImpl
    extends
      AverageManagementExpenseCalculationService<ManagementFeeResult> {

  private final SecurityDataFetcher<FeeData> feesSecurityDataFetcher;
  private final DefaultDataProperties defaultDataProperties;

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.MANAGEMENT_FEE;
  }

  @Override
  protected void setNullForScaledAndForcedReportFeeIfHoldingContainsNoFunds(final ManagementFeeResult response,
      final AverageMerCommand reqDTO) {
    setNullForScaledIfHoldingContainsNoFunds(response.getManagementFee(), reqDTO);
  }

  @Override
  public Map<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> fetchData(
      final AverageMerCommand reqDTO) {
    Map<PortfolioHolding, FeeData> rawData = feesSecurityDataFetcher.fetch(
        reqDTO.getHoldings(),
        getSpecifiedIfEmpty(reqDTO.getDataProviders(), defaultDataProperties.getDataProviders()));
    return groupAndMap(rawData, reqDTO.getHoldings());
  }

  @Override
  protected AverageManagementExpenseCalculation mapFeeDataToDto(PortfolioHolding holding, FeeData fees) {
    return AverageManagementExpenseCalculation.builder()
        .marketValue(holding.getValue())
        .holdingType(holding.getHoldingType())
        .actualManagementFee(fees.getManagementFee())
        .build();
  }

  @Override
  public List<Warning> setInitialFeeAndModifiedFeeValues(
      final Map<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> groupOfMers) {
    return groupOfMers.entrySet().stream()
        .filter(e -> FUNDS.contains(e.getKey()))
        .flatMap(e -> e.getValue().entrySet().stream())
        .map(e -> validateManagementFee(e.getValue(), e.getKey()))
        .flatMap(Optional::stream)
        .flatMap(Collection::stream)
        .toList();
  }

  public Optional<List<Warning>> validateManagementFee(
      AverageManagementExpenseCalculation averageManagementExpenseCalculationDTO,
      PortfolioHolding holding) {
    if (Objects.isNull(averageManagementExpenseCalculationDTO.getActualManagementFee())) {
      throw MISSING_MANAGEMENT_FEE.toExceptionForHolding(holding);
    }
    setFeeValues(averageManagementExpenseCalculationDTO, averageManagementExpenseCalculationDTO
        .getActualManagementFee());
    return Optional.empty();
  }

  @Override
  public ManagementFeeResult calculateAverageValue(List<ParameterType> parameterTypes,
      Map<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> averageMerCalculationDtos) {
    final var resDTO = new ManagementFeeResult();
    if (parameterTypes.contains(SCALED)) {
      final BigDecimal scaledAverageMer = getScaledAverageMer(averageMerCalculationDtos);
      resDTO.getManagementFee().put(SCALED, scaledAverageMer);
    }
    if (parameterTypes.contains(ABSOLUTE)) {
      final BigDecimal absoluteAverageMer = getAbsoluteAverageMer(averageMerCalculationDtos);
      resDTO.getManagementFee().put(ABSOLUTE, absoluteAverageMer);
    }
    return resDTO;
  }
}
