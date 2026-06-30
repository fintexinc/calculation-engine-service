# Peer Benchmark Reference (analyst working notes)

These are the reference points the two services are measured against. Sourced from
general knowledge of each vendor's public product surface as of early 2026; treated as
"peer-typical" baselines, not exhaustive specs. Where a specific claim is load-bearing
for a gap, it is flagged as an assumption to verify.

## Security Master peers

### Polygon.io (market-data peer)
- **Asset classes:** US stocks, options, indices, forex, crypto. (No mutual funds, no
  fixed income, limited international equities — its breadth is narrower than Morningstar
  on funds but far deeper on real-time market microstructure.)
- **Pricing:** real-time + historical OHLCV; trades & quotes (tick-level, NBBO bid/ask);
  aggregates from 1-second to 1-month bars; ~15+ years history on equities. Latency tiers
  from delayed (free) to real-time (paid). WebSocket streaming for live trades/quotes.
- **Corporate actions:** dedicated splits and dividends endpoints; ticker events
  (symbol changes, name changes); IPO data. Adjusted vs unadjusted close via an `adjusted`
  query param — adjustment is a first-class feature.
- **Reference:** Ticker details (CIK, composite FIGI, share class FIGI, SIC code,
  market cap, primary exchange MIC). Ticker types reference. Related companies.
- **Fundamentals:** financial statements (income/balance/cash-flow) via `vX/reference/financials`,
  sourced from SEC filings, point-in-time with filing dates.
- **API:** REST + WebSocket, cursor pagination, documented rate limits per tier,
  versioned (`/v2`, `/v3`, `/vX`), OpenAPI spec, strong docs.

### Financial Modeling Prep (FMP) — also a SMS source provider
- **Asset classes:** US + international equities, ETFs, mutual funds, indices, forex,
  crypto, commodities. Broad symbol coverage (~25k+ stocks).
- **Pricing:** real-time-ish quote, historical EOD (full + light), intraday (1min–4hr),
  historical with split/dividend adjustment.
- **Corporate actions:** stock splits calendar, dividends calendar (historical +
  upcoming), M&A, IPO calendar, delisting data.
- **Reference:** company profile (sector, industry, CEO, exchange, CIK, ISIN, CUSIP),
  symbol lists, tradable symbols, exchange symbols, ETF holdings/sector weightings,
  mutual fund holdings.
- **Fundamentals:** full financial statements (annual/quarterly, as-reported + standardized),
  60+ ratios, key metrics, enterprise value, financial scores, DCF valuation, earnings
  surprises, analyst estimates, price targets, grades.
- **API:** REST, API-key, documented rate limits, bulk/batch endpoints (e.g.
  batch quotes, bulk EOD), stable versioned paths.
- NOTE: FMP is BOTH a peer and one of SMS's own upstream providers — relevant because
  SMS could in principle expose much more of FMP than it currently does.

### Other reference points (mentioned where relevant)
- **Bloomberg / Refinitiv (institutional security master):** FIGI/OpenFIGI, full
  identifier cross-reference, point-in-time reference data, survivorship-bias-free
  universes, corporate-action golden copy. The institutional "gold standard" for what a
  security master SHOULD track (provenance, as-of-date, lifecycle).
- **OpenFIGI (Bloomberg, free):** identifier mapping ISIN/CUSIP/SEDOL/ticker → FIGI.
  Cheap to integrate; relevant to the identifier-completeness gap.
- **Morningstar Direct / API:** SMS's other source. Strong on funds/ETFs: holdings,
  asset allocation, sector exposure, credit quality, maturity buckets, style box,
  Morningstar category, sustainability. Weaker / licensed differently for tick pricing.

## Calculation Engine peers

### GIPS (Global Investment Performance Standards) — the methodology standard
Not a metric list per se, but the compliance bar for performance reporting:
- **Return method:** Time-Weighted Return (TWR) required for composites, with
  revaluation at large external cash flows (or Modified Dietz / daily). Money-Weighted
  Return (MWR/IRR) permitted/required only in specific cases (private markets, control
  over cash flows).
- **Geometric linking** of sub-period returns.
- **Net-of-fees AND gross-of-fees** presentation; fee schedule disclosure.
- **Annualization:** periods < 1 year must NOT be annualized.
- **Composite construction**, dispersion (e.g. asset-weighted std dev of portfolio
  returns), 3-year annualized ex-post standard deviation required.
- **Benchmark** total return shown alongside, consistent with mandate.
- **Significant cash flow** policy, valuation policy, point-in-time discipline.
The CE doesn't need to be GIPS-*certified* for a retail/robo use case, but GIPS defines
the methodology choices a credible engine should make (TWR, geometric, no sub-year
annualization, net & gross).

### Addepar (wealth-platform analytics peer — the breadth benchmark)
Representative analytic surface for a wealth/PFM platform:
- **Returns:** TWR and IRR (MWR) side-by-side, gross & net, multi-currency with FX
  attribution, since-inception, custom date ranges, periodic (MTD/QTD/YTD/trailing/
  calendar-year), money-weighted vs time-weighted toggle.
- **Risk:** std dev, downside deviation, beta, alpha, R², tracking error, max drawdown,
  Value-at-Risk, correlation matrices.
- **Risk-adjusted:** Sharpe, Sortino, Treynor, Information Ratio, Jensen's alpha,
  Calmar, capture ratios (up/down).
- **Attribution:** performance attribution (allocation/selection/interaction —
  Brinson), contribution to return by holding/asset class/sector.
- **Composition:** asset allocation, sector/region/currency exposure, look-through into
  funds, concentration, fixed-income analytics (duration, credit quality, maturity,
  yield).
- **Fees:** management fee, expense ratios, fee drag, net vs gross.
- **Income/cash flow:** dividend/interest income, yield, projected income.
- **Multi-currency** throughout, with base-currency reporting and FX impact isolation.
- Handles cash flows mid-period (it's a TWR/IRR engine over real transaction histories).

### Standard risk/risk-adjusted metric set (the "should-have" checklist for CE)
Returns: cumulative, annualized (CAGR), trailing (1m/3m/6m/1y/3y/5y/10y/YTD/SI),
calendar-year, TWR, MWR/IRR, rolling returns, best/worst period.
Risk: standard deviation (ann.), downside deviation, semi-variance, max drawdown,
drawdown duration/recovery, VaR (historical/parametric), CVaR/expected shortfall,
tracking error, beta, downside/upside beta.
Risk-adjusted: Sharpe, Sortino, Treynor, Information Ratio, Jensen's alpha, Calmar/MAR,
Omega, upside/downside capture, M².
Relative: alpha, beta, R², correlation, active return, tracking error, batting average.
Composition: asset allocation, sector/geo/currency, look-through, concentration (top-N,
HHI), fixed-income duration/convexity/credit/maturity, equity style/market-cap.
Fees: gross vs net, expense ratio drag, fee-adjusted returns.
Forecasts: Monte Carlo projection, expected return/vol, goal-probability, glide paths.
