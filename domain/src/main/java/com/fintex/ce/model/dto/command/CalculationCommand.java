package com.fintex.ce.model.dto.command;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "metric", visible = true, defaultImpl = Void.class)
@JsonSubTypes({
    @Type(value = PeriodCommand.class, names = {
        "trailing-total-returns", "excess-returns", "standard-deviation", "mean",
        "sharpe-ratio", "sortino-ratio", "max-drawdown", "downside-deviation",
        "mar-ratio", "treynor-ratio", "information-ratio", "tracking-error",
        "alpha", "beta", "rsquared", "upside-capture", "downside-capture"
    }),
    @Type(value = RollingCalculationCommand.class, names = {
        "rolling-total-returns", "rolling-standard-deviation", "rolling-sharpe-ratio"
    }),
    @Type(value = PortfolioHoldingsCommand.class, names = {
        "asset-allocations", "asset-allocations-em", "equity-sector",
        "equity-country-exposure", "equity-stylebox-exposure", "equity-geographic-exposure",
        "equity-market-capitalization", "fixed-income-country-exposure",
        "fixed-income-geographic-exposure", "fixed-income-bond-sector",
        "fixed-income-stylebox-exposure", "maturity-allocation",
        "classification-allocation", "sales-charge", "fixed-income-credit-quality",
        "number-of-unique-holdings"
    }),
    @Type(value = ReturnCommand.class, names = {"annual-returns", "growth-of-10k"}),
    @Type(value = AverageMerCommand.class, names = {"mer", "management-fee"}),
    @Type(value = LeadingTotalReturnCommand.class, name = "leading-total-returns"),
    @Type(value = BestWorstPeriodsCommand.class, name = "best-worst-periods"),
    @Type(value = DistributionOfReturnsCommand.class, name = "distribution-of-monthly-returns"),
    @Type(value = CorrelationCommand.class, name = "correlation"),
    @Type(value = RollingCorrelationCommand.class, name = "rolling-correlation"),
    @Type(value = IncomeForecastCommand.class, name = "income-forecast"),
    @Type(value = YieldCommand.class, name = "yield"),
    @Type(value = MultiplePortfoliosCommand.class, name = "common-performance-dates"),
    @Type(value = TopCommonHoldingsCommand.class, name = "top-common-holdings")
})
public abstract class CalculationCommand {

  @Schema(description = "Calculation metric type that determines which calculation to execute")
  private CalculationMetric metric;
}
