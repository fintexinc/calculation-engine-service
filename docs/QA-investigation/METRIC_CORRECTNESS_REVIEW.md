# Calculation Engine — Metric Correctness Review

Financial and mathematical review of the metric formulas as **implemented in code**.
Findings are ranked by severity. Two items (R-squared, capture ratios) were verified
directly against source; the rest are assessed against the documented formulas.

See also: [`METRIC_FORMULAS.md`](./METRIC_FORMULAS.md) (formulas) and
[`METRIC_TESTING_STRATEGY.md`](./METRIC_TESTING_STRATEGY.md) (test scenarios).

**Bottom line:** the return, risk, risk-adjusted, regression-input (α/β), and
composition/fee metrics are methodologically correct. **R-squared is a genuine bug.**
**Capture ratios** use a non-standard basis and need a product-spec check.

---

## 🔴 HIGH — R-squared is mathematically incorrect

**Source:** `application/.../calculation/metric/core/RSquaredCalculationAbstract.java:53–93`
(verified).

**Implemented:**

```
R² = 1 − SSR/TSS
  SSR = Σ(rpᵢ − rbᵢ)²        // portfolio excess − benchmark excess, per month
  TSS = Σ(rpᵢ − μp)²
```

**Why it's wrong.** The coefficient of determination `1 − SSR/TSS` is valid only when
`SSR` is the residual sum of squares from the **fitted** regression line, i.e.
`SSR = Σ(rpᵢ − (α + β·rbᵢ))²`. This code uses `Σ(rpᵢ − rbᵢ)²`, which is the residual
**only if α=0 and β=1** — i.e. it assumes the portfolio should exactly equal the
benchmark 1:1. In finance, R² is universally defined as the **square of the correlation**
between portfolio and benchmark (`R² = ρ²`) and must lie in `[0, 1]`.

Consequences:

- **It can go negative.** If the portfolio deviates enough, `SSR > TSS` → `R² < 0`,
  which is nonsensical for "proportion of variance explained."
- **It understates R² whenever β ≠ 1.** Example: portfolio = exactly 2× benchmark
  (perfect correlation, β=2). True R² = 1.0. This formula gives ≈ 0.75 or worse, because
  `rp − rb = rb` inflates SSR.
- **It is internally inconsistent with the `correlation` metric.** Under the correct
  definition `R² = correlation²`. A client computing both will find they don't reconcile
  — mathematically impossible under the standard definition, and a clear tell of the bug.

**Suggested fix.** Compute `R² = ρ²` (reuse `CorrelationCalculation`), or keep the
regression form but use the true residual `Σ(rpᵢ − (α + β·rbᵢ))²` with the fitted α, β
already computed for alpha/beta. Add a test asserting `R² == correlation²`.
Secondary: standard R² uses **raw** returns, not excess returns.

---

## 🟡 MEDIUM — Capture ratios use a non-standard basis

**Source:** `application/.../calculation/metric/core/UpDownSideCalculationAbstract.java:68–92`
(verified).

**Implemented** — ratio of **per-month geometric-mean** returns:

```
capture = [ (∏(1+rpᵢ))^(1/n) − 1 ] / [ (∏(1+rbᵢ))^(1/n) − 1 ] × 100
          (up-capture: months where rb>0 ; down-capture: months where rb<0)
```

The prevailing industry (Morningstar) definition uses the ratio of **cumulative
compounded** returns over the qualifying months:

```
capture_standard = [∏(1+rpᵢ) − 1] / [∏(1+rbᵢ) − 1] × 100
```

These are **not equal**. Example — portfolio +2%/mo, benchmark +1%/mo, 10 up months:

- Cumulative (standard): `(1.02¹⁰−1)/(1.01¹⁰−1)` = 0.219/0.105 ≈ **209%**
- Geometric-mean (code): `0.02/0.01` = **200%**

Not a math error — it's a coherent alternative — but it deviates from what most
consumers expect. **Verify against the product spec.** If the spec follows Morningstar,
this is a bug.

Two smaller quirks in the same method:

- A **missing portfolio return** inside a qualifying month defaults to `+100%`
  (factor 2.0, line 54). Guarded by the size check for the dummy tail (entries 101–180),
  but a genuine gap *within* the valid window would silently bias the ratio. Add a
  targeted test.
- `benchmarkDeviation == 0 → returns 0` (line 77–78) rather than null; an all-flat
  benchmark window yields 0% capture, which is debatable.

---

## 🟢 LOW — Methodology notes (not errors, worth documenting)

- **Downside deviation divisor is `n`** (all observations), not the count of downside
  months — this is the **correct** canonical Sortino definition. The target is the
  **risk-free rate** (not 0 or a MAR); a valid convention, but should be stated since it
  means Sortino and a "downside-vs-0" reading differ.
- **Sharpe / Information-ratio return basis.** Sharpe's numerator is the **arithmetic**
  annualized mean excess return (`×12`) over the **sample** stdev (`×√12`) of excess
  returns — textbook-correct. Note the return basis is arithmetic while the headline
  trailing return is geometric (CAGR); intentional, not a bug. Information ratio mixes a
  geometric annualized-return difference (numerator) with an arithmetic-based tracking
  error (denominator) — common in practice, minor inconsistency.
- **Distribution bins = ⌊√count⌋** is a heuristic (square-root choice), a modelling
  decision rather than a correctness question.

---

## ✅ Verified sound (financially and mathematically standard)

| Metric | Verdict |
|---|---|
| Trailing / leading / rolling total returns | ✓ Cumulative <1yr, geometric-annualized (CAGR) ≥1yr — Morningstar-standard |
| Annual returns, Growth of $10K | ✓ Correct compounding |
| Standard deviation | ✓ Sample (`n−1`), `×√12` annualization |
| Sharpe, Sortino | ✓ Correct structure (see LOW notes on basis) |
| Downside deviation | ✓ Divisor `n` is the correct Sortino convention |
| Max drawdown + recovery | ✓ Correct peak-to-trough |
| MAR ratio | ✓ CAGR / \|MaxDD\| |
| Beta | ✓ `Cov(excess_p, excess_b)/Var(excess_b)` — CAPM form, `n` cancels |
| Alpha | ✓ Jensen's alpha (excess form), arithmetically annualized |
| Correlation | ✓ Standard Pearson |
| Tracking error | ✓ Sample (`n−1`) stdev of active returns, `×√12` |
| Treynor, Excess returns | ✓ Standard |
| Allocations / exposures | ✓ Value-weighted, FX-converted before weighting, denoise — sound |
| MER / Management fee / Fees / Yield | ✓ Value-weighted aggregation — sound |

---

## Recommended follow-ups

1. **Fix R-squared** (HIGH) — switch to `ρ²` or fitted-regression residuals; add a test
   asserting `R² == correlation²` and that `R² ∈ [0, 1]`.
2. **Confirm capture-ratio basis against the product spec** (MEDIUM) — geometric-mean vs.
   cumulative compounded; align if Morningstar parity is required.
3. **Add targeted tests** for the capture-ratio missing-return default and the
   zero-benchmark-deviation branch.
4. **Document conventions** (LOW) — downside-deviation target, Sharpe arithmetic basis —
   so reviewers don't mistake them for defects.
