package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.config.DefaultDataProperties;
import com.fintex.ce.domain.dto.command.AverageMerCommand;
import com.fintex.ce.domain.exception.notification.pattern.Notification;
import com.fintex.ce.domain.model.AverageManagementExpenseCalculation;
import com.fintex.ce.domain.model.FeeData;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.enumeration.CalculationMetric;
import com.fintex.ce.domain.model.enumeration.ParameterType;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.result.ManagementFeeResult;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

import static com.fintex.ce.constant.HoldingTypeGroup.FUNDS;
import static com.fintex.ce.domain.model.enumeration.ExceptionCode.ERR_MF_MF_001;
import static com.fintex.ce.domain.model.enumeration.ParameterType.ABSOLUTE;
import static com.fintex.ce.domain.model.enumeration.ParameterType.SCALED;
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
  public Map<FinancialInstrumentType, Map<Holding, AverageManagementExpenseCalculation>> fetchData(
      final AverageMerCommand reqDTO) {
    Map<Holding, FeeData> rawData = feesSecurityDataFetcher.fetch(
        reqDTO.getHoldings(),
        getSpecifiedIfEmpty(reqDTO.getDataProviders(), defaultDataProperties.getDataProviders()));
    return groupAndMap(rawData, reqDTO.getHoldings());
  }

  @Override
  protected AverageManagementExpenseCalculation mapFeeDataToDto(Holding holding, FeeData fees) {
    return AverageManagementExpenseCalculation.builder()
        .marketValue(holding.getValue())
        .holdingType(holding.getHoldingType())
        .actualManagementFee(fees.getManagementFee())
        .build();
  }

  @Override
  public List<Warning> setInitialFeeAndModifiedFeeValues(
      final Map<FinancialInstrumentType, Map<Holding, AverageManagementExpenseCalculation>> groupOfMers) {
    var notification = new Notification();
    List<Warning> warnings = groupOfMers.entrySet().stream()
        .filter(e -> FUNDS.contains(e.getKey()))
        .flatMap(e -> e.getValue().entrySet().stream())
        .map(e -> validateManagementFee(e.getValue(), e.getKey(), notification))
        .flatMap(Optional::stream)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
    notification.ifAnyErrorThrowException();
    return warnings;
  }

  public Optional<List<Warning>> validateManagementFee(
      AverageManagementExpenseCalculation averageManagementExpenseCalculationDTO,
      Holding holding,
      Notification notification) {
    if (Objects.isNull(averageManagementExpenseCalculationDTO.getActualManagementFee())) {
      notification.addError(ERR_MF_MF_001.error(holding, org.springframework.http.HttpStatus.BAD_REQUEST));
    }
    setFeeValues(averageManagementExpenseCalculationDTO, averageManagementExpenseCalculationDTO
        .getActualManagementFee());
    return Optional.empty();
  }

  @Override
  public ManagementFeeResult calculateAverageValue(List<ParameterType> parameterTypes,
      Map<FinancialInstrumentType, Map<Holding, AverageManagementExpenseCalculation>> averageMerCalculationDtos) {
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
