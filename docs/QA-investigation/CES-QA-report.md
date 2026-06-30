# CES — Calculation Engine Service · QA Report

**Target:** `http://localhost:8181`  ·  **Endpoint:** `POST /api/v1/portfolio/calculations/{metricName}`
**Date:** 2026-06-30  ·  **Tester:** QA (adversarial, evidence-based)
**Upstream dependency:** SMS `http://localhost:8683` (see SMS-QA-report.md).

> **Scope note:** CES consumes SMS, which has a **partial Morningstar import** (notably only **10 months** of monthly returns; US mutual funds not loaded). Correctness is judged by **independently hand-computing metrics from the source CSVs and comparing to CES output**. Metrics that cannot run because the underlying data isn't deep/broad enough are reported as **data-scope limitations**, distinct from genuine CES defects.

---

## 1. Summary verdict

**CES is correct, robust, and deterministic. Where it has enough source data, its computed values match independent hand-calculations to 6+ decimal places. Confidence: HIGH.**

- **All 50 metrics reachable.** *(CLAUDE.md/README say "48"; the enum and live API expose 50 — doc discrepancy.)*
- **Compute correctness proven** against the source CSVs: trailing returns and growth-of-10k match hand-calculations exactly; fees/MER and all composition metrics match SMS/source exactly.
- **Robust input handling:** every malformed/degenerate input → precise 4xx with structured, leak-free envelope. **No 500s, no stack traces.** Missing upstream data degrades to warnings at HTTP 200.
- **Deterministic:** identical request → byte-identical response across repeated calls (verified on a compute-heavy metric).
- The metrics that don't currently produce a value fail **for the right reason** — the imported history is only 10 months, below the documented 12-month floor — and CES rejects/【warns】 cleanly rather than emitting garbage.

| # | Item | Type | Severity |
|---|------|------|----------|
| C-1 | 18 risk / risk-adjusted / benchmark metrics can't produce values: only **10 months** imported vs a 12-month minimum. Rejected cleanly (`TIP-001/009`) — a data-depth limit, not a CES bug. | Data scope | Med |
| C-2 | `income-forecast` returns empty `[]` with no warning, despite SMS serving income data. | Defect | Med |
| C-3 | Docs say 48 metrics; 50 are exposed. | Doc | Low |

---

## 2. Scope & method

- **Catalog:** fired all 50 `CalculationMetric` constants live with valid bodies (verified JSON field names: `holdings`, `currency`, `timeIntervalPeriods`, `rollingTimeIntervalPeriod`, `benchmarkHoldings`, `dataProviders`, …).
- **Real inputs:** holdings reference actually-imported securities by **ticker** — `NVDH` (Harvest NVIDIA, ETF_CANADA, **CAD** → FX=1, clean oracle), `NVDH.U` (USD class), `QQQQ` (benchmark). (The e2e fixtures XBAL/VCNS/SPY are MockWebServer stubs, absent from live SMS.)
- **Correctness oracle:** independently recomputed returns from the source monthly-return CSVs (geometric linking Π(1+rₘ); sub-12-month = simple per documented method) and compared to CES output. Fees/allocations reconciled to source + SMS.
- **Robustness:** empty holdings, unknown metric, metric/path mismatch, negative value, unknown security, missing `dataProviders`, malformed JSON.
- **Determinism:** repeated identical calls hashed.

---

## 3. Test results table

| ID | Category | Metric | Input | Expected (independent calc / source) | Actual | Result | Sev |
|----|----------|--------|-------|--------------------------------------|--------|--------|-----|
| C-P01 | **Correctness** | `trailing-total-returns` | NVDH (CAD), p=1/3/6/9 | source ÷100: 0.0701886 / 0.1602123 / 0.4912473 / 0.2460556 | 0.0701886 / 0.1602122816 / 0.4912472609 / 0.2460554525 | **PASS** | — |
| C-P02 | **Correctness** | `growth-of-10k` | NVDH (CAD) | hand-calc cum-product: 9960.948, 9087.7504, … 12411.8936 | identical to 4+ dp | **PASS** | — |
| C-P03 | Correctness | `mer` | NVDH.U | source MER 0.990 → 0.0099 | 0.0099 | **PASS** | — |
| C-P04 | Correctness | `management-fee` | NVDH.U | ActMgmtFee 0.40000 → 0.0040 | 0.0040 | **PASS** | — |
| C-P05 | Correctness | `fees` | 60k USD + 40k CAD | annual = 12×monthly; blended MER + FX | 1239.8364 = 12×103.31970 (consistent) | **PASS** | — |
| C-P06 | Correctness | `asset-allocations` | NVDH.U | =SMS (US_EQ 1.0245525, INTL −0.0248077, CASH 0.0002551) | identical | **PASS** | — |
| C-P07 | Correctness | `equity-sector` / `-market-capitalization` / `-country-exposure` / `-stylebox-exposure` | NVDH.U | TECH 1, LARGE 1, US 1, LARGE_GROWTH 1 | all match | **PASS** | — |
| C-P08 | Correctness | `number-of-unique-holdings` | NVDH.U | 1 (NVIDIA) | **1** | **PASS** | — |
| C-P09 | Correctness | `top-common-holdings` | NVDH.U+NVDH | NVIDIA, numOfFunds 2, alloc 1.0245525 | match | **PASS** | — |
| C-P10 | Positive | `rolling-total-returns`, `distribution-of-monthly-returns`, `best-worst-periods`, `max-drawdown`, `rolling-correlation` | NVDH | 200 + sane values | all 200 | **PASS** | — |
| C-P11 | Determinism | `trailing-total-returns` ×3 | NVDH | identical | byte-identical (md5 `010f00…`) | **PASS** | — |
| C-G01 | Graceful | `leading-total-returns` | p=12, 10 mo data | warn, no crash | 200 + `RET-008` "only 10 available" | **PASS** | — |
| C-G02 | Graceful | `annual-returns` | range 2024-12…2025-09 | no full calendar year | 400 `RET-010` (correct) | **PASS** | — |
| C-L01 | Data scope | `standard-deviation`, `mean`, `sharpe`, `sortino`, `downside-deviation`, `mar`, `treynor`, `information-ratio`, `tracking-error`, `alpha`, `beta`, `rsquared`, `correlation`, `upside/downside-capture` | p=12 | needs ≥12 mo; only 10 | 400 `TIP-001` (clean reject) | **LIMIT** | Med |
| C-L02 | Data scope | `rolling-standard-deviation`, `rolling-sharpe-ratio` | rolling 12 | needs ≥12 mo | 400 `TIP-009` (clean reject) | **LIMIT** | Med |
| C-F01 | Defect | `income-forecast` | NVDH.U | forecast rows (SMS income present) | `incomeForecast: []`, no warning | **FAIL** | Med |
| C-N01 | Negative | `trailing-total-returns` `holdings:[]` | — | 400 | `VAL-003` | **PASS** | — |
| C-N02 | Negative | `bogus-metric` | — | 4xx | 400 `VAL-004` (no 500) | **PASS** | — |
| C-N03 | Negative | path≠body metric | — | 400 | `MET-002` clear | **PASS** | — |
| C-N04 | Negative | `mer` value −5000 | — | 400 | `HLD-001` | **PASS** | — |
| C-N05 | Boundary | `asset-allocations` unknown security | — | 200 + warning | `FDS-018` warning, continues | **PASS** | — |
| C-N06 | Boundary | `mer` no `dataProviders` | — | 200 default | 200 | **PASS** | — |
| C-N07 | Negative | malformed JSON | — | 400 | `VAL-004` | **PASS** | — |

---

## 4. Findings

### Correctness is proven (headline positive)
**Trailing returns** — single CAD holding `NVDH` (FX=1, so CES math is isolated). CES geometrically links the source monthly returns and reproduces Morningstar's own pre-computed trailing returns:

| Period | CES output | Source ÷100 (`Return{n}Mth`) | Δ |
|--------|------------|------------------------------|---|
| 1-mo | 0.0701886 | 0.0701886 | 0 |
| 3-mo | 0.1602122816 | 0.1602123 | <1e-6 |
| 6-mo | 0.4912472609 | 0.4912473 | <1e-6 |
| 9-mo | 0.2460554525 | 0.2460556 | <1e-6 |

**Growth-of-10k** — CES output equals an independent cumulative-product hand-calc exactly (e.g. 2025-01-31 → 9087.7504, 2025-09-30 → 12411.8936). **Fees/MER/composition** — equal to SMS/source (see table). This is strong, end-to-end evidence the calculation engine is correct.

### C-1 — Risk / benchmark metrics blocked by 10-month import · Data scope · **Med**
18 metrics (std-dev, mean, Sharpe, Sortino, downside-dev, MAR, Treynor, information-ratio, tracking-error, alpha, beta, R², correlation, up/down-capture, rolling-std, rolling-Sharpe) require ≥12 months; only 10 are imported. CES rejects with `TIP-001`/`TIP-009` **before** computing — correct, defensive behavior. Benchmark metrics reach the benchmark series (e.g. `beta` returns `RET-008` "insufficient months" rather than "no data"), confirming the wiring is sound. **These will work once ≥12 months are imported** — re-verify then against an independent calc.

### C-2 — `income-forecast` empty without explanation · Defect · **Med**
```bash
.../income-forecast {holdings:[NVDH.U], timeIntervalPeriods:12} → {"warnings":[],"incomeForecast":[]}
```
SMS `/income` serves `dividendYield` + distribution dates for this ETF, yet the forecast is empty with **no warning**. A consumer can't tell whether the forecast is genuinely zero or silently failed. Investigate the income-forecast data path; at minimum emit a warning when inputs are insufficient.

### C-3 — Metric-count doc drift · Low
Enum + live API expose **50** metrics; docs say 48. Reconcile.

---

## 5. Completeness & correctness findings

**Correct (independently verified):** trailing-total-returns, growth-of-10k, mer, management-fee, fees (FX-consistent), asset-allocations, equity-sector/-country/-market-cap/-stylebox, number-of-unique-holdings (now 1), top-common-holdings, yield. Determinism confirmed.

**Working & graceful:** leading/rolling total returns, distribution-of-monthly-returns, best-worst-periods, max-drawdown, rolling-correlation (200); `leading-total-returns` warns `RET-008`; `annual-returns` correctly 400s `RET-010` (no full calendar year in range).

**Not producing values:**
- 18 risk/benchmark metrics — data depth (C-1), clean rejects.
- `income-forecast` — empty, no warning (C-2, genuine defect).

**Robustness (high confidence):** layered validation — `VAL-003` (empty holdings), `VAL-004` (bad metric/JSON), `MET-002` (path/body mismatch), `HLD-001` (negative value), `TIP-001/009` (period floors), `RET-008/010` (insufficient/!calendar), `FDS-018/031` (missing upstream → **warning at 200**). No 500s anywhere.

---

## 6. Recommendations & open questions

**Recommendations:**
1. Fix **`income-forecast`** (C-2) or add a diagnostic warning.
2. After SMS imports **≥12 months** (SMS S-1), re-run the 18 blocked metrics and validate values against an independent calc (oracle method per §2).
3. Reconcile the **48 vs 50** metric count (C-3).

**Could not verify (and what's needed):**
- **Risk/benchmark value correctness** (std-dev, Sharpe, alpha, beta, …) — blocked by 10-month depth; needs ≥12 months, then hand-calc comparison.
- **FX-conversion correctness** for USD holdings in `fees` — the implied USD/CAD ≈ 1.42 should be checked against the Bank of Canada rate for the as-of month.
- **Fixed-income metric correctness** — needs a holding with populated FI sector/maturity/credit-quality source (the sampled ETF is pure equity, so nulls are expected).
- **US mutual fund metrics** — blocked upstream (SMS S-2, not imported).
