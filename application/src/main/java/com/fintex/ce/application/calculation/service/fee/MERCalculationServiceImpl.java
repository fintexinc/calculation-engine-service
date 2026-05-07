package com.fintex.ce.application.calculation.service.fee;

import com.fintex.ce.application.calculation.service.DefaultTargetCurrencyConverter;
import com.fintex.ce.application.config.DefaultDataProperties;
import com.fintex.ce.model.domain.calculation.fee.AverageManagementExpenseCalculation;
import com.fintex.ce.model.domain.calculation.fee.FeeData;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.enumeration.FeeAggregationMode;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.fee.AverageMerResult;
import com.fintex.ce.model.dto.command.AverageMerCommand;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.error.Notification;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.fintex.ce.model.domain.enumeration.FeeAggregationMode.FUNDS_ONLY;
import static com.fintex.ce.model.domain.enumeration.FeeAggregationMode.FUNDS_ONLY_STRICT;
import static com.fintex.ce.model.domain.enumeration.FeeAggregationMode.WHOLE_PORTFOLIO;
import static com.fintex.ce.util.FilterUtils.getSpecifiedIfEmpty;

@Service
public class MERCalculationServiceImpl extends AbstractFeeCalculationService<AverageMerResult> {

  private final SecurityDataFetcher<FeeData> feesSecurityDataFetcher;
  private final DefaultDataProperties defaultDataProperties;
  private final FeeResolver feeResolver;

  public MERCalculationServiceImpl(SecurityDataFetcher<FeeData> feesSecurityDataFetcher,
      DefaultDataProperties defaultDataProperties, DefaultTargetCurrencyConverter defaultTargetCurrencyConverter,
      MerFeeResolver feeResolver) {
    super(defaultTargetCurrencyConverter);
    this.feesSecurityDataFetcher = feesSecurityDataFetcher;
    this.defaultDataProperties = defaultDataProperties;
    this.feeResolver = feeResolver;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.MER;
  }

  @Override
  protected Map<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> fetchData(
      AverageMerCommand command) {
    Map<PortfolioHolding, FeeData> rawData = feesSecurityDataFetcher.fetch(
        command.getHoldings(),
        getSpecifiedIfEmpty(command.getDataProviders(), defaultDataProperties.getDataProviders()));
    return groupAndMap(rawData, command.getHoldings());
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
  protected AverageMerResult calculateAverageValue(List<FeeAggregationMode> modes,
      Map<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> calculations) {
    var result = new AverageMerResult();
    if (modes.contains(FUNDS_ONLY)) {
      result.getManagementExpenseRatio().put(FUNDS_ONLY, getFundsOnlyAverage(calculations));
    }
    if (modes.contains(WHOLE_PORTFOLIO)) {
      result.getManagementExpenseRatio().put(WHOLE_PORTFOLIO, getWholePortfolioAverage(calculations));
    }
    if (modes.contains(FUNDS_ONLY_STRICT)) {
      result.getManagementExpenseRatio().put(FUNDS_ONLY_STRICT, getFundsOnlyStrictAverage(calculations));
    }
    return result;
  }

  @Override
  protected void nullOutEmptyFundModes(AverageMerResult response, AverageMerCommand command) {
    nullOutEmptyFundModes(response.getManagementExpenseRatio(), command);
  }
}
