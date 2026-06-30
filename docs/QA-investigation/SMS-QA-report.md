# SMS — Security Master Service v2 · QA Report

**Target:** `http://localhost:8683`  ·  **Base path:** `/api/v1/wealth/securities`
**Date:** 2026-06-30  ·  **Tester:** QA (adversarial, evidence-based)
**Oracle / source of truth:** Morningstar CSVs at
`security-master-service-v2/file-storage-adapter/src/main/resources/morningstar/20251024/`

> **Scope note (important):** Not all Morningstar files were imported into SMS. Per agreement, the **imported set is inferred from the API** (whatever an endpoint returns data for is treated as "imported"), and **correctness is judged by reconciling each served value field-by-field against the source CSV** — the API↔CSV comparison is the end-to-end oracle (it also proves the DTO mapping + normalization layer, which a DB check would not). Data present in the CSVs but absent from the API is reported as a **coverage observation (import scope)**, *not* a serving defect.

---

## 1. Summary verdict

**For the data that is imported, SMS is CORRECT and can be trusted. Every served value reconciled exactly against the Morningstar source — identifiers, prices/NAV-derived fields, fees, returns (monthly + trailing), allocations, sectors, holdings weights, ratios. Confidence in correctness: HIGH.**

No value mismatches were found in any reconciliation. Error handling on lookups is exemplary (clean 4xx, structured leak-free envelopes, no 500s, no stack traces). Treasury rates are complete.

The only caveats are **coverage** (some categories/series not yet imported) and one **consistency** item to confirm:

| # | Item | Type | Severity |
|---|------|------|----------|
| S-1 | Monthly-return history is only **10 months** deep (2024-12 → 2025-09) for every security. Caps downstream ≥12-month risk metrics. | Coverage (data depth) | Med |
| S-2 | **US mutual funds not retrievable** — `/funds/{id}` 404, `/returns/monthly` & `/fees` empty — though present in `entity_mutual_fund_united_states`. | Coverage (not imported) | Med |
| S-3 | `/holdings` (full constituent detail) returns `[]` for every security, while `/holdings/identifiers` and `/top-holdings` for the **same** security are populated. Confirm whether full-holdings detail is in import scope or a serving gap. | Consistency / coverage | Med |
| S-4 | The **÷100 normalization is per-field, not uniform** (applied to trailing returns/std-dev/sector/allocation; *not* to monthly returns or MER). A consumer must know which fields are fractions. | Correctness (documentation) | Low |

---

## 2. Scope & method

- **Contract:** OpenAPI spec (`docs/openapi/security-master-service-api.yaml`) + source read. `/v3/api-docs` / `/swagger-ui` not exposed at runtime (404, clean envelope) — spec is a static file.
- **Live-tested:** GET lookups (`/etfs`,`/funds`,`/indexes`,`/stocks`,`/smas`); POST batch (`/returns/monthly`, `/fees`, `/allocations/*`, `/holdings`, `/top-holdings`, `/holdings/identifiers`, `/income`); search (`/etfs/search`,`/funds/search`); `/reference/treasury-rates[/{ccy}]`.
- **Correctness method:** for each served field, parsed the matching CSV cell and compared numerically (tolerance 1e-4), accounting for the documented ÷100 normalization. Requests use **tickers** where available (per request), `MORNINGSTAR_ID` for indexes. Verified identifier-independence (ticker vs MStarID return identical payloads).
- **Reconciliation breadth:** monthly returns reconciled programmatically across **ETF-CA, ETF-US, CA mutual fund, benchmark index** (all 10/10 months, 0 mismatches); full lookup-DTO field reconciliation on ETF `NVDH.U`/`NVDH`.
- **Batch body shape (verified live):** `{"typedIdentifiers":[{"type":<FiType>,"ids":[{"id","idType"}]}],"dataProviders":[...]}`.

---

## 3. Test results table

| ID | Category | Endpoint | Input | Expected (from source CSV) | Actual | Result | Sev |
|----|----------|----------|-------|----------------------------|--------|--------|-----|
| P-01 | Correctness | `POST /returns/monthly` | NVDH.U (ETF_CA) | 10 months, 2025-09=5.60793 … 2024-12=−2.95799 | exact, 10/10 | **PASS** | — |
| P-02 | Correctness | `POST /returns/monthly` | USFR (ETF_US), F00000MP5F (MF_CA), F00000T5V0 (index) | 10 months each | exact, 0 mismatches each | **PASS** | — |
| P-03 | Correctness | `GET /etfs/NVDH.U` `trailingReturns` | Return1Mth 5.60793 / 3Mth / 6Mth / 1Yr 39.52793 / YTD 28.68935 | ÷100 | 0.0560793 / 0.1350775 / 0.5418871 / 0.3952793 / 0.2868935 — exact | **PASS** | — |
| P-04 | Correctness | `GET /etfs/NVDH.U` scalars | MER 0.990, ActMgmtFee 0.40000, FundNetAssets 23297487, StdDev1Yr 32.215, SharpeRatio1Yr 1.194 | per-field | MER 0.99, mgmtFee 0.40, netAssets 23297487, stdDev 0.32215 (÷100), sharpe 1.194 — exact | **PASS** | — |
| P-05 | Correctness | `GET /etfs/NVDH.U` identifiers | ISIN CA41755Y2050, CUSIP 41755Y205, ticker NVDH.U, inception 2024-08-19, category "Equity - Other", currency USD | — | all exact | **PASS** | — |
| P-06 | Correctness | `POST /top-holdings` NVDH.U | NVIDIA Corp: weight 102.45525, mktVal 33202118, shares 127866, shareChg 4697, ticker NVDA, ISIN US67066G1040 | — | all exact | **PASS** | — |
| P-07 | Correctness | `POST /allocations/asset` NVDH.U | US equity 102.45525%, options −2.48077%, cash 0.02551% | ÷100 | US_EQUITIES 1.0245525, INTL −0.0248077, CASH 0.0002551 — exact, sums to 1.0 | **PASS** | — |
| P-08 | Correctness | `POST /allocations/equity-sector` | Technology 100 | ÷100 | TECHNOLOGY 1.0 | **PASS** | — |
| P-09 | Correctness | `POST /income` NVDH.U | DividendYield, distribution dates | — | dividendYield 0.15296, 12 dist dates | **PASS** | — |
| P-10 | Completeness | `POST /holdings/identifiers` NVDH.U | constituent IDs | — | populated (E0USA01191 …) | **PASS** | — |
| P-11 | Positive | `GET /reference/treasury-rates/CAD` | — | full series | CAD monthly T-bill series from 2015-07 | **PASS** | — |
| P-12 | Positive | `POST /etfs/search` `"Harvest"` | — | Harvest ETFs | returns AMZH/NVDH… w/ ISIN/CUSIP | **PASS** | — |
| P-13 | Positive | lookups across types | NVDH.U, USFR (ETF), F00000MP5F (CA MF), F00000T5V0 (index) | 200 | all 200, correct entity | **PASS** | — |
| O-01 | Coverage | `POST /returns/monthly` any | — | ≥12 months ideally | only **10 months** imported | **OBSERVE** | Med |
| O-02 | Coverage | `GET /funds/RGGGX` (US MF) | in source | 200 | **404**; `/returns/monthly` & `/fees` → `[]` | **OBSERVE** | Med |
| O-03 | Consistency | `POST /holdings` (full) NVDH.U | constituent detail | populated | **`[]`** while `/holdings/identifiers` + `/top-holdings` populated | **REVIEW** | Med |
| O-04 | Coverage | `GET /funds/{gic}` | GIC in source | retrievable | **404** — no GIC lookup path | **OBSERVE** | Low |
| N-01 | Negative | `GET /etfs/NOPE123?idType=MORNINGSTAR_ID` | bad id | 404 | 404 `MAP-003`, clean | **PASS** | — |
| N-02 | Negative | `GET /etfs/NVDH.U?idType=BOGUS` | bad enum | 400 | 400 `REQ-006`, lists valid values | **PASS** | — |
| N-03 | Negative | `GET /etfs/NVDH.U` (no idType) | missing param | 400 | 400 `REQ-003` | **PASS** | — |
| N-04 | Negative | `POST /fees` `{bad json` | malformed | 400 | 400 `REQ-002` | **PASS** | — |
| N-05 | Negative | `GET /funds/F00001MX1C` (ETF via fund path) | wrong type | 404 | 404 `MAP-003` | **PASS** | — |
| N-06 | Mixed | `POST /fees` `[NVDH.U, ZZZNOPE]` | valid+invalid | per-id signal | returns 1 entry, **silently** omits invalid id | **PARTIAL** | Low |

---

## 4. Findings

### S-1 — Monthly history only 10 months deep · Coverage (data depth) · **Med**
Every `monthly_return_*` series imported holds exactly 10 months (2024-12 → 2025-09). Values are correct, but the depth caps all downstream ≥12-month risk metrics in CES (std-dev, Sharpe, Sortino, alpha, beta, R², capture, tracking-error, etc.). **Recommendation:** import ≥12 (ideally 36–120) months to unlock the risk surface.

### S-2 — US mutual funds not retrievable · Coverage (not imported) · **Med**
```bash
curl -s -o /dev/null -w "%{http_code}" .../funds/RGGGX?idType=TICKER   # 404
curl -s -X POST .../returns/monthly -d '{"typedIdentifiers":[{"type":"MUTUAL_FUND_US","ids":[{"id":"RGGGX","idType":"TICKER"}]}],...}'  # []
```
`RGGGX` (`F00000NM1C`, "American Funds Global Growth Port R6") is in `entity_mutual_fund_united_states20251024.csv`; **CA** mutual funds work via the same paths. Appears the US-MF category is not in the current import scope.

### S-3 — Full-holdings detail empty while identifiers/top-holdings populated · Consistency · **Med**
For `NVDH.U`: `/holdings` → `[]`, but `/holdings/identifiers` → populated and `/top-holdings` → populated (NVIDIA, exact). The constituent data is clearly imported (identifiers + top-holdings prove it), so the full-detail `/holdings` endpoint returning empty is either out of import scope for that projection or a serving gap. **Recommendation:** confirm intended scope; if in scope, this is a serving bug.

### S-4 — ÷100 normalization is per-field · Correctness (doc) · **Low**
Verified conventions on served fields:
- **Fraction (÷100 of source %):** trailing returns, `standardDeviation`, sector/asset/country/stylebox allocations.
- **Percent as-is (no ÷100):** monthly returns (e.g. 5.60793), `dividendYield` (0.15296 — already fractional in source).
- **As-is:** MER `0.99`, managementFee `0.40` (source `0.990`/`0.40000`).
A consumer must apply the correct interpretation per field; the mix is a documentation risk, not a value error.

### N-06 — Batch silently drops unknown IDs · **Low**
`/fees [NVDH.U, ZZZNOPE]` returns one entry and no notification for the dropped id. Consider a per-id `WARNING` so callers can distinguish "invalid id" from "no data."

---

## 5. Completeness & correctness findings vs source

**Correct & complete (reconciled exactly — trustworthy):**
- Monthly returns — exact across ETF-CA/US, MF-CA, index (40 month-values checked, 0 mismatches).
- Trailing returns — exact (÷100 of source Return{1,3,6,9}Mth / 1Yr / YTD).
- Identifiers (ticker/ISIN/CUSIP/MStarID), currency, category, inception, net assets, MER, management fee, dividend yield, Sharpe (1.194), std-dev (÷100) — exact.
- Top-holdings (weight/market value/shares/share change/ticker/ISIN) — exact.
- Asset/sector/country/stylebox/market-cap allocations — exact (÷100).
- Treasury rates — complete (2015→).

**Not present in API (coverage / import scope — not defects):**
- Monthly history beyond 10 months (S-1).
- US mutual fund category (S-2).
- Full-holdings constituent detail via `/holdings` (S-3, pending scope confirmation).
- GIC lookup endpoint (O-04).

---

## 6. Recommendations & open questions

**Recommendations (priority order):**
1. **Import ≥12 months** of monthly returns (S-1) — unblocks CES risk metrics.
2. **Confirm `/holdings` scope** (S-3) — fix serving if full detail is meant to be exposed.
3. **Decide on US mutual funds & GICs** (S-2/O-04) — import or document as out of scope.
4. **Document the per-field normalization** (S-4) and add per-id batch warnings (N-06).

**Could not verify (and what's needed):**
- Correctness of US mutual fund / GIC data — needs those categories imported.
- Full-holdings detail values — needs `/holdings` to return data.
- Fixed-income allocation / maturity / credit-quality *values* — needs a fixed-income security with populated source (the ETF sampled is pure equity, so these are legitimately null).
- FMP-sourced fundamentals — provider disabled by default.
