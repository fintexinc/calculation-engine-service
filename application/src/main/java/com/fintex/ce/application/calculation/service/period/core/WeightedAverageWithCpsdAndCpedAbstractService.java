package com.fintex.ce.application.calculation.service.period.core;

import com.fintex.ce.application.returns.MonthlyReturnsContext;
import com.fintex.ce.application.returns.PortfolioMonthlyReturnsContextProvider;
import com.fintex.ce.application.returns.WeightedAverageResult;
import com.fintex.ce.application.returns.pipeline.CpsdCpedScaleParams;
import com.fintex.ce.application.returns.pipeline.PortfolioWeightedAverageWithCpsdAndCpedPipeline;
import com.fintex.ce.application.util.ReturnFactorScale;
import com.fintex.ce.calculation.ReturnsBasedCalculationService;
import com.fintex.ce.model.domain.calculation.input.PeriodCalculationInput;
import com.fintex.ce.model.domain.calculation.returns.HoldingMonthlyReturns;
import com.fintex.ce.model.domain.calculation.returns.PortfolioBenchmarkReturns;
import com.fintex.ce.model.domain.result.BaseCalculationResult;
import com.fintex.ce.model.domain.security.SecurityData;
import com.fintex.ce.model.dto.command.PortfolioBenchmarkCommand;
import com.fintex.ce.model.dto.command.contract.CustomPedProvider;
import com.fintex.ce.model.dto.command.contract.CustomPsdProvider;
import com.fintex.wm.commons.domain.enumeration.CompositeSecurityAttribute;

import java.util.List;

/**
 * Base class for services whose pipeline contract is the portfolio-side weighted-average with both CPSD and CPED
 * applied. Mirrors {@link PortfolioWeightedAverageWithCpsdAndCpedPipeline} on the service side: every subclass owns
 * this pipeline implicitly and shapes the result into its metric-specific input.
 *
 * @param <C>
 *          command type — must carry portfolio holdings, target currency, CPSD and CPED
 * @param <R>
 *          result type produced by the service
 */
public abstract class WeightedAverageWithCpsdAndCpedAbstractService<C extends PortfolioBenchmarkCommand & CustomPsdProvider & CustomPedProvider, R extends BaseCalculationResult>
    implements
      ReturnsBasedCalculationService<C, R> {

  protected final PortfolioMonthlyReturnsContextProvider portfolioMonthlyReturnsContextProvider;
  protected final PortfolioWeightedAverageWithCpsdAndCpedPipeline portfolioWeightedAverageWithCpsdAndCped;

  protected WeightedAverageWithCpsdAndCpedAbstractService(
      PortfolioMonthlyReturnsContextProvider portfolioMonthlyReturnsContextProvider,
      PortfolioWeightedAverageWithCpsdAndCpedPipeline portfolioWeightedAverageWithCpsdAndCped) {
    this.portfolioMonthlyReturnsContextProvider = portfolioMonthlyReturnsContextProvider;
    this.portfolioWeightedAverageWithCpsdAndCped = portfolioWeightedAverageWithCpsdAndCped;
  }

  @Override
  public List<CompositeSecurityAttribute> requiredAttributes() {
    return List.of(CompositeSecurityAttribute.MONTHLY_RETURNS);
  }

  @Override
  public PortfolioBenchmarkReturns prepareData(SecurityData securityData) {
    return PortfolioBenchmarkReturns.from(securityData);
  }

  /**
   * Builds the portfolio context from the supplied monthly returns and runs the CPSD+CPED weighted-average pipeline.
   * Subclasses call this to obtain the raw {@link WeightedAverageResult} when they need direct access to the snapshot
   * (e.g. Growth-of-10K).
   */
  protected WeightedAverageResult<HoldingMonthlyReturns> runWeightedAverage(C command, ReturnFactorScale scale,
      PortfolioBenchmarkReturns returnsData) {
    MonthlyReturnsContext<HoldingMonthlyReturns> context = portfolioMonthlyReturnsContextProvider.get(
        command.getHoldings(), command.getCurrency(), returnsData.portfolio());
    return portfolioWeightedAverageWithCpsdAndCped.run(context,
        new CpsdCpedScaleParams(command.getCustomPsd(), command.getCustomPed(), scale));
  }

  /**
   * Default {@link PeriodCalculationInput} builder for subclasses that consume the weighted-average series via the
   * period-calculation pipeline. Carries the upstream warnings so they propagate into the final result.
   */
  protected PeriodCalculationInput buildPeriodCalculationInput(C command, ReturnFactorScale scale,
      PortfolioBenchmarkReturns returnsData) {
    WeightedAverageResult<HoldingMonthlyReturns> result = runWeightedAverage(command, scale, returnsData);
    return PeriodCalculationInput.builder()
        .weightedAveragePortfolioReturns(result.weightedAverage())
        .warnings(result.getErrorsAsWarnings())
        .build();
  }
}
