package com.fintex.ce.application.calculation.service;

import com.fintex.ce.domain.dto.command.AverageMerCommand;
import com.fintex.ce.domain.exception.notification.pattern.Notification;
import com.fintex.ce.domain.model.AverageManagementExpenseCalculation;
import com.fintex.ce.domain.model.FeeData;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.enumeration.CalculationMetric;
import com.fintex.ce.domain.model.enumeration.DataProvider;
import com.fintex.ce.domain.model.enumeration.ParameterType;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.result.AverageMerResult;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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

  private final SecurityDataFetcher<FeeData> feesSecurityDataFetcher;

  public MERCalculationServiceImpl(final SecurityDataFetcher<FeeData> feesSecurityDataFetcher) {
    super();
    this.feesSecurityDataFetcher = feesSecurityDataFetcher;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.MER;
  }

  @Override
  public Map<FinancialInstrumentType, Map<Holding, AverageManagementExpenseCalculation>> fetchData(
      final AverageMerCommand reqDTO) {
    Map<Holding, FeeData> rawData = feesSecurityDataFetcher.fetch(
        reqDTO.getHoldings(),
        getSpecifiedIfEmpty(reqDTO.getDataProviders(), DataProvider.DEFAULT_PROVIDERS));
    return groupAndMap(rawData, reqDTO.getHoldings());
  }

  @Override
  protected AverageManagementExpenseCalculation mapFeeDataToDto(Holding holding, FeeData fees) {
    FinancialInstrumentType type = holding.getHoldingType();
    var builder = AverageManagementExpenseCalculation.builder()
            .marketValue(holding.getValue())
            .holdingType(type);
    if (type == FinancialInstrumentType.ETF_US || type == FinancialInstrumentType.MUTUAL_FUND_US) {
      builder.netExpenseRatio(fees.getNetExpenseRatio())
              .grossExpenseRatio(fees.getGrossExpenseRatio());
    } else {
      builder.managementExpenseRatio(fees.getManagementExpenseRatio())
              .actualManagementFee(fees.getManagementFee());
    }
    return builder.build();
  }

  @Override
  public List<Warning> setInitialFeeAndModifiedFeeValues(
          final Map<FinancialInstrumentType, Map<Holding, AverageManagementExpenseCalculation>> groupOfMers) {
    var notification = new Notification();
    // TODO TMI-369: refactor this logic. Many types are/were simply not handled in the initial impl.
    // Therefore we must investigate:
    // If we should only that the specific children types like MUTUAL_FUND_CANADA or also allow just FUND?
    // What processing logic must happen for each type?
    // This requires more detailed info on calculation logic.
    List<Warning> warnings = groupOfMers.entrySet().stream()
            .flatMap(entry -> {
              FinancialInstrumentType holdingType = entry.getKey();
              if (FinancialInstrumentType.ETF_CANADA.equals(holdingType)
                      || FinancialInstrumentType.MUTUAL_FUND_CANADA.equals(holdingType)
                      || FinancialInstrumentType.SEGREGATED_FUND_CANADA.equals(holdingType)
                      || FinancialInstrumentType.HEDGE_FUND_CANADA.equals(holdingType)) {
                return entry.getValue().entrySet().stream()
                        .map(e -> handleFeeDataForCanadaMutualHedgeFundsAndEtf(e.getValue(), e.getKey(), notification))
                        .flatMap(Optional::stream)
                        .flatMap(Collection::stream);
              } else if (FinancialInstrumentType.ETF_US.equals(holdingType)
                      || FinancialInstrumentType.MUTUAL_FUND_US.equals(holdingType)) {
                return entry.getValue().entrySet().stream()
                        .map(e -> handleFeeDataForUsEtfAndMutualFund(e.getValue(), e.getKey(), notification))
                        .flatMap(Optional::stream);
              }
              return Stream.empty();
            })
            .collect(Collectors.toList());

    notification.ifAnyErrorThrowException();
    return warnings;
  }

  public Optional<List<Warning>> handleFeeDataForCanadaMutualHedgeFundsAndEtf(
          AverageManagementExpenseCalculation averageManagementExpenseCalculationDTO,
          Holding holding,
          Notification notification) {
    if (Objects.isNull(averageManagementExpenseCalculationDTO.getManagementExpenseRatio()) &&
            Objects.isNull(averageManagementExpenseCalculationDTO.getActualManagementFee())) {
      if (FinancialInstrumentType.HEDGE_FUND_CANADA.equals(holding.getHoldingType())) {
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

  public Optional<Warning> handleFeeDataForUsEtfAndMutualFund(
          AverageManagementExpenseCalculation averageManagementExpenseCalculationDTO,
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
          final Map<FinancialInstrumentType, Map<Holding, AverageManagementExpenseCalculation>> averageMerCalculationDtos) {
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
