package ca.tangerine.pce.application.calculation.service.period.core;

import ca.tangerine.pce.application.returns.MonthlyReturnsContext;
import ca.tangerine.pce.application.returns.PortfolioMonthlyReturnsContextProvider;
import ca.tangerine.pce.application.returns.WeightedAverageResult;
import ca.tangerine.pce.application.returns.pipeline.CpedScaleParams;
import ca.tangerine.pce.application.returns.pipeline.PortfolioWeightedAverageWithCpedPipeline;
import ca.tangerine.pce.application.util.ReturnFactorScale;
import ca.tangerine.pce.calculation.PeriodCalculationService;
import ca.tangerine.pce.model.domain.calculation.input.PeriodCalculationInput;
import ca.tangerine.pce.model.domain.calculation.returns.HoldingMonthlyReturns;
import ca.tangerine.pce.model.domain.calculation.returns.PortfolioBenchmarkReturns;
import ca.tangerine.pce.model.domain.result.PeriodResult;
import ca.tangerine.pce.model.domain.security.SecurityData;
import ca.tangerine.pce.model.dto.command.PeriodCommand;
import ca.tangerine.wm.commons.domain.enumeration.CompositeSecurityAttribute;
import ca.tangerine.wm.commons.domain.enumeration.TimePeriod;

import java.util.List;
import java.util.Set;

/**
 * Base class for services whose pipeline contract is the portfolio-side weighted-average with CPED only (no per-holding
 * CPSD trim). Mirrors {@link PortfolioWeightedAverageWithCpedPipeline} on the service side.
 *
 * @param <C>
 *          command type — must carry portfolio holdings, target currency, and CPED
 * @param <R>
 *          result type produced by the service
 */
public abstract class WeightedAverageWithCpedAbstractService<C extends PeriodCommand, R extends PeriodResult>
    implements
      PeriodCalculationService<C, R> {
  protected final Set<TimePeriod> defaultPeriods;
  protected final PortfolioMonthlyReturnsContextProvider portfolioMonthlyReturnsContextProvider;
  protected final PortfolioWeightedAverageWithCpedPipeline portfolioWeightedAverageWithCped;

  protected WeightedAverageWithCpedAbstractService(
      PortfolioMonthlyReturnsContextProvider portfolioMonthlyReturnsContextProvider,
      PortfolioWeightedAverageWithCpedPipeline portfolioWeightedAverageWithCped,
      Set<TimePeriod> defaultPeriods) {
    this.portfolioMonthlyReturnsContextProvider = portfolioMonthlyReturnsContextProvider;
    this.portfolioWeightedAverageWithCped = portfolioWeightedAverageWithCped;
    this.defaultPeriods = defaultPeriods;
  }

  @Override
  public List<CompositeSecurityAttribute> requiredAttributes() {
    return List.of(CompositeSecurityAttribute.MONTHLY_RETURNS);
  }

  @Override
  public PortfolioBenchmarkReturns prepareData(SecurityData securityData) {
    return PortfolioBenchmarkReturns.from(securityData);
  }

  public WeightedAverageResult<HoldingMonthlyReturns> buildWeightedAverageResult(C command,
      ReturnFactorScale scale, PortfolioBenchmarkReturns returnsData) {
    MonthlyReturnsContext<HoldingMonthlyReturns> portfolioContext = portfolioMonthlyReturnsContextProvider.get(
        command.getHoldings(), command.getCurrency(), returnsData.portfolio());
    return portfolioWeightedAverageWithCped.run(portfolioContext, new CpedScaleParams(command.getCustomPed(), scale));
  }

  public PeriodCalculationInput buildPeriodCalculationInput(C command, ReturnFactorScale returnFactorScale,
      PortfolioBenchmarkReturns returnsData) {
    WeightedAverageResult<HoldingMonthlyReturns> result = buildWeightedAverageResult(command, returnFactorScale,
        returnsData);
    return new PeriodCalculationInput(command.getCustomIntervalPsd(), result.weightedAverage(),
        result.getErrorsAsWarnings());
  }
}
