package com.fintex.ce.application.calculation.service.fee;

import com.fintex.ce.application.calculation.service.HoldingCurrencyConverter;
import com.fintex.ce.model.domain.calculation.fee.AverageManagementExpenseCalculation;
import com.fintex.ce.model.domain.calculation.fee.FeeData;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.enumeration.FeeAggregationMode;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.fee.ManagementFeeResult;
import com.fintex.ce.model.dto.command.AverageMerCommand;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.error.Notification;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static com.fintex.ce.application.constant.HoldingTypeGroup.MER_BEARING_TYPES;
import static com.fintex.ce.application.constant.HoldingTypeGroup.ZERO_MER_TYPES;
import static com.fintex.ce.model.domain.enumeration.FeeAggregationMode.FUNDS_ONLY;
import static com.fintex.ce.model.domain.enumeration.FeeAggregationMode.WHOLE_PORTFOLIO;
import static com.fintex.ce.model.error.ErrorCode.MISSING_MANAGEMENT_FEE;
import static java.math.BigDecimal.ZERO;

@Service
public class ManagementFeeCalculationServiceImpl
    extends
      AbstractFeeCalculationService<ManagementFeeResult> {

  public ManagementFeeCalculationServiceImpl(HoldingCurrencyConverter currencyConverter) {
    super(currencyConverter);
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.MANAGEMENT_FEE;
  }

  @Override
  protected void nullOutEmptyFundModes(ManagementFeeResult response, AverageMerCommand command) {
    nullOutEmptyFundModes(response.getManagementFee(), command);
  }

  @Override
  protected AverageManagementExpenseCalculation mapFeeDataToCalculation(PortfolioHolding holding, FeeData fees) {
    return AverageManagementExpenseCalculation.builder()
        .marketValue(holding.getValue())
        .holdingType(holding.getHoldingType())
        .actualManagementFee(fees.getManagementFee())
        .currency(fees.getCurrency())
        .build();
  }

  /**
   * Sets {@code modifiedFee} per holding type:
   * <ul>
   * <li>MER-bearing: validate and use the source-reported management fee, throwing if missing.</li>
   * <li>Zero-MER (stocks, cash, GIC, fixed income): set {@code modifiedFee = 0} so the holding stays in the
   * {@link FeeAggregationMode#WHOLE_PORTFOLIO} denominator at 0%. Skipping this step leaves {@code modifiedFee = null}
   * and {@link #getWholePortfolioAverage} silently drops the holding, collapsing WHOLE_PORTFOLIO into FUNDS_ONLY.</li>
   * </ul>
   */
  @Override
  protected List<Notification> resolveFees(
      Map<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> groupOfMers) {
    return groupOfMers.entrySet().stream()
        .flatMap(entry -> {
          FinancialInstrumentType type = entry.getKey();
          if (MER_BEARING_TYPES.contains(type)) {
            return entry.getValue().entrySet().stream()
                .flatMap(e -> validateManagementFee(e.getValue(), e.getKey()).stream());
          }
          if (ZERO_MER_TYPES.contains(type)) {
            entry.getValue().values().forEach(c -> c.setModifiedFee(ZERO));
          }
          return Stream.empty();
        })
        .toList();
  }

  private List<Notification> validateManagementFee(AverageManagementExpenseCalculation calc, PortfolioHolding holding) {
    if (Objects.isNull(calc.getActualManagementFee())) {
      throw MISSING_MANAGEMENT_FEE.toExceptionForHolding(holding);
    }
    setFeeValues(calc, calc.getActualManagementFee());
    return List.of();
  }

  @Override
  protected ManagementFeeResult calculateAverageValue(List<FeeAggregationMode> modes,
      Map<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> calculations) {
    var result = new ManagementFeeResult();
    if (modes.contains(FUNDS_ONLY)) {
      result.getManagementFee().put(FUNDS_ONLY, getFundsOnlyAverage(calculations));
    }
    if (modes.contains(WHOLE_PORTFOLIO)) {
      BigDecimal whole = getWholePortfolioAverage(calculations);
      result.getManagementFee().put(WHOLE_PORTFOLIO, whole);
    }
    return result;
  }
}
