package com.fintex.ce.adapter.rest.batch;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.dto.command.AverageMerCommand;
import com.fintex.ce.model.dto.command.BatchCalculationCommand;
import com.fintex.ce.model.dto.command.BestWorstPeriodsCommand;
import com.fintex.ce.model.dto.command.CalculationCommand;
import com.fintex.ce.model.dto.command.CorrelationCommand;
import com.fintex.ce.model.dto.command.DistributionOfReturnsCommand;
import com.fintex.ce.model.dto.command.IncomeForecastCommand;
import com.fintex.ce.model.dto.command.LeadingTotalReturnCommand;
import com.fintex.ce.model.dto.command.PeriodCommand;
import com.fintex.ce.model.dto.command.PortfolioCommand;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.model.dto.command.ReturnCommand;
import com.fintex.ce.model.dto.command.RollingCalculationCommand;
import com.fintex.ce.model.dto.command.RollingCorrelationCommand;
import com.fintex.ce.model.dto.command.TopCommonHoldingsCommand;
import com.fintex.ce.model.dto.command.YieldCommand;
import com.fintex.ce.model.error.ErrorCode;

import org.springframework.stereotype.Component;

@Component
public class BatchCommandFactory {

  public CalculationCommand buildCommand(CalculationMetric metric, BatchCalculationCommand batch) {
    return switch (metric) {
      case COMMON_PERFORMANCE_DATES ->
        throw ErrorCode.METRIC_NOT_SUPPORTED_IN_BATCH.toException(metric.getValue());

      // --- PeriodCommand-based metrics ---
      case TRAILING_TOTAL_RETURNS, EXCESS_RETURNS, STANDARD_DEVIATION, MEAN,
          SHARPE_RATIO, SORTINO_RATIO, MAX_DRAWDOWN, DOWNSIDE_DEVIATION, MAR_RATIO,
          TREYNOR_RATIO, INFORMATION_RATIO, TRACKING_ERROR, ALPHA, BETA, R_SQUARED,
          UPSIDE_CAPTURE, DOWNSIDE_CAPTURE -> buildPeriodCommand(new PeriodCommand(), batch, metric);

      case CORRELATION -> buildPeriodCommand(new CorrelationCommand(), batch, metric);

      case LEADING_TOTAL_RETURNS -> {
        LeadingTotalReturnCommand cmd = new LeadingTotalReturnCommand();
        applyPortfolioFields(cmd, batch, metric);
        applyPeriodFields(cmd, batch);
        cmd.setCustomPsd(batch.getCustomPsd());
        yield cmd;
      }

      case DISTRIBUTION_OF_MONTHLY_RETURNS -> {
        DistributionOfReturnsCommand cmd = new DistributionOfReturnsCommand();
        applyPortfolioFields(cmd, batch, metric);
        applyPeriodFields(cmd, batch);
        cmd.setCustomPsd(batch.getCustomPsd());
        cmd.setCustomNumberOfBins(batch.getCustomNumberOfBins());
        yield cmd;
      }

      // --- RollingCalculationCommand-based metrics ---
      case ROLLING_TOTAL_RETURNS, ROLLING_STANDARD_DEVIATION, ROLLING_SHARPE_RATIO ->
        buildRollingCommand(new RollingCalculationCommand(), batch, metric);

      case ROLLING_CORRELATION -> buildRollingCommand(new RollingCorrelationCommand(), batch, metric);

      // --- ReturnCommand-based metrics ---
      case ANNUAL_RETURNS, GROWTH_OF_10K -> buildReturnCommand(new ReturnCommand(), batch, metric);

      case BEST_WORST_PERIODS -> {
        BestWorstPeriodsCommand cmd = new BestWorstPeriodsCommand();
        applyPortfolioFields(cmd, batch, metric);
        cmd.setCustomPsd(batch.getCustomPsd());
        cmd.setCustomPed(batch.getCustomPed());
        cmd.setBestWorstTimeIntervalPeriods(batch.getBestWorstTimeIntervalPeriods());
        yield cmd;
      }

      // --- PortfolioHoldingsCommand-based metrics ---
      case ASSET_ALLOCATIONS, ASSET_ALLOCATIONS_EM, EQUITY_SECTOR, EQUITY_COUNTRY_EXPOSURE,
          EQUITY_STYLEBOX_EXPOSURE, EQUITY_GEOGRAPHIC_EXPOSURE, EQUITY_MARKET_CAPITALIZATION,
          FIXED_INCOME_COUNTRY_EXPOSURE, FIXED_INCOME_GEOGRAPHIC_EXPOSURE, FIXED_INCOME_BOND_SECTOR,
          FIXED_INCOME_STYLEBOX_EXPOSURE, MATURITY_ALLOCATION, CLASSIFICATION_ALLOCATION, SALES_CHARGE,
          FIXED_INCOME_CREDIT_QUALITY, NUMBER_OF_UNIQUE_HOLDINGS ->
        buildHoldingsCommand(new PortfolioHoldingsCommand(), batch, metric);

      case INCOME_FORECAST -> {
        IncomeForecastCommand cmd = new IncomeForecastCommand();
        applyHoldingsCommandFields(cmd, batch, metric);
        cmd.setTimeIntervalPeriods(batch.getForecastTimeIntervalPeriods());
        yield cmd;
      }

      case YIELD -> {
        YieldCommand cmd = new YieldCommand();
        applyHoldingsCommandFields(cmd, batch, metric);
        cmd.setTimeIntervalPeriods(batch.getForecastTimeIntervalPeriods());
        yield cmd;
      }

      case TOP_COMMON_HOLDINGS -> {
        TopCommonHoldingsCommand cmd = new TopCommonHoldingsCommand();
        applyHoldingsCommandFields(cmd, batch, metric);
        cmd.setNumOfTopCommonHoldings(batch.getNumOfTopCommonHoldings());
        cmd.setAccumulateHoldingTypes(batch.getAccumulateHoldingTypes());
        yield cmd;
      }

      // --- AverageMerCommand-based metrics ---
      case MER, MANAGEMENT_FEE, FEES -> {
        AverageMerCommand cmd = new AverageMerCommand();
        cmd.setMetric(metric);
        cmd.setDataProviders(batch.getDataProviders());
        cmd.setHoldings(batch.getHoldings());
        cmd.setParameterTypes(batch.getParameterTypes());
        yield cmd;
      }
    };
  }

  private PeriodCommand buildPeriodCommand(PeriodCommand cmd, BatchCalculationCommand batch,
      CalculationMetric metric) {
    applyPortfolioFields(cmd, batch, metric);
    applyPeriodFields(cmd, batch);
    return cmd;
  }

  private RollingCalculationCommand buildRollingCommand(RollingCalculationCommand cmd,
      BatchCalculationCommand batch, CalculationMetric metric) {
    applyPortfolioFields(cmd, batch, metric);
    applyPeriodFields(cmd, batch);
    cmd.setCustomPsd(batch.getCustomPsd());
    cmd.setRollingPeriods(batch.getRollingPeriods());
    return cmd;
  }

  private ReturnCommand buildReturnCommand(ReturnCommand cmd, BatchCalculationCommand batch,
      CalculationMetric metric) {
    applyPortfolioFields(cmd, batch, metric);
    cmd.setCustomPsd(batch.getCustomPsd());
    cmd.setCustomPed(batch.getCustomPed());
    return cmd;
  }

  private PortfolioHoldingsCommand buildHoldingsCommand(PortfolioHoldingsCommand cmd,
      BatchCalculationCommand batch, CalculationMetric metric) {
    applyHoldingsCommandFields(cmd, batch, metric);
    return cmd;
  }

  private void applyPortfolioFields(PortfolioCommand cmd, BatchCalculationCommand batch,
      CalculationMetric metric) {
    cmd.setMetric(metric);
    cmd.setHoldings(batch.getHoldings());
    cmd.setBenchmarkHoldings(batch.getBenchmarkHoldings());
    cmd.setCurrency(batch.getCurrency());
  }

  private void applyPeriodFields(PeriodCommand cmd, BatchCalculationCommand batch) {
    cmd.setPeriods(batch.getPeriods());
    cmd.setCustomIntervalPsd(batch.getCustomIntervalPsd());
    cmd.setCustomPed(batch.getCustomPed());
  }

  private void applyHoldingsCommandFields(PortfolioHoldingsCommand cmd, BatchCalculationCommand batch,
      CalculationMetric metric) {
    cmd.setMetric(metric);
    cmd.setHoldings(batch.getHoldings());
    cmd.setDataProviders(batch.getDataProviders());
  }
}
