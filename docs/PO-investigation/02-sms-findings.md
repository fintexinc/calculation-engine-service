# Security Master Service v2 — Raw Findings (codebase scan)

Source: two read-only exploration passes over `/home/apinta/projects/security-master-service-v2`.

## API surface
Base path `/api/v1/wealth/securities`. OpenAPI 3.0.3 spec present (`docs/openapi/`).
- **Lookup (GET):** `/etfs/{id}`, `/funds/{id}`, `/indexes/{id}`, `/stocks/{id}`, `/smas/{id}`
  with `idType` + optional `dataProviders`.
- **Search (POST):** per-type + cross-type; searchTerm + filterConditions + sortRules +
  page/size pagination (page-number based, **no cursor**). Returns lightweight `SecuritySearchDto`.
- **Attribute/batch (POST):** `/returns/monthly`, `/fees`, `/income`, `/geography`,
  `/allocations/{asset|equity-sector|fixed-income-sector|equity-market-cap|country|sector|
  equity-stylebox|fixed-income-stylebox}`, `/credit-quality`, `/maturities`, `/holdings`,
  `/top-holdings`, `/holdings/identifiers`, `/sales-charge`. All batch via `TypedSecurityGroup`.
- **No** streaming / webhooks. **No** documented rate limits (infra-level only). Version `v1`
  in path only; no deprecation policy. Good per-endpoint docs; no data-freshness SLA.
- The CE-expected `/api/v1/wealth/reference/treasury-rates` endpoint was **NOT found** in the
  securities controller scan → either served by a different module or a gap (VERIFY).

## Asset-class & geographic coverage
US + Canada only. Types: ETF (US/CA), mutual fund (US/CA), pooled fund (CA), segregated
fund (CA), hedge fund (CA), GIC (CA), SMA (US/CA), index/benchmark, stock (US/CA).
No international/EM/UK/Europe/Asia direct securities.

## Identifiers
Ticker, TICKER_MIC (+exchange), CUSIP, ISIN, SEDOL (field present), FUNDSERV (CA funds),
MORNINGSTAR_ID, CIK, EXCHANGE_ID. **No FIGI / OpenFIGI.** Population quality of SEDOL/CUSIP
unverified (likely sparse outside US/CA).

## Pricing / market data
- Funds: NAV per unit (+ historical NAV series JSONB). ETFs: market close, NAV,
  premium/discount, 52-wk hi/lo, volume. Stocks: last close (FMP).
- **No OHLCV bars, no bid/ask, no intraday, no tick, no real-time quotes.** EOD/daily batch only.
- Historical: Morningstar monthly-return series up to 20yr; daily price history limited.

## Fundamentals
- Morningstar: fund-level P/E, P/B, P/CF, P/S, yields, ratings.
- FMP: company profile + **82 ratios** + dividend history — BUT FMP **disabled by default**
  (`...import.fmp.enabled=true` required), and only STOCK_US/STOCK_CA auto-imported.
- **No financial statements** (income/balance/cash-flow) stored. FMP statements not mapped.

## Corporate actions — ABSENT
No splits, mergers, spin-offs, name-change, or delisting tracking from either provider.
Dividends: FMP `FmpDividendDto` (date, adjDividend, dividend, yield, record/pay/declaration
dates) is **fetched but not persisted** — only an aggregate `dividendYield` scalar is stored.
No `corporate_actions` / `splits` / `dividend_history` tables. No split-adjustment logic.

## Multi-provider reconciliation — weak
- Provenance tracked at **record level** (`List<DataProvider>` on the entity), **not per field**.
- Merge = `AbstractDataMerger.fillNullFields`: first provider in caller-supplied priority wins;
  lower-priority providers only fill **null** fields. Whole-field replacement.
- **No cross-provider consistency checks** — if Morningstar P/E=15.2 and FMP=16.1, first wins
  silently; no discrepancy flag, no confidence score, no reconciliation report.
- Gap-filling is asymmetric & passive: Morningstar covers funds/allocations/holdings; FMP
  covers stock ratios/dividends. If FMP disabled (default), stock fundamentals simply absent.

## Data quality
- **Point-in-time: data OVERWRITTEN each import.** `DataVersion` tracks import batch/status
  but is not used for bitemporal history. Delisted/removed securities **disappear** →
  survivorship & look-ahead bias; a past report cannot be reproduced.
- **No null-rate monitoring**, no required-field validation, no freshness threshold (newest
  `yyyyMMdd` folder auto-selected regardless of age).
- Import: Morningstar daily cron `0 0 6 * * ?` (ShedLock, 2k securities/batch, 10k holdings/batch).
  FMP daily `0 0 7 * * ?` but disabled by default.

## Lifecycle / status — ABSENT
No active/delisted flag, no status-change history, no symbol-change history. Only
`inceptionDate`, `updatedAt`, per-datapoint `asOfDate`.

## Morningstar field richness (present in CSV)
Returns (1m–20yr + YTD + since-inception + calendar years), Sharpe/Sortino (multi-period),
Alpha/Beta (3/5/10yr), max-drawdown, std-dev, credit-quality buckets, FI super-sectors,
equity sectors (11), style boxes (9), market-cap bands, country/geo allocations, holdings
(weight, ticker, CUSIP, ISIN, currency, shares, mktval), MER/mgmt-fee/expense ratios,
distributions breakdown, ratings/ranks. Rich for funds; sparse/N-A for direct stocks.
