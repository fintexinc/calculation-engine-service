package com.fintex.ce.application.calculation.service.period.core;

import com.fintex.ce.application.returns.MonthlyReturnsContext;
import com.fintex.ce.application.returns.PerformancePeriodCalculator;
import com.fintex.ce.application.returns.PortfolioMonthlyReturnsContextProvider;
import com.fintex.ce.application.returns.ReturnsSnapshot;
import com.fintex.ce.application.returns.WeightedAverageComponent;
import com.fintex.ce.application.returns.pipeline.CpedParams;
import com.fintex.ce.application.returns.pipeline.PortfolioValidateCutAndFxPipeline;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.calculation.ReturnsBasedCalculationService;
import com.fintex.ce.model.domain.calculation.returns.HoldingMonthlyReturns;
import com.fintex.ce.model.domain.calculation.returns.PortfolioBenchmarkReturns;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.BaseCalculationResult;
import com.fintex.ce.model.domain.security.SecurityData;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.wm.commons.domain.enumeration.CompositeSecurityAttribute;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Base class for services whose pipeline contract is {@link PortfolioValidateCutAndFxPipeline} — validate + CPED cut +
 * FX conversion, without the per-holding PSD trim. The post-pipeline {@link ReturnsSnapshot} is returned to the caller
 * as-is; subclasses (e.g. correlation) consume the per-holding map directly and/or fold it into a weighted average via
 * {@link #weightedAverageAfterPsdTrim(ReturnsSnapshot, ReturnFactorScale)}.
 */
public abstract class ValidateCutAndFxAbstractService<C extends PeriodCommand, R extends BaseCalculationResult>
    implements
      ReturnsBasedCalculationService<C, R> {

  protected final PortfolioMonthlyReturnsContextProvider portfolioMonthlyReturnsContextProvider;
  protected final PortfolioValidateCutAndFxPipeline portfolioValidateCutAndFx;
  protected final WeightedAverageComponent weightedAverageComponent;

  protected ValidateCutAndFxAbstractService(
      PortfolioMonthlyReturnsContextProvider portfolioMonthlyReturnsContextProvider,
      PortfolioValidateCutAndFxPipeline portfolioValidateCutAndFx,
      WeightedAverageComponent weightedAverageComponent) {
    this.portfolioMonthlyReturnsContextProvider = portfolioMonthlyReturnsContextProvider;
    this.portfolioValidateCutAndFx = portfolioValidateCutAndFx;
    this.weightedAverageComponent = weightedAverageComponent;
  }

  @Override
  public List<CompositeSecurityAttribute> requiredAttributes() {
    return List.of(CompositeSecurityAttribute.MONTHLY_RETURNS);
  }

  @Override
  public PortfolioBenchmarkReturns prepareData(SecurityData securityData) {
    return PortfolioBenchmarkReturns.from(securityData);
  }

  protected ReturnsSnapshot<HoldingMonthlyReturns> runValidateCutAndFx(C command,
      PortfolioBenchmarkReturns returnsData) {
    MonthlyReturnsContext<HoldingMonthlyReturns> portfolioContext = portfolioMonthlyReturnsContextProvider.get(
        command.getHoldings(), command.getCurrency(), returnsData.portfolioReturns());
    return portfolioValidateCutAndFx.run(portfolioContext, new CpedParams(command.getCustomPed()));
  }

  /**
   * Per-holding PSD trim followed by weighted-average collapse. The PSD here is the snapshot's own
   * {@code performanceStartDate}, not the command's CPSD — this is what differentiates
   * {@link PortfolioValidateCutAndFxPipeline} consumers from CPSD-trimmed ones.
   */
  protected NavigableMap<LocalDate, BigDecimal> weightedAverageAfterPsdTrim(
      ReturnsSnapshot<HoldingMonthlyReturns> snapshot, ReturnFactorScale scale) {
    Map<PortfolioHolding, TreeMap<LocalDate, BigDecimal>> trimmed = PerformancePeriodCalculator.trimByStartDate(
        snapshot.returnsMap(), snapshot.performanceStartDate());
    return weightedAverageComponent.calculateWeightedAverage(trimmed, scale);
  }
}
