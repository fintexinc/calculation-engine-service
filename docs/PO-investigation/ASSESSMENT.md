# Completeness Assessment — Security Master Service v2 & Calculation Engine Service

**Audience/use case:** retail investing app, robo-advisor, wealth/PFM (Canada-first, US coverage).
**Date:** 2026-06-30. **Author:** PO / data-product review.
**Method:** read-only codebase scan of both repos (findings in `01-…` and `02-…`),
benchmarked against Polygon, FMP, GIPS, and Addepar (reference in `00-…`).

A recurring theme: many fields are **present in the schema** but their **reliable
population and correctness is unverified or known-weak**. I flag this distinction throughout.

---

## 1. Executive summary — most material gaps, ranked by impact

1. **The engine ignores cash flows (TWR-only, no MWR/IRR).** [HIGH]
   For a robo-advisor where clients deposit/withdraw on a schedule, a pure time-weighted
   geometric return is *not* the dollar-weighted return the client actually experienced.
   There is no Modified Dietz, no IRR/XIRR, no money-weighted return anywhere in the 50
   metrics. Consequence: the headline "your return" number can diverge materially from the
   client's real gain/loss, which is a trust and (in some jurisdictions) a suitability/
   disclosure problem.

2. **No corporate-action feed in SMS, and direct equities have no usable return series.** [HIGH]
   SMS stores **zero** splits/mergers/spin-offs and only an aggregate dividend yield (the
   detailed FMP dividend history is fetched then thrown away). The engine assumes SMS hands
   it split/dividend-adjusted *total-return* monthly factors. That assumption holds for
   Morningstar funds/ETFs (Morningstar pre-adjusts), but **FMP — the only source of stock
   data — is disabled by default and never produces a monthly-return series.** Net effect:
   portfolios holding **individual stocks cannot be analyzed** for returns/risk, and for
   funds the engine is blindly trusting an adjustment it can neither see nor validate.

3. **SMS overwrites data on every import — no point-in-time history.** [HIGH]
   Each nightly Morningstar import replaces the prior record; `DataVersion` exists but isn't
   used bitemporally. Securities dropped from the Morningstar file simply vanish. Consequence:
   **survivorship bias** (failed/merged funds disappear), **look-ahead bias** (reference data
   silently changes under historical windows), and **non-reproducibility** — you cannot
   regenerate the exact report a client saw last quarter. This is below the institutional
   security-master bar (Bloomberg/Refinitiv keep a versioned golden copy).

4. **Multi-provider reconciliation is record-level only — no field-level provenance, no
   conflict detection.** [HIGH]
   The merge keeps a per-record `List<DataProvider>` and does a "first provider wins, others
   fill nulls" shallow merge. When Morningstar and FMP disagree on an overlapping field
   (ticker, sector, beta, dividend yield, price), the loser is dropped **silently** — no flag,
   no confidence score, no reconciliation report. You cannot answer "where did this value come
   from?" per field, which undermines auditability and data-quality triage.

5. **Analytic surface is thin versus a wealth platform (Addepar).** [MED]
   Missing entirely: **performance attribution** (Brinson allocation/selection/interaction),
   **contribution to return / to risk**, **VaR / CVaR (expected shortfall)**, **portfolio-level
   fixed-income analytics** (duration, convexity, YTM — SMS has per-security duration/YTM but
   the engine exposes no portfolio roll-up), **currency-exposure / FX attribution**,
   **after-tax return / tax drag**, and any real **Monte-Carlo / goal-probability projection**
   (the "forecasts" category is just income-forecast + a naive leading-return + yield).

6. **Currency handling is narrow and CAD-locked.** [MED]
   Only 8 `*→CAD` pairs exist; a holding priced in any other currency triggers an
   `FX_RATES_UNAVAILABLE` warning and is **left unconverted**, silently distorting fee and
   allocation aggregates. Reporting currency is hardcoded to CAD — no per-request base
   currency, no multi-currency reporting, no FX-impact isolation.

7. **SMS has no real market-data tier.** [MED]
   No OHLCV bars, no bid/ask, no intraday, no tick, no streaming — daily EOD batch only, and
   no FIGI identifier, no delisting/status, no symbol-change history. Acceptable for an
   EOD-valued robo, but it rules out intraday valuation, trading, or any latency-sensitive
   use, and the missing lifecycle/status data feeds directly into the survivorship problem.

8. **Risk-adjusted metrics depend on a treasury-rates endpoint not found in the SMS scan.** [VERIFY/HIGH]
   The engine hard-fails Sharpe/Sortino/Treynor/alpha/etc. if any month's risk-free rate is
   missing, and it sources those from `/api/v1/wealth/reference/treasury-rates`. That route did
   **not** appear in the SMS securities-controller scan. If SMS does not reliably serve
   per-currency T-bill rates, the entire risk-adjusted category breaks. Needs confirmation.

---

## 2. Completeness matrices

Columns: **Present?** (in schema/code) · **Quality** (reliably populated/correct?) ·
**Peer-typical?** (do named peers do more?) · **Severity** (gap impact on the use case).

### 2A. Security Master Service v2

| Dimension | Present? | Quality | Peer-typical? | Severity & what peers provide |
|---|---|---|---|---|
| **Asset-class breadth** | Yes — ETFs, MFs, pooled/seg/hedge funds, GIC, SMA, index, stocks; US+CA only | Good for CA/US funds; stocks weak (FMP off) | Partial. FMP/Polygon add options, FX, crypto, broad intl equities | **Med** — no intl/EM direct securities; fine if Canada-first, limiting otherwise |
| **Identifiers (ISIN/CUSIP/SEDOL/FIGI/ticker+MIC)** | Ticker, TICKER_MIC, CUSIP, ISIN, SEDOL, FUNDSERV, CIK, MStarID. **No FIGI** | ISIN/CUSIP likely ok US/CA; SEDOL/CUSIP intl unverified | Below. Polygon exposes composite/share-class FIGI; OpenFIGI is free | **Med** — no FIGI cross-ref complicates mapping & dedup |
| **Classification (sector/GICS)** | Yes — 11 Morningstar sectors, style box, category | Good for funds; sparse for stocks; engine sees nulls (defaults to "unclassified") | Comparable (Morningstar taxonomy, not GICS-labelled) | **Low/Med** — not GICS-aligned; null sectors degrade allocation metrics |
| **Lifecycle / status (active, delisted, symbol change)** | **No** status flag, no delisting, no symbol-change history | N/A | Below. Polygon ticker-events; FMP delisting feed | **High** — drives survivorship bias downstream |
| **Market & pricing (OHLCV, bid/ask, intraday, tick)** | NAV (funds), last close + 52wk + volume (ETF/stock). **No OHLCV/bid-ask/intraday/tick** | EOD only, daily batch | Below. Polygon = tick/NBBO/aggregates/WebSocket; FMP intraday | **Med** — blocks intraday/trading; ok for EOD robo |
| **Historical depth & adjustment** | Monthly returns ≤20yr; daily price history limited; **no split/div adjustment in SMS** | Relies on Morningstar pre-adjusted returns; stocks unadjusted | Below. Polygon/FMP expose adjusted+raw with a flag | **High** — unadjusted stock prices ⇒ wrong returns across splits |
| **Fundamentals (statements, ratios)** | FMP 82 ratios + profile (but FMP **off by default**); fund-level ratios from MStar. **No financial statements** | Ratios unpopulated unless FMP enabled | Below. FMP full statements+ratios+estimates; Polygon SEC financials | **Med** — no statements; ratios dormant |
| **Corporate actions (splits/div/M&A/spin-off)** | **None.** FMP dividend history fetched but discarded | N/A | Below. Polygon & FMP both have splits+dividends endpoints | **High** — see returns-drift consequence |
| **Multi-provider reconciliation** | Record-level provenance; first-wins null-fill merge | **No field provenance, no conflict detection, no consistency checks** | Below institutional norm (golden-copy, per-field lineage) | **High** — silent wrong values, no auditability |
| **Data quality (freshness, nulls, PIT, survivorship)** | Daily import; `DataVersion` exists | **Overwrite-in-place (no PIT), no null-rate/freshness monitoring** | Below. Bloomberg/Refinitiv = bitemporal, survivorship-free universes | **High** — survivorship + look-ahead + non-reproducible |
| **API surface (search/filter/page/bulk)** | Strong: typed batch endpoints, search+filter+sort, page pagination, OpenAPI | Good | Mostly comparable | **Low** — solid batch design |
| **API: streaming/webhooks/rate-limit/versioning** | No streaming/webhooks; no documented rate limits; v1-in-path only | — | Below. Polygon WebSocket; both document limits | **Low/Med** — fine for batch consumer (the engine) |

### 2B. Calculation Engine Service

| Dimension | Present? | Quality | Peer-typical? | Severity & what peers provide |
|---|---|---|---|---|
| **Returns catalog** | Trailing, rolling, calendar, growth-of-10k, mean, distribution, leading | Solid TWR; sub-year not annualized (GIPS-correct) | Partial. **No MWR/IRR** | **High** — Addepar shows TWR+IRR side-by-side |
| **Risk catalog** | Std-dev, downside-dev, max-drawdown, tracking-error, rolling std-dev | Good; max-DD is monthly-granular (understates) | Partial. **No VaR/CVaR/expected-shortfall, no semivariance beyond downside-dev** | **Med** — VaR/CVaR standard at peers |
| **Risk-adjusted catalog** | Sharpe, rolling-Sharpe, Sortino, Treynor, Information Ratio, MAR(=Calmar), alpha | Correct CAPM-style; Sortino threshold fixed to rf (not configurable MAR) | Mostly. Missing Omega, M² (minor) | **Low/Med** — solid core set |
| **Relative/benchmark** | Beta, R², correlation, rolling-corr, up/down capture, excess returns | Good | Missing batting-average/win-rate, multi-factor | **Low** — good single-benchmark coverage |
| **Composition / allocation** | 19 metrics: equity & FI sector/country/geo/stylebox/cap, asset alloc, maturity, credit-quality, classification | Depends on SMS coverage; nulls → "unclassified" | Comparable breadth | **Med** — no portfolio **duration/convexity/YTM**, no **currency exposure**, no concentration index (HHI) |
| **Attribution / contribution** | **None** | — | Below. Addepar = Brinson attribution + contribution-to-return | **Med/High** — a core wealth-analytics expectation |
| **Fees** | MER, management-fee, fees ($), sales-charge; FUNDS_ONLY / WHOLE_PORTFOLIO modes | Good; hard error on missing MER/currency | Comparable | **Low/Med** — **no net-of-fee return** (gross only) |
| **Forecasts** | income-forecast, yield, leading-total-returns | Leading return appears naive; no stochastic model | Below. Peers/robos offer **Monte-Carlo goal projection, glide paths** | **Med** — thin for a robo/PFM |
| **Methodology: return method** | TWR geometric monthly | No cash-flow handling | **No MWR/IRR/Modified Dietz** | **High** (see Exec #1) |
| **Methodology: annualization** | 12/yr, geometric; sub-year simple | Correct | Yes | **Low** |
| **Methodology: gross/net** | Gross only; fees separate | — | Below. GIPS wants both | **Med** — no net-of-fee toggle |
| **Methodology: FX** | CAD only, 8 pairs, BoC | Missing pair ⇒ unconverted (silent) | Below. Addepar multi-ccy + FX attribution | **Med** |
| **Methodology: lookback config** | Fixed 1/3/6/12/36/60/120 + YTD + SI + custom CIPSD; rolling 12/36/60/120 | Limited | Below. Peers allow arbitrary windows/date ranges | **Low/Med** |
| **Standards (GIPS)** | Partial-by-accident: TWR, geometric, no sub-year annualization | Not a compliance effort; no composite/dispersion, no net-of-fee | Below for institutional; ok for retail | **Med** if GIPS ever needed |
| **Edge cases** | Zero-denom guards, insufficient-data warnings, null-sector defaults, zero-value guards | Actively hardened (recent commits) | Comparable | **Low** |
| **Input requirements / sensitivity** | Heavy dependency on SMS (returns, allocations, fees, T-bills) | Hard-fails on missing T-bill/MER/currency; silent nulls elsewhere | — | **High** — see integration section |

---

## 3. Detailed gap analysis (each tied to a concrete consequence)

### Security Master

- **No corporate-action feed → returns drift after every split/large distribution.**
  With no split data and only an aggregate dividend yield, any direct-equity return computed
  from raw prices is wrong across a split boundary (e.g., a 4-for-1 split looks like a −75%
  "return"). For funds this is masked only because Morningstar pre-adjusts — an external
  dependency SMS neither validates nor can reproduce.

- **Overwrite-in-place → survivorship bias & non-reproducible history.** A fund that closed
  last month is gone from the file and thus the DB; any "since inception" or multi-year
  backtest computed today silently excludes failures, inflating average performance. A client
  statement generated in Q1 cannot be regenerated identically in Q2 — a compliance/audit risk.

- **Record-level-only provenance → silent wrong values, no triage path.** When the FMP price
  and Morningstar price disagree, support/ops cannot see which source won or by how much. No
  way to build a data-quality dashboard or set per-field source rules (e.g., "prefer FMP for
  live price, Morningstar for sector").

- **FMP disabled by default → stock fundamentals and stock returns effectively absent.**
  The richest part of FMP (82 ratios, dividend history, statements) is dormant; even the base
  stock import covers only US/CA tickers and produces no return series. Any portfolio with
  direct single-stock holdings is under-served end-to-end.

- **No FIGI / no lifecycle status → mapping friction & stale universes.** Cross-referencing to
  other systems (custodian, OMS) is harder without FIGI; without an active/delisted flag the
  app may surface tradeable UI for a delisted security.

- **No null-rate/freshness monitoring → silent degradation.** If Morningstar ships a file with
  10% of `dividendYield` null, nothing alerts; downstream yield/income metrics quietly drop
  holdings or mis-aggregate.

### Calculation Engine

- **TWR-only → reported return ≠ client's dollar experience.** A client who deposited a large
  sum right before a drawdown sees a TWR that looks far better than their actual money-weighted
  loss. Robo users with auto-deposits are exactly the cash-flow-heavy case TWR hides.

- **No cash-flow handling at all → results invalid for funded/withdrawn accounts mid-period.**
  The fixed-weight monthly model assumes no external flows; real accounts violate this monthly.

- **Monthly-granular max-drawdown → understated worst-case.** A peak-to-trough that occurs and
  recovers within a month is invisible; the reported max drawdown is optimistic versus a
  daily-valued peer.

- **No VaR/CVaR, no attribution, no contribution → can't answer "why" or "how bad."** Advisors
  expect "what drove return" (attribution) and "how much could I lose" (VaR/CVaR); neither
  exists. This is the clearest catalog gap versus Addepar.

- **No portfolio fixed-income analytics roll-up.** SMS supplies per-security duration/YTM/credit
  quality, but the engine exposes only allocation buckets — no portfolio duration, convexity, or
  yield-to-maturity. Bond-heavy portfolios get composition without the headline risk number.

- **CAD-only, 8-pair FX → distorted aggregates for off-list currencies.** A EUR- or
  emerging-market-priced holding outside the 8 pairs is left unconverted in fee/allocation sums,
  silently skewing portfolio totals; and everything is reported in CAD only.

- **Gross-of-fees only → overstated net experience and no GIPS-style dual presentation.** Sharpe/
  Sortino/returns don't reflect the fee drag the separate fee metric quantifies.

- **Fixed lookback windows → limited advisor flexibility.** No arbitrary "since I bought it" or
  custom date-range risk window beyond the CIPSD start hook.

---

## 4. Integration findings — input the engine needs vs. what SMS reliably provides

| Engine input need | SMS provision | Mismatch & downstream consequence | Severity |
|---|---|---|---|
| Split/dividend-adjusted **total-return monthly factors** | Morningstar pre-adjusted series for funds; **nothing usable for stocks** | Direct-equity portfolios can't be analyzed; fund returns trust an unvalidated external adjustment | **High** |
| **Corporate actions** (to validate/adjust) | **None** | Engine has no way to detect or correct an unadjusted series; a bad split slips straight into returns | **High** |
| Per-currency **risk-free / T-bill rates** (`/reference/treasury-rates`) | **Endpoint not found in SMS scan** | If absent, all risk-adjusted metrics hard-fail (engine throws on missing month) | **High / VERIFY** |
| **Multi-currency** return series in a consistent currency | SMS returns are in each security's base currency | Combining funds of different base currencies without converting the *return series* to a common ccy is a methodology error; recent FX-validation commit suggests partial awareness | **Med/High / VERIFY** |
| FX rates for **all** held currencies | BoC 8 pairs only (engine side) | Off-list currency ⇒ silent non-conversion ⇒ distorted fee/allocation totals | **Med** |
| **Look-through holdings** (funds-of-funds) | Single-level holdings only | Nested fund exposure under-counted in composition metrics | **Med** |
| **Point-in-time** reference data for historical windows | Overwrite-in-place | Look-ahead bias: today's sector/fee applied to past periods; survivorship in long windows | **High** |
| **Sector/classification** completeness | Sparse for some securities | Engine defaults nulls to "unclassified" → allocation metrics misstate true exposure | **Med** |
| Stable **identifiers** for joins | No FIGI; ticker reused across exchanges | Mapping ambiguity for multi-listed names; engine relies on Morningstar ID | **Low/Med** |

**Bottom line:** the two services are well-matched for the **Canadian/US fund & ETF** case
(Morningstar's pre-adjusted, allocation-rich data is exactly what the engine consumes). They
are **not** well-matched for **direct equities**, **multi-currency**, **historical/point-in-time
reporting**, or **cash-flow-aware client returns** — and the engine has no defense against SMS
data-quality regressions because there's no provenance, no PIT, and no corporate-action signal.

---

## 5. Prioritized recommendations (sequenced; effort/impact; build-vs-buy)

### Quick wins (days–weeks; high ratio)
1. **Verify & document the treasury-rates dependency.** Confirm SMS serves
   `/reference/treasury-rates` per currency; if not, this is a P0 outage waiting to happen.
   *Effort: S · Impact: High · Build.*
2. **Persist FMP dividend history + enable FMP for stocks by default.** The data is already
   fetched and discarded; store it and turn the importer on. Unlocks stock dividends/yield and
   fundamentals. *Effort: S/M · Impact: Med/High · Build (data already in hand).*
3. **Parameterize reporting currency & expand FX pairs; fail loudly on missing FX.** Make
   `default-target-currency` per-request and convert the missing-pair warning into an explicit
   error or an OpenFIGI/ECB-backed rate lookup. *Effort: S/M · Impact: Med · Build.*
4. **Add cross-provider discrepancy logging.** Even before full field-provenance, log when
   Morningstar and FMP disagree beyond a threshold on overlapping fields. *Effort: S · Impact:
   Med · Build.* Foundation for a data-quality dashboard.
5. **Reconcile the "48 vs 50 metrics" doc drift** and publish the authoritative catalog.
   *Effort: XS · Impact: Low (credibility) · Build.*

### Mid-term (1–2 quarters; structural-lite)
6. **Add a corporate-actions feed (splits + dividends with effective dates).** Build-vs-buy:
   FMP already has both endpoints (buy/enable); Polygon is the cleaner dedicated source. Store
   with effective dates and use them to validate/adjust return series. *Effort: M · Impact:
   High · Buy (FMP/Polygon) + Build storage.*
7. **Field-level provenance on SMS entities.** Track source per atomic field; expose in the API.
   Enables per-field precedence rules and auditability. *Effort: M/L · Impact: High · Build.*
8. **Add money-weighted return (IRR/XIRR) + Modified Dietz to the engine.** Requires accepting
   external cash flows as input. The single most valuable analytic addition for a robo. *Effort:
   M/L · Impact: High · Build.*
9. **Net-of-fees return toggle.** Deduct MER from the return series on request; pairs naturally
   with existing fee logic. *Effort: M · Impact: Med · Build.*
10. **Portfolio fixed-income roll-up (duration, YTM, convexity).** SMS already has the
    per-security inputs. *Effort: M · Impact: Med · Build.*

### Structural (multi-quarter)
11. **Bitemporal / point-in-time SMS store (valid-from/valid-to, survivorship-free universe).**
    Stop overwriting; keep versioned history; retain delisted securities. Removes survivorship &
    look-ahead bias and makes reports reproducible. *Effort: L · Impact: High · Build* (this is
    the defining feature of a real security master; not something to buy piecemeal).
12. **Performance attribution (Brinson) + contribution-to-return/risk in the engine.** *Effort:
    L · Impact: Med/High · Build.*
13. **VaR / CVaR and a real projection engine (Monte-Carlo goal probability, glide paths).**
    *Effort: L · Impact: Med · Build (or buy a risk-model library).* 
14. **Daily-valued (or at least daily-price-backed) analytics** to fix monthly-granular drawdown
    and enable VaR — depends on SMS gaining a daily price tier (build) or a market-data vendor
    (buy Polygon for US equities/ETFs). *Effort: L · Impact: Med · Buy market data + Build.*

**Sequencing rationale:** 1–5 de-risk and unlock existing-but-dormant data cheaply; 6–10 close
the highest-impact correctness/parity gaps (corporate actions, MWR, provenance); 11 is the
structural keystone (PIT) that everything historical depends on; 12–14 extend the analytic
surface toward Addepar once the data foundation is sound.

---

## 6. Open questions & assumptions

**Assumptions I had to make (state explicitly):**
- Morningstar's monthly-return series are **total-return and split/dividend-adjusted** (standard
  Morningstar behaviour). The engine relies on this; SMS does not validate it. *If untrue, fund
  returns are also wrong.*
- "Reliably populated" judgments for SEDOL/CUSIP/intl identifiers and for stock-level allocations
  are **inferred from provider behaviour**, not measured — I did not have null-rate data.
- The 8 FX pairs and CAD default are taken from `application.yml`; I assume no hidden conversion
  path for other currencies (the code path leaves them native).
- Peer capabilities (Polygon/FMP/Addepar/GIPS) are from general product knowledge as of early
  2026, treated as "peer-typical" baselines, not contract-verified specs.
- I assume the engine's "48 metrics" doc vs 50 enum constants is doc drift, not 2 disabled metrics.

**Exact information that would sharpen this assessment:**
1. **A sample SMS response** for: `/returns/monthly` (to confirm currency & adjustment basis of
   the factors), an instrument lookup, and `/fees` — to verify population, not just schema.
2. **Confirmation of the treasury-rates endpoint** (does SMS serve `/reference/treasury-rates`,
   for which currencies, with what history/lag?).
3. **Null-rate / coverage stats** per field per security type (esp. sector, ISIN/CUSIP/SEDOL,
   dividend, duration) for the latest Morningstar import — to convert "present" into "reliable."
4. **Whether the engine accepts external cash flows / transaction histories** anywhere upstream
   (if a portfolio service holds them, MWR is achievable without changing SMS).
5. **The currency basis of multi-fund portfolio returns** — are return series converted to a
   common currency before linking? (Confirms or refutes the multi-currency methodology concern.)
6. **Whether FMP is enabled in production** (config says disabled by default) and which
   environments turn it on.
7. **Intended use depth:** EOD-valued robo only, or any intraday/trading? Determines whether the
   market-data gaps (OHLCV/bid-ask/tick) are P-low or blocking.
8. **GIPS ambition:** is composite-level GIPS compliance a goal? If so, net-of-fee, composite
   construction, dispersion, and 3-yr ex-post std-dev become required, not optional.
