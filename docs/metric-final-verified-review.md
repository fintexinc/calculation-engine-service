# Calculation Engine Metrics — Prioritized Code Fix Backlog

Review date: 2026-07-01  
Workspace: `F:\Fintex\Projects\calculation-engine-service`  
Branch reviewed: `TMI-359-Implement-tracing-properly-between-services`

## 1. Purpose and source verification

This file is a source-verified backlog of suggested calculation-code fixes, ordered by importance. It is not a formula specification; it is an implementation backlog for risks, bugs, and hardening work found in the calculation code.

The review checked calculation classes, orchestration services, mappers, and representative tests under:

- `application/src/main/java/com/fintex/ce/application/calculation/metric`
- `application/src/main/java/com/fintex/ce/application/calculation/metric/core`
- `application/src/main/java/com/fintex/ce/application/calculation/service`
- `application/src/main/java/com/fintex/ce/application/calculation/service/allocation`
- `application/src/main/java/com/fintex/ce/application/calculation/service/fee`
- `application/src/main/java/com/fintex/ce/application/mapping/response`
- `domain/src/main/java/com/fintex/ce/model/domain/enumeration/CalculationMetric.java`
- representative unit, integration, and E2E tests for the highest-risk metric families.

Important limit: these findings compare source behavior with standard financial/math expectations and visible in-repository contracts. Product owners still need to decide ambiguous conventions such as capture-ratio basis, R-squared return basis, and no-data behavior.

## 2. Executive priority order

Fix in this order:

1. **P0 — Correct confirmed wrong or unsafe formulas:** R-squared, rolling correlation, exact date alignment, capture-ratio missing-data defaults, and zero-denominator handling for benchmark ratios.
2. **P1 — Stabilize cross-family data policies:** shared return-basis helpers, explicit risk-free/T-Bill preconditions, currency basis per metric family, distribution edge cases, and income/yield unit handling.
3. **P2 — Improve tests so they prevent financial regressions:** independent-oracle numeric tests, representative E2E coverage for high-risk ratios, and replacement of tests that assert implementation quirks.
4. **P3 — Improve maintainability:** named factory paths instead of nullable switches, centralized precision policy, injected clocks, and consistent warnings for null/degenerate period results.

## 3. P0 — Must fix before trusting affected metric outputs

### P0.1 Fix `rsquared`: current formula can produce impossible negative R²

**Source evidence**

- `application/src/main/java/com/fintex/ce/application/calculation/metric/core/RSquaredCalculationAbstract.java`
  - `calculateRSquared(...)`
  - `calculateSumSquaredRegression(...)`
  - `calculateTotalSumOfSquares(...)`
- `application/src/test/java/com/fintex/ce/application/calculation/metric/core/RSquaredCalculationAbstractTest.java`
  - `shouldReturnRSquaredValue_whenRegressionAndTotalSumProvided()`

**Current behavior**

`RSquaredCalculationAbstract.calculateRSquared(...)` computes:

```text
R² = 1 - SSR/TSS
SSR = Σ(portfolioExcessReturn_i - benchmarkExcessReturn_i)^2
TSS = Σ(portfolioExcessReturn_i - avgPortfolioExcessReturn)^2
```

This is not standard coefficient of determination for portfolio-vs-benchmark regression. The residual term is not the residual from a fitted regression line unless alpha is zero and beta is one. The current test protects this behavior by expecting a negative R² value:

```text
expected = -0.0161300415
```

**Risk**

- R² can be negative for valid input, which contradicts the normal “variance explained” interpretation.
- Perfect linear relationships where beta is not one may be reported below 1.
- Consumers comparing against providers/libraries will see non-standard values.
- The existing test suite will fail if the formula is corrected without first updating expected values.

**Code fix**

Choose the basis explicitly:

- common provider parity: raw portfolio and benchmark return series; or
- CAPM/excess-return basis: portfolio excess and benchmark excess return series.

Then implement R² as either:

```text
R² = PearsonCorrelation(chosenPortfolioSeries, chosenBenchmarkSeries)^2
```

or:

```text
R² = 1 - Σ(y_i - (alpha + beta*x_i))² / Σ(y_i - avg(y))²
```

where alpha and beta are fitted from the same aligned input window.

Add denominator guards:

- if benchmark variance is zero, return a documented null/warning or domain error;
- if portfolio variance is zero, return a documented null/warning or domain error;
- never divide by zero through `DecimalUtils.divide(...)` for degenerate windows.

**Tests to add/change**

- Replace the negative-R² expected-value test.
- `portfolio = 2 × benchmark` must produce `R² = 1`.
- `portfolio = -1 × benchmark` must produce `R² = 1` if using squared Pearson correlation.
- Valid non-degenerate inputs must satisfy `0 <= R² <= 1`.
- R² must equal squared Pearson correlation for the same aligned dataset.
- Zero benchmark variance and zero portfolio variance must follow the documented null/error contract.

### P0.2 Fix `rolling-correlation`: it delegates raw returns into a helper that expects centered values

**Source evidence**

- `application/src/main/java/com/fintex/ce/application/calculation/metric/RollingCorrelationCalculation.java`
  - `calculateRollingValue(...)`
  - `initializePortfolioReturns(...)`
  - `initializeBenchmarkReturns(...)`
- `application/src/main/java/com/fintex/ce/application/calculation/metric/CorrelationCalculation.java`
  - `calculatePortfolioBaseTotalReturnValuesByPeriod(...)`
  - `calculateCorrelation(...)`
- `application/src/test/java/com/fintex/ce/application/calculation/metric/RollingCorrelationCalculationTest.java`

**Current behavior**

`CorrelationCalculation.calculateCorrelation(...)` computes:

```text
Σ(x_i * y_i) / sqrt(Σx_i² * Σy_i²)
```

That is Pearson correlation only if `x` and `y` are already de-meaned. Ordinary holding-to-holding correlation de-means each holding return series earlier in `calculatePortfolioBaseTotalReturnValuesByPeriod(...)`.

`RollingCorrelationCalculation.calculateRollingValue(...)` instead passes raw portfolio and benchmark windows directly to `calculateCorrelation(...)`:

```text
return correlationCalculation.calculateCorrelation(portfolioReturns, benchmarkReturns);
```

**Risk**

- Rolling correlation is closer to raw cosine similarity than Pearson correlation when the rolling window is not zero-centered.
- Users can receive materially wrong correlations.
- R² cannot be safely compared to rolling correlation until both use the same aligned Pearson basis.

**Code fix**

Create a dedicated helper for two-series Pearson correlation:

```text
pearson(seriesA, seriesB):
  aligned = requireSameDates(seriesA, seriesB)
  avgA = average(aligned.A)
  avgB = average(aligned.B)
  numerator = Σ((A_i - avgA) * (B_i - avgB))
  denominator = sqrt(Σ(A_i - avgA)^2 * Σ(B_i - avgB)^2)
```

Use it from `RollingCorrelationCalculation` and the fixed R² implementation. Do not reuse `CorrelationCalculation.calculateCorrelation(...)` with raw maps.

**Tests to add/change**

- A 12-month window where Pearson and uncentered cosine differ, for example monotonic shifted series.
- Perfect positive and negative linear relationships.
- Zero-variance portfolio or benchmark window.
- Missing or extra dates in either series.
- Boundary window where portfolio and benchmark have the same count but different dates.

### P0.3 Replace count-only validation with exact date alignment for two-series and three-series metrics

**Source evidence**

- `application/src/main/java/com/fintex/ce/application/calculation/metric/core/PortfolioBenchmarkCalculationAbstract.java`
  - `calculatePeriodForNumberOfMonths(...)`
  - `availableMonths()`
- `application/src/main/java/com/fintex/ce/application/calculation/metric/TrackingErrorCalculation.java`
  - `calculateExcessPortfolioReturnOverBenchmark()`
- `application/src/main/java/com/fintex/ce/application/calculation/metric/core/UpDownSideCalculationAbstract.java`
  - `getPortfolioDetermination()`
- `application/src/main/java/com/fintex/ce/application/calculation/metric/core/PeriodCalculationAbstract.java`
  - `calculateExcessReturn(...)`
  - `validateTBillsCoverage(...)`

**Current behavior**

Several benchmark/risk-free calculations validate only the size of the input series. Some then substitute synthetic values:

- tracking error uses `benchmarkReturnOrZero` when a benchmark date is missing;
- capture ratios use `HUNDRED` as the missing portfolio return fallback, which becomes a factor of `2.0` after `/100 + 1`;
- benchmark regression metrics validate excess-return sizes but do not prove portfolio/benchmark/T-Bill dates are identical for the target window.

**Risk**

A portfolio series and a benchmark/risk-free series can have equal counts but different dates. The result can look plausible while being calculated from substituted or misaligned data.

**Code fix**

Introduce one shared alignment utility, for example:

```text
AlignedSeries alignExact(String metric, NavigableMap<LocalDate, BigDecimal> left,
	NavigableMap<LocalDate, BigDecimal> right, SortedSet<LocalDate> requiredWindowDates)
```

and a three-series variant for portfolio/benchmark/T-Bill metrics.

Recommended policy:

- exact date coverage for all dates in the requested window;
- no synthetic numeric defaults for missing portfolio, benchmark, or T-Bill values;
- return a documented warning/null or throw a domain exception with metric/date context;
- apply the same policy per rolling window.

**Tests to add/change**

- Same count, different dates must not calculate.
- Portfolio missing one benchmark-qualified capture date must not become `+100%`.
- Benchmark missing one tracking-error date must not become `0`.
- Excess-return maps derived from T-Bills must be checked date-by-date.

### P0.4 Remove synthetic defaults from upside/downside capture calculations

**Source evidence**

- `application/src/main/java/com/fintex/ce/application/calculation/metric/core/UpDownSideCalculationAbstract.java`
- `application/src/main/java/com/fintex/ce/application/calculation/metric/UpsideCaptureCalculation.java`
- `application/src/main/java/com/fintex/ce/application/calculation/metric/DownsideCaptureCalculation.java`

**Current behavior**

For each qualifying benchmark date, missing portfolio return is handled as:

```text
ofNullable(getPortfolioTotalReturns().get(e.getKey())).orElse(HUNDRED)
```

After dividing by 100 and adding 1, that missing value becomes a `2.0` return factor, equivalent to a +100% monthly return.

**Risk**

A missing portfolio date in an up/down benchmark window can materially inflate or deflate capture ratios without an error.

**Code fix**

Use exact portfolio/benchmark date alignment before building capture factors. Decide and encode the product convention:

- current implementation uses per-month geometric means; or
- product may require cumulative compounded return over the selected months.

Either convention is acceptable only if explicitly decided and tested. Missing data must not be represented as a numeric return.

**Tests to add/change**

- Missing portfolio date in an upside window.
- Missing portfolio date in a downside window.
- Zero benchmark return is excluded from both upside and downside windows.
- Empty qualifying benchmark window returns documented null/zero behavior.
- Benchmark geometric mean of zero must not cause an accidental divide-by-zero path.

### P0.5 Add zero-denominator/null guards to ratio metrics

**Source evidence**

- `application/src/main/java/com/fintex/ce/application/calculation/metric/core/AlphaBetaCalculationAbstract.java`
  - `calculateBeta(...)`
- `application/src/main/java/com/fintex/ce/application/calculation/metric/core/RSquaredCalculationAbstract.java`
  - `calculateRSquared(...)`
- `application/src/main/java/com/fintex/ce/application/calculation/metric/InformationRatioCalculation.java`
  - `calculatePeriodForNumberOfMonths(...)`
- `application/src/main/java/com/fintex/ce/application/calculation/metric/TreynorRatioCalculation.java`
  - `calculateTreynorRatio(...)`
- `application/src/main/java/com/fintex/ce/application/calculation/metric/CorrelationCalculation.java`
  - `calculateCorrelation(...)`

**Current behavior**

Some ratios explicitly return null for zero denominator (`SharpeRatioCalculation`, `SortinoRatioCalculation`), but others divide without a local guard:

- beta divides by benchmark excess variance;
- R-squared divides by portfolio total sum of squares;
- information ratio divides by tracking error;
- Treynor divides by beta;
- correlation divides by `sqrt(sumX² * sumY²)`.

**Risk**

Flat or degenerate series can produce exceptions, undefined values, or inconsistent null behavior across similar metrics.

**Code fix**

Define one ratio-denominator contract and apply it consistently:

```text
if denominator == null or denominator == 0:
  return null and add metric-specific warning
```

or throw a domain exception if product requires hard failure. Prefer null + warning for period metrics so the response can still include other periods.

**Tests to add/change**

- flat benchmark for beta and Treynor;
- flat portfolio or benchmark for R² and correlation;
- zero tracking error for information ratio;
- period response still contains other successful periods and emits a warning for the failed period.

## 4. P1 — Should fix before provider comparison or broad release

### P1.1 Centralize return-basis conventions

**Source evidence**

- `TrailingTotalReturnsCalculation`
- `ExcessReturnsCalculation`
- `StandardDeviationCalculation`
- `SharpeRatioCalculation`
- `SortinoRatioCalculation`
- `DownsideDeviationCalculation`
- `AlphaBetaCalculationAbstract`
- `TrackingErrorCalculation`
- `InformationRatioCalculation`
- `TreynorRatioCalculation`

**Current behavior**

Different families intentionally use different bases:

- total-return metrics use geometric product of monthly return factors;
- standard deviation and tracking error use arithmetic deviations and annualize with `sqrt(12)`;
- Sharpe, Sortino, and Treynor annualize portfolio and T-Bill returns with arithmetic average times 12;
- alpha/beta/R² use excess-return maps derived from T-Bills;
- information ratio uses trailing total return for active return and tracking error for active risk.

The code can support these choices, but the conventions are spread across classes and easy to mix incorrectly.

**Code fix**

Create small named helpers for each convention:

- `geometricAnnualizedReturn(window)`
- `arithmeticAnnualizedReturn(window)`
- `sampleAnnualizedVolatility(window)`
- `populationAnnualizedDownsideDeviation(window)`
- `activeReturnSeries(portfolio, benchmark)`
- `excessReturnSeries(totalReturns, tBills)`

Use these helpers from metric classes to make convention drift visible in code review.

**Tests to add/change**

- one independent-oracle test per helper;
- a ratio-family test that proves Sharpe/Sortino/Treynor use arithmetic annualization if that remains the selected product convention;
- a total-return-family test that proves geometric annualization.

### P1.2 Make T-Bill/risk-free dependencies explicit and consistent

**Source evidence**

- `PeriodCalculationAbstract.validateTBillsCoverage(...)`
- `TrailingTotalReturnsCalculation`
- `SharpeRatioCalculation`
- `SortinoRatioCalculation`
- `DownsideDeviationCalculation`
- `TreynorRatioCalculation`
- `PortfolioBenchmarkCalculationAbstract`

**Current behavior**

T-Bill checks have improved in several classes, but the dependency is not uniformly expressed at metric boundaries. `TrailingTotalReturnsCalculation` has an optional `tBills` constructor path that enforces coverage only when provided, while composed internal usages avoid the precondition.

**Risk**

It is difficult to tell from a metric class whether missing risk-free data should return null, warning, or exception. Internal reuse of `TrailingTotalReturnsCalculation` can also obscure whether T-Bills are actually part of the metric contract.

**Code fix**

Introduce explicit interfaces or factory methods:

- `TrailingTotalReturnsCalculation.withTBillPrecondition(...)`
- `TrailingTotalReturnsCalculation.mathOnly(...)`
- `RiskFreeWindowValidator.requireCoverage(...)`

Do not rely on a nullable constructor argument to switch behavior.

**Tests to add/change**

- missing T-Bill date inside the requested window;
- T-Bill data available outside but not inside the requested window;
- metrics that do not require T-Bills must not fetch or validate them.

### P1.3 Decide and enforce currency basis per metric family

**Source evidence**

- `DefaultTargetCurrencyConverter`
- `PortfolioWeightCalculator`
- `AbstractAssetAllocationService.perform(...)`
- `AbstractAssetAllocationService.calculate(...)`
- `BreakdownAbstractService.calculateNetProducts(...)`
- `AbstractFeeCalculationService`
- `FeesCalculationServiceImpl`
- `SalesChargeCalculation`
- `YieldResponseMapper`
- `CommonHoldingsService`

**Current behavior**

Currency handling is not uniform:

- asset allocation `perform(...)` uses `PortfolioWeightCalculator`, which converts holding values to the default target currency when a source currency is available;
- asset allocation `calculate(...)` and most `BreakdownAbstractService` subclasses use raw holding values;
- fee/MER calculations convert market values before weighting/summing;
- fees convert only MER-bearing holdings because zero-fee holdings do not affect fee dollars;
- top common holdings converts parent values before allocating leaf weights;
- sales charge uses raw holding values;
- yield uses raw holding values and only divides GIC yield by 100;
- missing FX rates usually warn and keep raw values, while missing currency is fatal only in selected fee/top-holdings paths.

**Risk**

Multi-currency portfolios can produce family-dependent results that are hard to explain. Some paths can silently mix currencies.

**Code fix**

Create a currency policy table in code/config and enforce it through named strategies:

```text
TARGET_CURRENCY_REQUIRED
TARGET_CURRENCY_BEST_EFFORT_WITH_WARNING
RAW_VALUE_INTENTIONAL
LOCAL_SOURCE_ONLY
```

Apply one strategy per metric family. If raw value is intentional for sales charge or yield, encode that in class names/tests. If target-currency weighting is required, route all breakdown calculations through `PortfolioWeightCalculator` or equivalent.

**Tests to add/change**

- mixed CAD/USD portfolio with non-1 FX rate for each value-weighted family;
- missing currency behavior per family;
- FX-rate unavailable behavior per family;
- same holdings through asset allocation, fixed-income breakdown, fees, sales charge, yield, and top common holdings to prove intentional differences.

### P1.4 Define distribution behavior for empty, flat, and bin-boundary inputs

**Source evidence**

- `DistributionOfReturnsCalculation.calculateDistributionOfReturnsFor(...)`
- `DistributionOfReturnsCalculation.calculateNumberOfBins(...)`
- `DistributionOfReturnsCalculation.calculateBinWidthIncrements(...)`
- `DistributionOfReturnsCalculation.calculateFrequencyOfReturns(...)`

**Current behavior**

Number of bins defaults to `floor(sqrt(count))`. Bin width is `(max - min) / bins`. Frequencies use `<= current bin` and `> previous bin` comparisons. Annual distribution is omitted when 12-month rolling returns are unavailable.

**Risk**

- Empty input can fail when min/max/first/last are requested.
- Flat input gives zero bin width, producing repeated identical bin ranges.
- Boundary values can be assigned unexpectedly if rounding changes bin intervals.
- Custom bin values rely on validation outside this class; the calculation class does not defend itself.

**Code fix**

Add explicit contracts:

- empty returns -> null interval with warning or empty distribution result;
- one return -> one bin or documented null;
- flat returns -> one bin containing all observations or N bins with only one non-zero frequency, explicitly tested;
- custom bins outside accepted range -> validation exception before calculation.

**Tests to add/change**

- empty map;
- one data point;
- all equal returns;
- values exactly equal to bin edges;
- custom bins at min and max allowed values;
- annual returns unavailable because fewer than 12 months.

### P1.5 Define income/yield source-unit and no-data contracts

**Source evidence**

- `YieldCalculationServiceImpl`
- `YieldResponseMapper`
- `IncomeForecastCalculationServiceImpl`

**Current behavior**

Yield calculation:

- skips holdings with null yield or null value;
- uses raw holding values as weights;
- divides GIC yield by 100;
- returns `BigDecimal.ZERO` if no usable weighted yield data exists.

Income forecast:

- GIC client interest rate is divided by 100;
- non-GIC dividend yield is used as supplied;
- fixed income at maturity uses `amount * dividendYield / 12 * monthsBetween`;
- forecast entries before current date are filtered out based on `LocalDate.now()`.

**Risk**

- If Security Master supplies non-GIC dividend yield as `5` for 5% instead of `0.05`, income/yield will be overstated by 100x.
- Returning zero for no usable yield data can be indistinguishable from a true 0% yield portfolio.
- Time-dependent filtering makes tests and outputs dependent on the system clock.

**Code fix**

- Define source unit per instrument type and provider field.
- Convert all percentages at mapper boundaries, not inside aggregate loops.
- Return null plus warning when no usable yield data exists, unless product explicitly wants zero.
- Inject `Clock` for income forecast filtering instead of calling `LocalDate.now()` directly.

**Tests to add/change**

- GIC yield `5` means 5% after `/100`.
- Non-GIC yield `0.05` means 5% if decimal is the chosen contract.
- Non-GIC yield `5` is rejected or normalized according to the selected source-unit policy.
- No usable yield data returns the documented null/zero behavior.
- Income forecast with fixed clock so future/past filtering is deterministic.

### P1.6 Align common-performance-date behavior with downstream calculation windows

**Source evidence**

- `CommonPerformanceDateServiceImpl`
- `MonthlyReturnsService`
- `ReturnsSnapshot`

**Current behavior**

The service separately computes common portfolio and benchmark date ranges. Empty or null snapshots become `DateRange.UNBOUNDED`. It does not verify overlap between the portfolio common range and benchmark common range.

**Risk**

A caller can receive portfolio and benchmark common ranges that do not overlap enough for benchmark metrics, then later calculations fail or return null/warnings in ways that are not predictable from the common-date response.

**Code fix**

Return both individual ranges and the intersection range needed by benchmark metrics, or emit a warning when portfolio and benchmark ranges do not overlap for at least the minimum required months.

**Tests to add/change**

- empty portfolio list;
- empty benchmark list;
- portfolio and benchmark ranges that do not overlap;
- overlapping range shorter than 12 months;
- warnings from `ReturnsSnapshot` are preserved.

## 5. P2 — Testing improvements required to prevent regressions

### P2.1 Replace implementation-coupled tests with independent-oracle tests

**Source evidence**

Many current tests mock the class under test and verify internal method calls. This is useful for orchestration but insufficient for financial correctness. Concrete examples include R-squared, information ratio, tracking error, and rolling correlation tests.

**Required change**

For formula correctness, expected values must be computed independently from production classes. Acceptable options:

- inline derivation in the test fixture;
- a small independent helper in test sources only;
- spreadsheet/Python/R expected values pasted as constants with explanation;
- property/invariant assertions such as bounds, symmetry, and scale relationships.

**Priority independent-oracle tests**

1. R-squared and Pearson correlation invariants.
2. Date-alignment failures for benchmark and T-Bill metrics.
3. Capture-ratio edge cases.
4. Zero-denominator ratios.
5. Multi-currency weighting differences.
6. Distribution flat/empty/bin-edge behavior.
7. Income/yield source-unit behavior.

### P2.2 Add representative E2E coverage for high-risk metric families

**Current coverage profile**

E2E coverage exists for selected return, allocation, fee, and cache-adjacent flows, but high-risk benchmark/risk-free ratio families need representative HTTP-boundary scenarios.

**Required E2E additions**

- Benchmark regression family: alpha, beta, R² after fix, and correlation/R² invariant for one request fixture.
- Capture family: up, down, zero, missing, and flat benchmark cases.
- Risk-free family: Sharpe, Sortino, Treynor with complete and missing T-Bill scenarios.
- Active-risk family: tracking error and information ratio with exact date alignment and zero tracking error.
- Currency-sensitive family: same mixed-currency holdings through allocation, fixed-income breakdown, fees, sales charge, yield, and top common holdings.
- Distribution family: fewer than 12 months, flat returns, and custom-bin boundary behavior.

Follow repository rules:

- E2E tests live in `bootstrap`.
- Use full HTTP boundary.
- Do not use Mockito or `@MockBean` for application beans.
- Build request and external-service payloads with DTO/domain objects and the configured `ObjectMapper`.
- Parse responses into typed DTOs and assert exact numeric values, warnings, PSD/PED/CPSD/CPED, and normalization invariants.

### P2.3 Strengthen integration tests around external data gaps

Add adapter/integration coverage for:

- partial T-Bill series from Security Master;
- missing benchmark dates;
- missing currency fields;
- FX-rate unavailable responses;
- malformed or ambiguous yield units;
- missing fee rows for MER-bearing holdings;
- top-holdings trees with missing weights, cycles, and max-depth truncation.

External HTTP services can be simulated by the repository’s HTTP test server pattern. Do not replace application beans with mocks in integration/E2E tests.

## 6. P3 — Lower-priority code maintainability fixes

### P3.1 Replace nullable constructor switches with explicit variants

`TrailingTotalReturnsCalculation` changes behavior based on whether `tBills` is null. Replace this with explicit named constructors/factories so call sites show whether T-Bill coverage is required.

### P3.2 Centralize scale and rounding decisions

`DecimalUtils.pow(...)` uses `Math.pow(double, double)` internally, and some call sites round intermediate products before exponentiation. Keep this if acceptable, but document it and test tolerances per metric. Avoid applying user scale before intermediate calculations unless product requires that exact behavior.

### P3.3 Make time-dependent services clock-injected

`IncomeForecastCalculationServiceImpl` uses current date directly. Inject `Clock` so production remains current-date based and tests become deterministic.

### P3.4 Make warnings consistent for null period values

`PeriodCalculationAbstract.addInsufficientDataWarnings(...)` handles nulls caused by insufficient data and CIPSD out-of-range. Degenerate denominators, zero variance, missing aligned dates, and empty qualifying windows should add metric-specific warnings too, not just return null silently.

### P3.5 Reduce direct raw-value aggregation paths where currency matters

`BreakdownAbstractService` and `AllocationHelper.calculateNetProducts(...)` intentionally aggregate with raw holding values. Keep this only where raw-value behavior is explicitly chosen. Otherwise route through a common weight calculator that applies the selected currency policy.

## 7. Formula-family verification map

| Metric family | Main source checked | Current formula behavior | Main action |
|---|---|---|---|
| Trailing/leading/rolling total returns | `TrailingTotalReturnsCalculation`, rolling/leading service classes | Geometric product; annualized with `12/n` exponent at 12+ months | Keep formula; clarify explicit T-Bill precondition variants and intermediate precision |
| Excess returns | `ExcessReturnsCalculation` | Difference between annualized geometric portfolio and benchmark returns | Add exact date-alignment guard |
| Annual returns | `AnnualReturnServiceImpl` | Full Jan-Dec years only; bracketed incomplete years throw | Keep; add E2E for incomplete bracketed year |
| Growth of 10K | `GrowthOf10KCalculationServiceImpl`, `Growth10KHelper` | Compounds factor-form monthly returns from a 10,000 seed | Keep; test empty series and scale assumptions |
| Best/worst periods | `BestWorstPeriodCalculation` | Rolling compounded windows with positive-period stats | Add independent oracle and edge windows |
| Distribution | `DistributionOfReturnsCalculation` | Histogram with default `floor(sqrt(count))` bins | Define empty/flat/bin-edge behavior |
| Mean | `MeanCalculation` | Arithmetic average, 12-month minimum | Keep; test warning/null behavior |
| Standard deviation | `StandardDeviationCalculation` | Sample volatility, annualized by `sqrt(12)` | Keep; add flat/one-window tests |
| Sharpe | `SharpeRatioCalculation` | Arithmetic annualized excess over T-Bills divided by standard deviation | Keep if product confirms; strengthen T-Bill/date tests |
| Sortino/downside deviation | `SortinoRatioCalculation`, `DownsideDeviationCalculation` | Downside deviation uses population divisor and negative excess returns | Keep if product confirms; define no-downside behavior |
| Max drawdown/MAR | `MaxDrawdownService`, `MarRatioCalculationService` | Drawdown service logic, MAR uses return divided by absolute drawdown | Add zero-drawdown behavior tests |
| Alpha/beta | `AlphaBetaCalculationAbstract`, `AlphaCalculation`, `BetaCalculation` | Excess-return CAPM style alpha/beta | Add zero benchmark variance guard and date alignment |
| R-squared | `RSquaredCalculationAbstract` | Non-standard `1 - Σ(p-b)² / Σ(p-avgP)²` | Fix formula |
| Correlation | `CorrelationCalculation` | Holding-to-holding component correlation using pre-centered holding series | Keep semantics clear; do not reuse for benchmark raw series |
| Rolling correlation | `RollingCorrelationCalculation` | Raw portfolio/benchmark maps delegated to centered-series helper | Fix to true Pearson per window |
| Tracking error | `TrackingErrorCalculation` | Sample standard deviation of active returns | Remove missing benchmark -> zero default |
| Information ratio | `InformationRatioCalculation` | Active trailing return divided by tracking error | Add zero/null tracking-error guard and alignment |
| Treynor | `TreynorRatioCalculation` | Arithmetic annualized risk premium divided by beta | Add zero-beta guard |
| Capture ratios | `UpDownSideCalculationAbstract` | Per-month geometric mean ratio over positive/negative benchmark months | Remove missing portfolio -> +100% default; decide basis |
| Asset allocation | `AbstractAssetAllocationService`, `PortfolioWeightCalculator` | FX-normalized weights in `perform(...)`; raw weights in `calculate(...)` | Enforce explicit currency policy |
| Other breakdown allocations | `BreakdownAbstractService`, `AllocationHelper`, specific services/mappers | Mostly raw-value weighted net products with mapper-specific normalization | Decide currency policy per family |
| MER/management fee/fees | `AbstractFeeCalculationService`, concrete fee services | FX-converted market values; mode-specific averages/sums | Keep; test missing currency/rate by mode |
| Sales charge | `SalesChargeCalculation` | Raw value ratios by sales-charge category | Confirm raw basis or convert |
| Income forecast | `IncomeForecastCalculationServiceImpl` | Schedule-based amount generation; GIC rates divided by 100 | Define non-GIC units; inject clock |
| Yield | `YieldResponseMapper` | Raw-value weighted yield; GIC yield divided by 100; no data -> zero | Define units and no-data result |
| Unique holdings | `NumberOfUniqueHoldingsService` | Distinct configured identifier values plus null-id holdings | Add explicit no-identifier/null-id tests and warnings |
| Top common holdings | `CommonHoldingsService` | FX-normalized parent weights, recursive leaf expansion, cycle/depth guards | Add cycle/depth/missing-weight tests |
| Common performance dates | `CommonPerformanceDateServiceImpl` | Separate portfolio and benchmark common ranges | Add overlap/intersection warning/field |

## 8. Suggested implementation sequence

1. Add shared aligned-series and denominator-guard utilities with independent unit tests.
2. Fix rolling correlation using the new Pearson helper.
3. Fix R-squared using the same aligned Pearson/regression basis.
4. Remove capture and tracking-error synthetic defaults.
5. Add warnings/null behavior for zero denominators across beta, R², correlation, information ratio, and Treynor.
6. Add high-risk independent unit tests before broad refactors.
7. Add representative E2E scenarios for benchmark/risk-free/capture families.
8. Implement explicit currency policy strategies and update affected allocation/yield/sales-charge tests.
9. Define distribution and income/yield edge contracts.
10. Clean up constructor switches, clock usage, and warning consistency.

## 9. Acceptance checklist

Before calculation output is considered reliable for provider comparison:

- [ ] R-squared is fixed and cannot produce negative values for valid non-degenerate inputs.
- [ ] Rolling correlation computes de-meaned Pearson correlation per window.
- [ ] Two-series and three-series metrics enforce exact date alignment.
- [ ] Capture ratios do not use synthetic +100% portfolio returns for missing dates.
- [ ] Tracking error does not default missing benchmark returns to zero.
- [ ] Beta, R², correlation, information ratio, and Treynor have documented zero-denominator behavior.
- [ ] T-Bill requirements are explicit at metric boundaries.
- [ ] Currency policy is encoded and tested per metric family.
- [ ] Distribution edge cases are defined and tested.
- [ ] Income/yield units and no-data behavior are defined and tested.
- [ ] High-risk metrics have independent-oracle unit tests.
- [ ] High-risk ratio families have representative E2E coverage through the REST boundary.
