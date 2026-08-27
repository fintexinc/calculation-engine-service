package ca.tangerine.pce.application.calculation.service.fee;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static ca.tangerine.pce.model.domain.enumeration.FeeAggregationMode.FUNDS_ONLY;
import static ca.tangerine.pce.model.domain.enumeration.FeeAggregationMode.FUNDS_ONLY_STRICT;
import static ca.tangerine.pce.model.domain.enumeration.FeeAggregationMode.WHOLE_PORTFOLIO;

import ca.tangerine.pce.application.calculation.service.HoldingCurrencyConverter;
import ca.tangerine.pce.model.domain.calculation.fee.AverageManagementExpenseCalculation;
import ca.tangerine.pce.model.domain.calculation.fee.FeeData;
import ca.tangerine.pce.model.domain.enumeration.CalculationMetric;
import ca.tangerine.pce.model.domain.enumeration.FeeAggregationMode;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.pce.model.domain.result.fee.AverageMerResult;
import ca.tangerine.pce.model.dto.command.AverageMerCommand;
import ca.tangerine.wm.commons.domain.enumeration.FinancialInstrumentType;
import ca.tangerine.wm.commons.error.Notification;

@Service
public class MERCalculationServiceImpl extends AbstractFeeCalculationService<AverageMerResult> {

  private final FeeResolver feeResolver;

  public MERCalculationServiceImpl(HoldingCurrencyConverter currencyConverter,
      FeeResolver feeResolver) {
    super(currencyConverter);
    this.feeResolver = feeResolver;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.MER;
  }

  @Override
  protected AverageManagementExpenseCalculation mapFeeDataToCalculation(PortfolioHolding holding, FeeData fees) {
    return feeResolver.mapFeeDataToCalculation(holding, fees);
  }

  @Override
  protected List<Notification> resolveFees(
      Map<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> groupOfMers) {
    return feeResolver.resolveFees(groupOfMers);
  }

  @Override
  protected AverageMerResult calculateAverageValue(AverageMerCommand command, List<FeeAggregationMode> modes,
      Map<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> calculations) {
    var result = new AverageMerResult();
    if (modes.contains(FUNDS_ONLY)) {
      result.getManagementExpenseRatio().put(FUNDS_ONLY, getFundsOnlyAverage(calculations));
      result.getBaseValue().put(FUNDS_ONLY, getFundsOnlyBase(calculations));
    }
    if (modes.contains(WHOLE_PORTFOLIO)) {
      result.getManagementExpenseRatio().put(WHOLE_PORTFOLIO, getWholePortfolioAverage(calculations));
      result.getBaseValue().put(WHOLE_PORTFOLIO, getWholePortfolioBase(calculations));
    }
    if (modes.contains(FUNDS_ONLY_STRICT)) {
      result.getManagementExpenseRatio().put(FUNDS_ONLY_STRICT, getFundsOnlyStrictAverage(calculations));
      result.getBaseValue().put(FUNDS_ONLY_STRICT, getFundsOnlyStrictBase(calculations));
    }
    return result;
  }

  @Override
  protected void nullOutEmptyFundModes(AverageMerResult response, AverageMerCommand command) {
    nullOutEmptyFundModes(response.getManagementExpenseRatio(), command);
  }
}
