package com.fintex.ce.application.service.calculation;

import com.fintex.ce.domain.dto.AverageManagementExpenseCalculationDTO;
import com.fintex.ce.domain.dto.command.AverageMerCommand;
import com.fintex.ce.domain.exception.notification.pattern.Notification;
import com.fintex.ce.domain.model.AverageMer;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.enumeration.DataProvider;
import com.fintex.ce.domain.model.enumeration.HoldingType;
import com.fintex.ce.domain.model.enumeration.ParameterType;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.result.AverageMerResult;
import com.fintex.ce.port.sm.SecurityDataFetcher;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;
import static com.fintex.ce.domain.model.enumeration.ExceptionCode.ERR_MER_MERMF_001;
import static com.fintex.ce.domain.model.enumeration.ExceptionCode.ERR_MER_NERGER_001;
import static com.fintex.ce.domain.model.enumeration.ExceptionCode.WRN_MER_AMF_001;
import static com.fintex.ce.domain.model.enumeration.ExceptionCode.WRN_MER_GER_001;
import static com.fintex.ce.domain.model.enumeration.ExceptionCode.WRN_MER_MER_001;
import static com.fintex.ce.domain.model.enumeration.ExceptionCode.WRN_MER_NER_001;
import static com.fintex.ce.domain.model.enumeration.ParameterType.ABSOLUTE;
import static com.fintex.ce.domain.model.enumeration.ParameterType.FORCE_REPORT_FEE;
import static com.fintex.ce.domain.model.enumeration.ParameterType.SCALED;
import static com.fintex.ce.util.FilterUtils.getSpecifiedIfEmpty;

@Service
public class MERCalculationServiceImpl extends AverageManagementExpenseCalculationService<AverageMerResult> {

  private final SecurityDataFetcher<AverageMer> averageMerSecurityDataFetcher;

  public MERCalculationServiceImpl(final SecurityDataFetcher<AverageMer> averageMerSecurityDataFetcher) {
    super();
    this.averageMerSecurityDataFetcher = averageMerSecurityDataFetcher;
  }

  @Override
  public Map<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> fetchData(
      final AverageMerCommand reqDTO) {
    Map<Holding, AverageMer> rawData = averageMerSecurityDataFetcher.fetch(
        reqDTO.getHoldings(),
        getSpecifiedIfEmpty(reqDTO.getDataProviders(), DataProvider.DEFAULT_PROVIDERS));
    return groupAndMap(rawData, reqDTO.getHoldings());
  }

  private Map<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> groupAndMap(
      Map<Holding, AverageMer> rawData, List<? extends Holding> holdings) {
    Map<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> result = new EnumMap<>(HoldingType.class);

    for (var entry : rawData.entrySet()) {
      Holding holding = entry.getKey();
      AverageMer mer = entry.getValue();
      AverageManagementExpenseCalculationDTO dto = mapMerToDto(holding, mer);
      result.computeIfAbsent(holding.getType(), k -> new HashMap<>()).put(holding, dto);
    }

    for (Holding holding : holdings) {
      if (!rawData.containsKey(holding)) {
        AverageManagementExpenseCalculationDTO dto = new AverageManagementExpenseCalculationDTO()
            .setMarketValue(holding.getValue())
            .setHoldingType(holding.getType())
            .setInitialFee(BigDecimal.ZERO)
            .setModifiedFee(BigDecimal.ZERO);
        result.computeIfAbsent(holding.getType(), k -> new HashMap<>()).put(holding, dto);
      }
    }

    return result;
  }

  private AverageManagementExpenseCalculationDTO mapMerToDto(Holding holding, AverageMer mer) {
    var dto = new AverageManagementExpenseCalculationDTO()
        .setMarketValue(holding.getValue())
        .setHoldingType(holding.getType());
    HoldingType type = holding.getType();
    if (type == HoldingType.US_ETF || type == HoldingType.US_MUTUAL_FUNDS) {
      dto.setNetExpenseRatio(mer.getNetExpenseRatio());
      dto.setGrossExpenseRatio(mer.getGrossExpenseRatio());
    } else {
      dto.setManagementExpenseRatio(mer.getMer());
      dto.setActualManagementFee(mer.getActualManagementFee());
    }
    return dto;
  }

  @Override
  public List<Warning> setInitialFeeAndModifiedFeeValues(
      final Map<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> groupOfMers) {
    List<Warning> warnings = new ArrayList<>();
    var notification = new Notification();
    groupOfMers.forEach((holdingType, mer) -> {
      if (HoldingType.CANADA_ETF.equals(holdingType) || HoldingType.CANADA_MUTUAL_FUNDS.equals(holdingType)
          || HoldingType.SEGREGATED_FUND_CANADA.equals(holdingType)
          || HoldingType.CANADA_HEDGE_FUNDS.equals(holdingType)) {
        mer.forEach((key, value) -> handleFeesForCanadaMutualHedgeFundsAndEtf(value, key, notification).ifPresent(
            warnings::addAll));
      } else if (HoldingType.US_ETF.equals(holdingType) || HoldingType.US_MUTUAL_FUNDS.equals(holdingType)) {
        mer.forEach((key, value) -> handleFeesForUsEtfAndMutualFund(value, key, notification).ifPresent(warnings::add));
      }
    });

    notification.ifAnyErrorThrowException();
    return warnings;
  }

  public Optional<List<Warning>> handleFeesForCanadaMutualHedgeFundsAndEtf(
      AverageManagementExpenseCalculationDTO averageManagementExpenseCalculationDTO,
      Holding holding,
      Notification notification) {
    if (Objects.isNull(averageManagementExpenseCalculationDTO.getManagementExpenseRatio()) &&
        Objects.isNull(averageManagementExpenseCalculationDTO.getActualManagementFee())) {
      if (HoldingType.CANADA_HEDGE_FUNDS.equals(holding.getType())) {
        setFeeValues(averageManagementExpenseCalculationDTO, averageManagementExpenseCalculationDTO
            .getManagementExpenseRatio());
        return Optional.of(List.of(WRN_MER_MER_001.warning(holding), WRN_MER_AMF_001.warning(holding)));
      } else {
        notification.addError(ERR_MER_MERMF_001.error(holding));
      }
    } else if (Objects.isNull(averageManagementExpenseCalculationDTO.getManagementExpenseRatio())) {
      setFeeValues(averageManagementExpenseCalculationDTO, averageManagementExpenseCalculationDTO
          .getActualManagementFee());
      return Optional.of(List.of(WRN_MER_MER_001.warning(holding)));
    } else if (Objects.isNull(averageManagementExpenseCalculationDTO.getActualManagementFee())) {
      setFeeValues(averageManagementExpenseCalculationDTO, averageManagementExpenseCalculationDTO
          .getManagementExpenseRatio());
      return Optional.of(List.of(WRN_MER_AMF_001.warning(holding)));
    }
    setFeeValues(averageManagementExpenseCalculationDTO, averageManagementExpenseCalculationDTO
        .getManagementExpenseRatio());
    return Optional.empty();
  }

  public Optional<Warning> handleFeesForUsEtfAndMutualFund(
      AverageManagementExpenseCalculationDTO averageManagementExpenseCalculationDTO,
      Holding holding,
      Notification notification) {
    if (Objects.isNull(averageManagementExpenseCalculationDTO.getNetExpenseRatio()) &&
        Objects.isNull(averageManagementExpenseCalculationDTO.getGrossExpenseRatio())) {
      notification.addError(ERR_MER_NERGER_001.error(holding));
    } else if (Objects.isNull(averageManagementExpenseCalculationDTO.getNetExpenseRatio())) {
      setFeeValues(averageManagementExpenseCalculationDTO, averageManagementExpenseCalculationDTO
          .getGrossExpenseRatio());
      return Optional.of(WRN_MER_NER_001.warning(holding));
    } else if (Objects.isNull(averageManagementExpenseCalculationDTO.getGrossExpenseRatio())) {
      setFeeValues(averageManagementExpenseCalculationDTO, averageManagementExpenseCalculationDTO.getNetExpenseRatio());
      return Optional.of(WRN_MER_GER_001.warning(holding));
    }
    setFeeValues(averageManagementExpenseCalculationDTO, averageManagementExpenseCalculationDTO.getNetExpenseRatio());
    return Optional.empty();
  }

  @Override
  public AverageMerResult calculateAverageValue(final List<ParameterType> parameterTypes,
      final Map<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> averageMerCalculationDtos) {
    final var resDTO = new AverageMerResult();
    if (parameterTypes.contains(SCALED)) {
      final BigDecimal scaledAverageMer = getScaledAverageMer(averageMerCalculationDtos);
      resDTO.getManagementExpenseRatio().put(SCALED, scaledAverageMer);
    }
    if (parameterTypes.contains(ABSOLUTE)) {
      final BigDecimal absoluteAverageMer = getAbsoluteAverageMer(averageMerCalculationDtos);
      resDTO.getManagementExpenseRatio().put(ABSOLUTE, absoluteAverageMer);
    }
    if (parameterTypes.contains(FORCE_REPORT_FEE)) {
      final BigDecimal forceReportFeeAverageMer = getForceReportFeeAverageMer(averageMerCalculationDtos);
      resDTO.getManagementExpenseRatio().put(FORCE_REPORT_FEE, forceReportFeeAverageMer);
    }
    return resDTO;
  }

  @Override
  public void setNullForScaledAndForcedReportFeeIfHoldingContainsNoFunds(final AverageMerResult response,
      final AverageMerCommand reqDTO) {
    setNullForScaledAndForcedReportFeeIfHoldingContainsNoFunds(response.getManagementExpenseRatio(), reqDTO);
  }
}
