# Calculation Engine — Metric Testing Strategy

How to test the ~48 portfolio metrics: which scenario types to cover, where each test
lives, and how to avoid duplicating near-identical tests across metrics that share
structure. Aligns with the repo's `testing-guideline` skill and `CONTRIBUTING.md`.

See also: [`METRIC_FORMULAS.md`](METRIC_FORMULAS.md) for the formula each metric implements.

---

## 1. Which layer does each scenario belong to?

Keep the pyramid: **many unit, fewer integration, fewest e2e.**

| Layer | Spring | Mocks | Tag | What to test here | Metric coverage |
|---|---|---|---|---|---|
| **Unit** | ❌ | ✅ (ports) | none | Pure math in `calculation/metric/*` and orchestration in `calculation/service/*`. **Bulk of scenario coverage lives here.** | Every formula: exact numeric outputs, null guards, divisors, period resolution. |
| **Integration** | ✅ | ❌ | `@Tag("integration")` | SMS web-client adapter — deserialization, Resilience4j, malformed/empty SMS payloads. Uses WireMock. | Data-fetch failure modes, not the math. |
| **E2E** | ✅ | ❌ | `@Tag("e2e")` | Full `POST /calculations/{metric}` boundary. **Prefer 1 complex, diverse request** per metric family (mixed security types, currencies, identifiers, periods). Uses WireMock for SMS. | Contract + one realistic end-to-end pass per metric family. |

Unit tests must not touch Spring, network, DB, or filesystem. Integration/e2e must not
use mocks or `@MockBean`.

---

## 2. Scenario taxonomy (tailored to these metrics)

### 2.1 Positive / happy path
Golden-value tests with hand-verified expected numbers — assert **specific expected
values**, not just "not null".

- Multi-year return series → assert the exact annualized `(∏(1+rᵢ))^(12/n) − 1`.
- Known covariance dataset → assert beta / alpha / R² to scale.
- Mixed-currency holdings → assert FX-weighted allocation buckets sum to 1.0.

### 2.2 Negative / error path
Driven by the actual guards in the code:

- **Division-by-zero returns** — Sharpe (`stdev = 0`), MAR (`maxDD = 0`),
  correlation / beta (zero-variance benchmark) → assert `null`, not an exception.
- **Missing benchmark** for benchmark-only metrics (excess returns, treynor, capture…)
  → assert the proper warning / notification code.
- **Missing T-Bill rate** — Sharpe throws `MISSING_TBILL_RATE`; rolling-Sharpe
  *degrades that window to null*. Test both behaviors.
- **Missing currency on a fee-bearing holding** → hard error, whereas an allocation gap
  → `UNCLASSIFIED` bucket + warning. These asymmetric error policies each need a test.
- **Unsupported metric name** → `UNSUPPORTED_METRIC`.

### 2.3 Boundary values
Where the formulas change behavior:

- **The 12-month threshold** — `n=11` vs `n=12`: trailing returns switch from raw
  product to annualized; mean / std-dev / benchmark metrics require the 12-month
  minimum. Test `11, 12, 13`.
- **Divisor edges** — `n=1` (std dev `n−1=0`), `n=0` / empty series, single return.
- **Period resolution** — YTD at Jan boundary, SINCE_INCEPTION, PSD/PED trimming,
  CPSD/CPED alignment across holdings.
- **Denoise threshold** — allocation values just above / below `1e-5`.
- **Sign boundaries** — a month exactly `0` for upside (`rᵦ>0`) vs downside (`rᵦ<0`)
  capture filters; all-up series (downside capture has *no* qualifying months) and
  all-down series (upside capture empty).
- **Drawdown** — monotonically rising series (drawdown = 0, no trough); never-recovers
  case (recovery time = null).

### 2.4 Data-quality / degradation (SMS is the sole data source)
- Null / zero holding values, null yields, null sector / allocation data.
- Partial return series, gaps mid-series, misaligned portfolio vs benchmark dates.
- Fund recursion: max-depth hit, cyclic fund graph, missing underlying holdings
  (top-common-holdings).
- Every SMS-data-gap bug fix (e.g. equity-sector warnings, max-drawdown null/zero
  handling) should carry a regression test in this category.

### 2.5 Property / invariant checks (ideal for `@ParameterizedTest`)
- Allocation / exposure buckets **sum to ~1.0** (post-denoise) across arbitrary inputs.
- Capture ratios and weighted fees are currency-invariant given consistent FX.
- Weighted-average yield ∈ [min holding yield, max holding yield].
- Rolling metric window count = `series length − window + 1`.

---

## 3. Avoiding duplication across 48 metrics (critical)

These metrics share structure, so **do not write 48 near-identical test classes.**

- **`@ParameterizedTest` + `@MethodSource`** for "same formula, different return series →
  different expected value" — ideal for the boundary matrix (`n = 1, 11, 12, 60`).
- **Template-method abstract test classes** for families sharing a base:
  - The ~15 allocation/exposure services all extend `BreakdownAbstractService`, so an
    `AbstractBreakdownServiceTest` holds the shared scenarios (sum-to-1, denoise,
    UNCLASSIFIED, FX weighting) and each concrete test supplies only the bucket source
    and expected map.
  - Benchmark metrics (`PeriodBenchmarkAbstractService`) and rolling metrics
    (`RollingAbstractCalculation`) follow the same pattern.
- **Rolling metrics** — test window-slicing once in the abstract base; per-metric tests
  only assert the per-window value.

---

## 4. Minimum coverage matrix per metric

For each calculation, the minimum viable unit set is roughly six cases — most expressible
as parameterized rows — plus one diverse e2e per family:

| Scenario | Example assertion |
|---|---|
| Happy path | exact expected numeric value |
| Zero-denominator | returns `null` |
| Below-minimum period (`n<12`) | correct null / skip behavior |
| Empty / single-element series | no crash, defined output |
| Negative & all-negative returns | correct sign, downside handling |
| Missing dependency (benchmark / T-Bill / currency) | correct warning code or error |

---

## 5. Assertion quality (from the skill)

- **Unit tests:** many assertions — validate all important response fields with specific
  expected values: PSD/PED, warning codes, key numeric outputs, and normalization
  invariants (sums, weights, map keys).
- **E2E tests:** build requests and SMS responses from DTOs/domain classes (never raw
  JSON strings), serialize with the repo `ObjectMapper`, parse responses to typed DTOs
  (never `JsonNode`), and assert important fields with specific expected values.

---

## 6. PR gate

Before opening a PR:

- Run the relevant test suites locally (`mvn test`, or `-Dtag=integration` / `-Dtag=e2e`).
- Run the `code-reviewer` skill against the change set (especially adapters, pipelines,
  validators).
