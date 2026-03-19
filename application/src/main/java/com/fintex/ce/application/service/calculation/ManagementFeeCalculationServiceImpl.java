package com.fintex.ce.application.service.calculation;

import com.fintex.ce.domain.dto.AverageManagementExpenseCalculationDTO;
import com.fintex.ce.domain.dto.command.AverageMerCommand;
import com.fintex.ce.domain.exception.notification.pattern.Notification;
import com.fintex.ce.domain.model.ManagementFee;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.enumeration.DataProvider;
import com.fintex.ce.domain.model.enumeration.ParameterType;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.result.ManagementFeeResult;
import com.fintex.ce.port.sm.SecurityDataFetcher;
import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;

import static com.fintex.ce.constant.HoldingTypeGroup.FUNDS;
import static com.fintex.ce.domain.model.enumeration.ExceptionCode.ERR_MF_MF_001;
import static com.fintex.ce.domain.model.enumeration.ParameterType.ABSOLUTE;
import static com.fintex.ce.domain.model.enumeration.ParameterType.SCALED;
import static com.fintex.ce.util.FilterUtils.getSpecifiedIfEmpty;

@Service
public class ManagementFeeCalculationServiceImpl
    extends
      AverageManagementExpenseCalculationService<ManagementFeeResult> {

  private final SecurityDataFetcher<ManagementFee> managementFeeSecurityDataFetcher;

  public ManagementFeeCalculationServiceImpl(final SecurityDataFetcher<ManagementFee> managementFeeSecurityDataFetcher) {
    super();
    this.managementFeeSecurityDataFetcher = managementFeeSecurityDataFetcher;
  }

  @Override
  protected void setNullForScaledAndForcedReportFeeIfHoldingContainsNoFunds(final ManagementFeeResult response,
      final AverageMerCommand reqDTO) {
    setNullForScaledIfHoldingContainsNoFunds(response.getManagementFee(), reqDTO);
  }

  @Override
  public Map<FinancialInstrumentType, Map<Holding, AverageManagementExpenseCalculationDTO>> fetchData(
      final AverageMerCommand reqDTO) {
    Map<Holding, ManagementFee> rawData = managementFeeSecurityDataFetcher.fetch(
        reqDTO.getHoldings(),
        getSpecifiedIfEmpty(reqDTO.getDataProviders(), DataProvider.DEFAULT_PROVIDERS));
    return groupAndMap(rawData, reqDTO.getHoldings());
  }

  private Map<FinancialInstrumentType, Map<Holding, AverageManagementExpenseCalculationDTO>> groupAndMap(
      Map<Holding, ManagementFee> rawData, List<? extends Holding> holdings) {
    Map<FinancialInstrumentType, Map<Holding, AverageManagementExpenseCalculationDTO>> result = new EnumMap<>(FinancialInstrumentType.class);

    for (var entry : rawData.entrySet()) {
      Holding holding = entry.getKey();
      ManagementFee fee = entry.getValue();
      var dto = new AverageManagementExpenseCalculationDTO()
          .setMarketValue(holding.getValue())
          .setHoldingType(holding.getHoldingType())
          .setActualManagementFee(fee.getManagementFee());
      result.computeIfAbsent(holding.getHoldingType(), k -> new HashMap<>()).put(holding, dto);
    }

    for (Holding holding : holdings) {
      if (!rawData.containsKey(holding)) {
        var dto = new AverageManagementExpenseCalculationDTO()
            .setMarketValue(holding.getValue())
            .setHoldingType(holding.getHoldingType())
            .setInitialFee(BigDecimal.ZERO)
            .setModifiedFee(BigDecimal.ZERO);
        result.computeIfAbsent(holding.getHoldingType(), k -> new HashMap<>()).put(holding, dto);
      }
    }

    return result;
  }

  @Override
  public List<Warning> setInitialFeeAndModifiedFeeValues(
      final Map<FinancialInstrumentType, Map<Holding, AverageManagementExpenseCalculationDTO>> groupOfMers) {
    List<Warning> warnings = new ArrayList<>();
    var notification = new Notification();
    groupOfMers.forEach((holdingType, managementFee) -> {
      if (FUNDS.contains(holdingType)) {
        managementFee.forEach((key, value) -> validateManagementFee(value, key, notification).ifPresent(
            warnings::addAll));
      }
    });
    notification.ifAnyErrorThrowException();
    return warnings;
  }

  public Optional<List<Warning>> validateManagementFee(
      AverageManagementExpenseCalculationDTO averageManagementExpenseCalculationDTO,
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
      Map<FinancialInstrumentType, Map<Holding, AverageManagementExpenseCalculationDTO>> averageMerCalculationDtos) {
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
