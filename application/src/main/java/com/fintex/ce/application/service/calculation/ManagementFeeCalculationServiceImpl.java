package com.fintex.ce.application.service.calculation;

import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.enumeration.ParameterType;
import com.fintex.ce.domain.model.AverageManagementExpenseCalculationDTO;
import com.fintex.ce.domain.model.ParamHolderDTO;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.port.input.command.AverageMerCommand;
import com.fintex.ce.port.input.result.ManagementFeeResult;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.exception.notification.pattern.Notification;
import com.fintex.ce.port.output.HoldingDataLoader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.fintex.ce.constant.HoldingTypeGroup.FUNDS;
import static com.fintex.ce.domain.enumeration.ExceptionCode.ERR_MF_MF_001;
import static com.fintex.ce.domain.enumeration.ParameterType.ABSOLUTE;
import static com.fintex.ce.domain.enumeration.ParameterType.SCALED;
import static com.fintex.ce.util.FilterUtils.getSpecifiedIfEmpty;

@Service
public class ManagementFeeCalculationServiceImpl
    extends
      AverageManagementExpenseCalculationService<ManagementFeeResult> {

  private final HoldingDataLoader<Map<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>>> managementFeeCachePort;

  public ManagementFeeCalculationServiceImpl(@Qualifier("managementFee") final HoldingDataLoader<Map<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>>> managementFeeCachePort) {
    super();
    this.managementFeeCachePort = managementFeeCachePort;
  }

  @Override
  protected void setNullForScaledAndForcedReportFeeIfHoldingContainsNoFunds(final ManagementFeeResult response,
      final AverageMerCommand reqDTO) {
    setNullForScaledIfHoldingContainsNoFunds(response.getManagementFee(), reqDTO);
  }

  @Override
  public Map<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> loadDataFromCacheStorage(
      final AverageMerCommand reqDTO) {
    return managementFeeCachePort.load(reqDTO.getHoldings(),
        getSpecifiedIfEmpty(reqDTO.getDataProviders(), DataProvider.DEFAULT_PROVIDERS), List.of(),
        new ParamHolderDTO());
  }

  @Override
  public List<Warning> setInitialFeeAndModifiedFeeValues(
      final Map<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> groupOfMers) {
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
      notification.addError(ERR_MF_MF_001.error(holding, HttpStatus.BAD_REQUEST));
    }
    setFeeValues(averageManagementExpenseCalculationDTO, averageManagementExpenseCalculationDTO
        .getActualManagementFee());
    return Optional.empty();
  }

  @Override
  public ManagementFeeResult calculateAverageValue(List<ParameterType> parameterTypes,
      Map<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> averageMerCalculationDtos) {
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
