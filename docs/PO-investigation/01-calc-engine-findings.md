# Calculation Engine Service — Raw Findings (codebase scan)

Source: two read-only exploration passes over `/home/apinta/projects/calculation-engine-service`.

## Metric catalog (50 enum constants in `CalculationMetric`)
> Note: project CLAUDE.md/README says "48 metrics"; the enum actually has **50** constants.
> Discrepancy worth reconciling in docs.

**Returns (7):** trailing-total-returns, leading-total-returns, rolling-total-returns,
annual-returns (calendar), growth-of-10k, distribution-of-monthly-returns, mean.

**Risk / drawdown (7):** standard-deviation, rolling-standard-deviation,
downside-deviation, max-drawdown, sortino-ratio, mar-ratio, excess-returns.

**Risk-adjusted / ratios (8):** sharpe-ratio, rolling-sharpe-ratio, treynor-ratio,
information-ratio, sortino-ratio, mar-ratio, tracking-error, alpha.

**Relative / benchmark (7):** beta, rsquared, correlation, rolling-correlation,
upside-capture, downside-capture, excess-returns.

**Composition / allocation (19):** equity-sector, equity-country-exposure,
equity-stylebox-exposure, equity-geographic-exposure, equity-market-capitalization,
fixed-income-bond-sector, fixed-income-country-exposure, fixed-income-geographic-exposure,
fixed-income-stylebox-exposure, fixed-income-credit-quality, asset-allocations,
asset-allocations-em, maturity-allocation, classification-allocation. (+ stylebox/cap variants)

**Fees (4):** mer, management-fee, fees (annual/monthly $), sales-charge.

**Holdings (2):** top-common-holdings, number-of-unique-holdings.

**Forecasts / income (3):** income-forecast, yield, common-performance-dates.

- **Benchmark-required (10):** excess-returns, treynor, information-ratio, tracking-error,
  alpha, beta, rsquared, upside-capture, downside-capture, rolling-correlation.
- **12-month-minimum (16):** std-dev, mean, sharpe, sortino, downside-dev, excess-returns,
  treynor, info-ratio, tracking-error, alpha, beta, rsquared, up/down-capture, mar, correlation.
- **Holdings-input (27):** all allocation/exposure + fees + holdings + income/yield.

## Methodology
- **Return method:** Time-Weighted Return only. Geometric linking of **monthly return
  factors** (Π(1+r)). **NO** money-weighted return / IRR / XIRR / Modified Dietz.
- **Cash flows:** NOT modeled at all. Fixed beginning-of-period weights; assumes monthly
  rebalance to initial weights. No contributions/withdrawals mid-period.
- **Granularity:** month-end only. No daily/intraday. Annualization factor = **12**
  (hardcoded `TWELVE`); no 252-trading-day convention.
- **Sub-year periods:** returns < 12 months are simple (product − 1), NOT annualized
  → GIPS-aligned on this point. ≥12 months: (product)^(12/n) − 1.
- **Fees:** all return/risk metrics are **gross of fees**. Fees computed as a *separate*
  metric (Σ marketValue_CAD × MER, /12 for monthly). **No gross/net toggle** on returns.
- **FX:** target currency hardcoded **CAD**. Source = Bank of Canada Valet API. Supported
  pairs: USD, EUR, GBP, AUD, JPY, CNY, MXN, CHF → CAD (8). Missing currency on MER-bearing
  holding = hard error; missing FX rate = warning, value left in native currency.
- **Risk-free rate:** per-currency T-bill series fetched from SMS
  `/api/v1/wealth/reference/treasury-rates` (24h cache). Missing month in window = hard error.
- **Risk model:** sample std-dev (n−1) × √12; downside-deviation uses **n** denominator,
  threshold = risk-free rate (not a configurable MAR). Beta/alpha = CAPM on excess returns
  vs benchmark. R² = corr². Max-drawdown computed on the **monthly** growth-of-10k curve
  (so intra-month troughs are invisible → drawdown understated).
- **Lookback windows:** fixed sets (1,3,6,12,36,60,120 + YTD + SINCE_INCEPTION + a custom
  "CIPSD" start date). Rolling windows fixed at 12/36/60/120. Not arbitrary.
- **Forecasts:** only income-forecast, yield, leading-total-returns. No Monte Carlo / goal
  probability / glide path.

## Edge-case handling (recent commits)
- Zero denominators (Sharpe/Sortino/MaxDD) → return null, no 500.
- Insufficient months → warning INSUFFICIENT_MONTHLY_RETURNS_FOR_PERIOD, null result.
- Missing T-bill rate → hard error.
- Missing equity/FI sector → warning + default to "unclassified" bucket.
- Zero holding value → guard, null instead of ∞.
- CIPSD outside data range → null + warning.

## Inputs consumed from SMS
Monthly returns (core, assumed total-return & pre-adjusted), holdings & identifiers,
top-holdings, all allocation/exposure endpoints, fees, sales-charge, income, geography,
and treasury-rates (risk-free). **Corporate actions NOT requested** — engine trusts SMS
to deliver split/dividend-adjusted total-return factors.
