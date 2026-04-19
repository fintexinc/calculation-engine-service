package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.config.DefaultDataProperties;
import com.fintex.ce.model.domain.calculation.fee.AverageManagementExpenseCalculation;
import com.fintex.ce.model.domain.calculation.fee.FeeData;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.enumeration.ParameterType;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.fee.AverageMerResult;
import com.fintex.ce.model.dto.command.AverageMerCommand;
import com.fintex.ce.model.error.PceExceptionCollector;
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
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;

import static com.fintex.ce.model.domain.enumeration.ParameterType.ABSOLUTE;
import static com.fintex.ce.model.domain.enumeration.ParameterType.FORCE_REPORT_FEE;
import static com.fintex.ce.model.domain.enumeration.ParameterType.SCALED;
import static com.fintex.ce.model.error.ErrorCode.MISSING_ACTUAL_MANAGEMENT_FEE;
import static com.fintex.ce.model.error.ErrorCode.MISSING_GROSS_EXPENSE_RATIO;
import static com.fintex.ce.model.error.ErrorCode.MISSING_MANAGEMENT_EXPENSE_RATIO;
import static com.fintex.ce.model.error.ErrorCode.MISSING_MER_AND_MANAGEMENT_FEE;
import static com.fintex.ce.model.error.ErrorCode.MISSING_NER_AND_GER;
import static com.fintex.ce.model.error.ErrorCode.MISSING_NET_EXPENSE_RATIO;
import static com.fintex.ce.util.FilterUtils.getSpecifiedIfEmpty;

@Service
@RequiredArgsConstructor
public class MERCalculationServiceImpl extends AverageManagementExpenseCalculationService<AverageMerResult> {

  private final SecurityDataFetcher<FeeData> feesSecurityDataFetcher;
  private final DefaultDataProperties defaultDataProperties;

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.MER;
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
      final Map<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> groupOfMers) {
    // TODO TMI-369: refactor this logic. Many types are/were simply not handled in the initial impl.
    // Therefore we must investigate:
    // If we should only that the specific children types like MUTUAL_FUND_CANADA or also allow just FUND?
    // What processing logic must happen for each type?
    // This requires more detailed info on calculation logic.
    PceExceptionCollector collector = new PceExceptionCollector();
    List<Warning> warnings = groupOfMers.entrySet().stream()
        .flatMap(entry -> {
          FinancialInstrumentType holdingType = entry.getKey();
          if (FinancialInstrumentType.ETF_CANADA.equals(holdingType)
              || FinancialInstrumentType.MUTUAL_FUND_CANADA.equals(holdingType)
              || FinancialInstrumentType.SEGREGATED_FUND_CANADA.equals(holdingType)
              || FinancialInstrumentType.HEDGE_FUND_CANADA.equals(holdingType)) {
            return entry.getValue().entrySet().stream()
                .map(e -> collector.tryCatch(() -> handleFeeDataForCanadaMutualHedgeFundsAndEtf(e.getValue(), e
                    .getKey())))
                .filter(Objects::nonNull)
                .flatMap(Optional::stream)
                .flatMap(Collection::stream);
          } else
            if (FinancialInstrumentType.ETF_US.equals(holdingType)
                || FinancialInstrumentType.MUTUAL_FUND_US.equals(holdingType)) {
                  return entry.getValue().entrySet().stream()
                      .map(e -> collector.tryCatch(() -> handleFeeDataForUsEtfAndMutualFund(e.getValue(), e.getKey())))
                      .filter(Objects::nonNull)
                      .flatMap(Optional::stream);
                }
          return Stream.empty();
        })
        .toList();
    collector.throwIfAny();
    return warnings;
  }

  public Optional<List<Warning>> handleFeeDataForCanadaMutualHedgeFundsAndEtf(
      AverageManagementExpenseCalculation averageManagementExpenseCalculationDTO,
      PortfolioHolding holding) {
    if (Objects.isNull(averageManagementExpenseCalculationDTO.getManagementExpenseRatio()) &&
        Objects.isNull(averageManagementExpenseCalculationDTO.getActualManagementFee())) {
      if (FinancialInstrumentType.HEDGE_FUND_CANADA.equals(holding.getHoldingType())) {
        setFeeValues(averageManagementExpenseCalculationDTO, averageManagementExpenseCalculationDTO
            .getManagementExpenseRatio());
        return Optional.of(List.of(MISSING_MANAGEMENT_EXPENSE_RATIO.warning(holding),
            MISSING_ACTUAL_MANAGEMENT_FEE.warning(holding)));
      }
      throw MISSING_MER_AND_MANAGEMENT_FEE.toExceptionForHolding(holding);
    } else if (Objects.isNull(averageManagementExpenseCalculationDTO.getManagementExpenseRatio())) {
      setFeeValues(averageManagementExpenseCalculationDTO, averageManagementExpenseCalculationDTO
          .getActualManagementFee());
      return Optional.of(List.of(MISSING_MANAGEMENT_EXPENSE_RATIO.warning(holding)));
    } else if (Objects.isNull(averageManagementExpenseCalculationDTO.getActualManagementFee())) {
      setFeeValues(averageManagementExpenseCalculationDTO, averageManagementExpenseCalculationDTO
          .getManagementExpenseRatio());
      return Optional.of(List.of(MISSING_ACTUAL_MANAGEMENT_FEE.warning(holding)));
    }
    setFeeValues(averageManagementExpenseCalculationDTO, averageManagementExpenseCalculationDTO
        .getManagementExpenseRatio());
    return Optional.empty();
  }

  public Optional<Warning> handleFeeDataForUsEtfAndMutualFund(
      AverageManagementExpenseCalculation averageManagementExpenseCalculationDTO,
      PortfolioHolding holding) {
    if (Objects.isNull(averageManagementExpenseCalculationDTO.getNetExpenseRatio()) &&
        Objects.isNull(averageManagementExpenseCalculationDTO.getGrossExpenseRatio())) {
      throw MISSING_NER_AND_GER.toExceptionForHolding(holding);
    } else if (Objects.isNull(averageManagementExpenseCalculationDTO.getNetExpenseRatio())) {
      setFeeValues(averageManagementExpenseCalculationDTO, averageManagementExpenseCalculationDTO
          .getGrossExpenseRatio());
      return Optional.of(MISSING_NET_EXPENSE_RATIO.warning(holding));
    } else if (Objects.isNull(averageManagementExpenseCalculationDTO.getGrossExpenseRatio())) {
      setFeeValues(averageManagementExpenseCalculationDTO, averageManagementExpenseCalculationDTO.getNetExpenseRatio());
      return Optional.of(MISSING_GROSS_EXPENSE_RATIO.warning(holding));
    }
    setFeeValues(averageManagementExpenseCalculationDTO, averageManagementExpenseCalculationDTO.getNetExpenseRatio());
    return Optional.empty();
  }

  @Override
  public AverageMerResult calculateAverageValue(final List<ParameterType> parameterTypes,
      final Map<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> averageMerCalculationDtos) {
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
