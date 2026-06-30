# Calculation Engine — Metric Formula Reference

This document lists every metric the calculation-engine-service computes, with the
formula **as actually implemented in code** (not textbook approximations) and a
one-line explanation. File/line references point to the core computation.

## Conventions

- `rᵢ` = monthly return for month *i*; `n` = number of months in the period.
- `∏` = product across the months in the period; `Σ` = sum across months.
- Annualization of a **compounded return** uses the exponent `^(12/n)`.
- Annualization of a **volatility** uses `× √12`.
- All returns use **geometric (multiplicative) compounding** — `∏(1+rᵢ)` — never summation.
- Holding values are **FX-converted to a target currency (default CAD)** before weighting.

---

## 1. Return metrics

| Metric | Formula (as implemented) | What it measures |
|---|---|---|
| **Trailing total returns** | `n<12: ∏(1+rᵢ) − 1`  ·  `n≥12: (∏(1+rᵢ))^(12/n) − 1` | Compound return over the last *n* months ending at the report date; annualized once the period reaches a year. |
| **Leading total returns** | Same as trailing, but window starts at **inception** going forward | Forward-looking compound return from portfolio inception. |
| **Rolling total returns** | Trailing-return formula recomputed for every month's *n*-month window | How the *n*-month return evolves month over month. |
| **Excess returns** | `annualized_portfolio − annualized_benchmark` (both `(∏(1+rᵢ))^(12/n) − 1`, min 12 mo) | Annualized outperformance vs. the benchmark. |
| **Annual returns** | Per calendar year: `∏ⱼₐₙ→ᵈᵉᶜ(1+rᵢ) − 1` | Compounded return for each full calendar year (partial years skipped). |
| **Growth of $10K** | seed `$10,000`; `Vₜ = Vₜ₋₁ × (1+rₜ)` | Dollar value curve of a hypothetical $10K invested, compounded monthly. |
| **Best / worst periods** | rolling `∏(1+rⱼ) − 1` per *n*-mo window; annualized `(1+v)^(12/n) − 1` if `n≥12`; also % positive periods | Best and worst *n*-month windows plus average and hit-rate. |
| **Distribution of monthly returns** | bins = `⌊√count⌋`; width = `(max−min)/bins`; frequency per bin | Histogram of monthly (and annual, if ≥12 mo) returns. |
| **Mean** | `(Σrᵢ)/n` (arithmetic, min 12 mo) | Simple arithmetic average monthly return. |

Core files: `TrailingTotalReturnsCalculation.java:39`, `LeadingTotalReturnsCalculation.java:28`,
`RollingTotalReturnsCalculation.java:26`, `ExcessReturnsCalculation.java:28`,
`AnnualReturnServiceImpl.java:75`, `GrowthOf10KCalculationServiceImpl.java:75`,
`BestWorstPeriodCalculation.java:154`, `DistributionOfReturnsCalculation.java:95`,
`MeanCalculation.java:45`.

---

## 2. Risk & risk-adjusted metrics

| Metric | Formula (as implemented) | What it measures |
|---|---|---|
| **Standard deviation** | `√( Σ(rᵢ−μ)² / (n−1) ) × √12` | Annualized volatility; **sample** variance (divisor `n−1`). |
| **Rolling standard deviation** | same formula, per *n*-month window | Volatility over a moving window. |
| **Sharpe ratio** | `(annⁿ_return − annⁿ_riskfree) / stdev`; return & T-Bill each annualized as `(Σr/n)×12` | Excess return per unit of total risk. Returns null if stdev = 0. |
| **Rolling Sharpe ratio** | Sharpe per window; a window degrades to null if its T-Bill data is missing | Sharpe over a moving window. |
| **Sortino ratio** | `(annⁿ_return − annⁿ_riskfree) / downside_deviation` | Excess return per unit of **downside** risk only. |
| **Downside deviation** | `√( Σ min(rᵢ−rf, 0)² / n ) × √12` | Volatility of below-risk-free returns only; **population** divisor `n`. |
| **Max drawdown** | per month `(Vₜ − peak₀→ₜ)/peak₀→ₜ`; result = most negative; **recovery** = months until value ≥ prior peak | Worst peak-to-trough loss, and how long recovery took. |
| **MAR ratio** | `annualized_total_return / |max_drawdown|` | Return earned per unit of worst-case drawdown. |

> Note the two different divisors: **standard deviation uses `n−1`** (sample) while
> **downside deviation uses `n`** (population). Intentional, but a common source of
> "why don't Sharpe and Sortino denominators reconcile" questions.

Core files: `StandardDeviationCalculation.java:75`, `RollingStandardDeviationCalculation.java:27`,
`SharpeRatioCalculation.java:88`, `RollingSharpeRatioCalculation.java:34`,
`SortinoRatioCalculation.java:78`, `DownsideDeviationCalculation.java:70`,
`MaxDrawdownCalculation.java:111`, `MarRatioCalculation.java:34`.

---

## 3. Relative / benchmark metrics

Beta, Alpha and R² operate on **excess returns** (`rᵢ − T-Bill`). `μₚ`, `μᵦ` = mean
portfolio / benchmark excess return.

| Metric | Formula (as implemented) | What it measures |
|---|---|---|
| **Beta** | `Cov(Rₚ−Rf, Rᵦ−Rf) / Var(Rᵦ−Rf) = Σ(rₚ−μₚ)(rᵦ−μᵦ) / Σ(rᵦ−μᵦ)²` | Sensitivity of the portfolio to benchmark moves. |
| **Alpha** | `(μₚ − β·μᵦ) × 12` | Annualized return beyond what beta (CAPM) predicts. |
| **R-squared** | `1 − SSE/TSS`, with `SSE = Σ(rₚ−rᵦ)²`, `TSS = Σ(rₚ−μₚ)²` | Fraction of portfolio variance explained by the benchmark. |
| **Correlation** | `Σ(xᵢ−μₓ)(yᵢ−μᵧ) / √(Σ(xᵢ−μₓ)²·Σ(yᵢ−μᵧ)²)` (Pearson) | Strength of co-movement between the two return series. |
| **Rolling correlation** | Pearson correlation per *n*-month window | Correlation over a moving window. |
| **Tracking error** | `√( Σ(eᵢ−μₑ)² / (n−1) ) × √12`, where `eᵢ = rₚ,ᵢ − rᵦ,ᵢ` | Annualized volatility of active (portfolio−benchmark) returns. |
| **Information ratio** | `(annⁿ_portfolio − annⁿ_benchmark) / tracking_error` | Active return per unit of active risk. |
| **Treynor ratio** | `(annⁿ_portfolio − annⁿ_riskfree) / β` | Excess return per unit of **systematic** (beta) risk. |
| **Upside capture** | over months where `rᵦ>0`: `(geoMean(rₚ) / geoMean(rᵦ)) × 100`, geoMean = `∏(1+r)^(1/n) − 1` | % of benchmark's gains captured in up markets. |
| **Downside capture** | same, over months where `rᵦ<0` | % of benchmark's losses absorbed in down markets. |

> Capture ratios use **per-month geometric means** (`∏(1+r)^(1/n)`), not cumulative
> compounded period returns — worth verifying against the product spec, as some
> providers define capture on cumulative returns.

Core files: `AlphaBetaCalculationAbstract.java:60`, `AlphaCalculation.java:61`,
`RSquaredCalculationAbstract.java:53`, `CorrelationCalculation.java:185`,
`RollingCorrelationCalculation.java:32`, `TrackingErrorCalculation.java:75`,
`InformationRatioCalculation.java:32`, `TreynorRatioCalculation.java:67`,
`UpsideCaptureCalculation.java:29` / `UpDownSideCalculationAbstract.java:69`,
`DownsideCaptureCalculation.java:29`.

---

## 4. Portfolio composition (allocations & exposures)

All ~15 allocation/exposure metrics share **one** value-weighted aggregation, differing
only in which SMS datapoint feeds the buckets:

```
bucket_weight(type) = Σ_holdings ( holdingWeight × holdingAllocation[type] )
    holdingWeight = valueConvertedToTargetCcy / Σ(all holdings converted)
```

A **denoise** step then clamps `|v| < 1e-5 → 0` (drops Morningstar residuals).

| Metric group | Bucket source / variation |
|---|---|
| **Asset allocations** / **…-EM** | Asset-class buckets; stocks map by region, funds use SMS, cash/GIC single bucket; EM variant splits emerging vs developed. |
| **Equity sector** | Sector buckets (non-cash/GIC); missing → UNCLASSIFIED. |
| **Equity country / geographic exposure** | Country / region buckets from geography or SMS. |
| **Equity market capitalization** | Large/Mid/Small buckets. |
| **Equity stylebox** | 2-D Value/Blend/Growth × cap. |
| **Fixed-income bond sector / country / geographic** | FI-only buckets (also excludes stocks). |
| **Fixed-income credit quality** | AAA…Below-investment-grade buckets. |
| **Fixed-income stylebox** | 2-D duration × credit quality. |
| **Maturity allocation** | Maturity-bucket weighting (0-1y / 1-5y / …). |
| **Classification allocation** | Security-classification buckets, with re-scale normalization. |

Core files: `AbstractAssetAllocationService.java:74`, `AllocationHelper.java:48`,
`PortfolioWeightCalculator.java:35`, `DefaultTargetCurrencyConverter.java:52`.

---

## 5. Fees, holdings & forecast

| Metric | Formula (as implemented) | Notes |
|---|---|---|
| **MER** | `Σ( MERfund × valueConvertedfund / Σ(denominator) )` | Value-weighted avg; per-country fallback chain (CA: MER→Mgmt Fee; US: NER→GER→Mgmt Fee). Modes: funds-only / whole-portfolio / strict. |
| **Management fee** | same weighting, but **only** the management-fee field (no fallback chain) | Missing fee on a fund → error; non-funds set to 0%. |
| **Fees ($)** | `Annual$ = Σ(valueConvertedfund × resolvedFee%)`; `Monthly$ = Annual$ / 12` | Dollar cost in target currency; missing currency on a fee-bearing holding → hard error. |
| **Sales charge** | `alloc[type] = Σ(value where chargeType=type) / Σ(all categorized values)` | Pure value ratios per charge category + per-holding; no FX. |
| **Income forecast** | Equity/fund: `(value × yield)/payoutsPerYear` per future payout date · GIC: `(value × rate/100)/12` per interest period · FI-at-maturity: `value × yield × monthsToMaturity/12` | Per-holding schedule of future (date, amount); past dates dropped. |
| **Yield** | `Σ(value × yield) / Σ(value)` (GIC yield ÷100) | Value-weighted portfolio yield; null yield/value holdings skipped. |
| **Number of unique holdings** | `|distinct(idValue for configured idType)| + count(holdings null for that idType)` | Dedup by one configured identifier (TICKER/ISIN/…). |
| **Top common holdings** | expand funds recursively; `leafWeight = parentWeight × ∏(nodeWeights)`; aggregate by security → group weight; sort by weight desc; take top N | FX-normalized parent weights; depth + max-leaves guards; cycle detection. |
| **Common performance dates** | `start = MAX(holding start dates)`, `end = MIN(holding end dates)` | Overlapping return window common to all holdings (portfolios & benchmark computed separately). |

Core files: `MERCalculationServiceImpl.java:56`, `ManagementFeeCalculationServiceImpl.java:66`,
`FeesCalculationServiceImpl.java:32`, `MerFeeResolver.java:25`, `SalesChargeCalculation.java:55`,
`IncomeForecastCalculationServiceImpl.java:68`, `YieldCalculationServiceImpl.java`,
`NumberOfUniqueHoldingsService.java:49`, `CommonHoldingsService.java:75`,
`CommonPerformanceDateServiceImpl.java:39`.

---

## Correctness flags worth double-checking

1. **Divisor asymmetry** — standard deviation uses `n−1` (sample), downside deviation
   uses `n` (population). Confirm this matches the product spec for Sharpe vs. Sortino.
2. **Capture ratios** use per-month geometric means, not cumulative compounded returns.
3. **FX ordering** — all allocation/fee weights are computed *after* FX conversion, which
   materially affects multi-currency portfolios.
4. **Fee fallback chains are country-specific** (CA vs. US); a missing currency on a
   fee-bearing holding is a hard error, whereas allocation gaps degrade to UNCLASSIFIED.
