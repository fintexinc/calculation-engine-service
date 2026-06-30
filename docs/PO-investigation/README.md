# PO Investigation — Completeness Assessment

Independent completeness review of **Security Master Service v2** (data provider) and the
**Calculation Engine Service** (analytics consumer), benchmarked against Polygon, FMP, GIPS,
and Addepar.

## Contents
- **`ASSESSMENT.md`** — the deliverable: executive summary, two completeness matrices,
  per-dimension gap analysis, integration findings, prioritized roadmap, open questions.
- `00-peer-benchmarks-reference.md` — peer/standard baselines used for benchmarking.
- `01-calc-engine-findings.md` — raw codebase findings: 50-metric catalog + methodology.
- `02-sms-findings.md` — raw codebase findings: API surface, schema, providers, reconciliation,
  data quality.
- **`03-competitive-comparison.md`** — competitive evaluation of the Calculation Engine Service
  vs. Addepar and BlackRock Aladdin (positioning, capability matrix, head-to-head, differentiation).

## Headline conclusion
The pair is well-matched for **Canadian/US funds & ETFs** but has high-severity gaps for
**direct equities, multi-currency, point-in-time/historical reporting, and cash-flow-aware
client returns**. Top three: (1) engine is TWR-only with no money-weighted return; (2) SMS has
no corporate-action feed and discards FMP dividend history; (3) SMS overwrites data on import
(no point-in-time → survivorship/look-ahead bias). See `ASSESSMENT.md` §1.

> Findings are from a static codebase read. Several "quality" judgments need a sample response
> or null-rate data to confirm — see `ASSESSMENT.md` §6.
