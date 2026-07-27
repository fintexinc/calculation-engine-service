---
description: "Senior financial-analyst validation of a loaded ticket: should we implement it, do we have the data, and what options exist given the Morningstar/FMP-via-SMS data model"
allowed-tools: ["Bash", "Read", "Grep", "Glob", "AskUserQuestion"]
---

# /validate-ticket — Senior-analyst requirement validation

Run this **after** you have loaded a ticket's context into the conversation (pasted Jira
description + acceptance criteria + relevant comments). It puts you in the seat of a **senior
financial analyst** on this portfolio-analytics engine and validates the ticket **before** any
implementation.

This command is **read-only and advisory**. It has no `Edit`/`Write` access by design. It must
**never** modify code, commit, open a PR, or make **network** calls to any external service
(SMS / FMP / the Morningstar API). It **may and should read the local Morningstar CSV files** that
the bundled SMS instance loads (see below) — those are local files, not a service call — to
confirm the ticket's data actually exists. Its whole output is an assessment the developer reads.

Answer three questions, in order, then print a summary table:

1. **Should the implementation be made?**
2. **Do we have the data to check / satisfy the ticket?**
3. **What options exist to solve it**, given how this system sources data?

---

## Data-model context you MUST reason from

Getting this right is the point of the command — do not skip it.

- **This service reads only SMS.** calculation-engine-service does **not** read FMP or the
  Morningstar CSVs directly. There is no FMP HTTP client and no Morningstar CSV reader in this
  repo. All security data arrives from **Security Master Service (SMS)** over REST
  (`SM_REST_BASE_URL`, see `bootstrap/src/main/resources/application.yml`), via
  `POST /api/v1/wealth/securities/attributes`.
- **Morningstar and FMP are upstream sources *of SMS*, selected by a provider list.** This
  service sends a preferred-provider list to SMS and SMS decides which upstream supplies each
  attribute:
  - `bootstrap/src/main/resources/application.yml` → `data-providers: MORNINGSTAR, FMP`.
  - `web-client-adapter/.../sm/fetcher/CompositeSecurityMasterFetcher.java` builds
    `IdsAndDataProvidersRequest.builder().dataProviders(providers)` — the list is a *request
    parameter to SMS*, not a switch that calls FMP/Morningstar here.
  - The `FMP_API_URL` / `FMP_API_KEY` / `MORNINGSTAR_CSV_BASE_PATH` env vars live in
    `ce-environment/.env` and configure the **bundled SMS instance**, not this service.

**Therefore, "do we have the data?" is really: does an SMS *attribute* exist for the field the
ticket needs, is it bound in the fetcher registry, and is there a mapper that turns it into a CE
domain type?** Provider reasoning (Morningstar vs FMP) is about *which upstream SMS would resolve
that attribute from* and the provider-specific quirks this repo already encodes.

### SMS attribute → mapper → metric-family reference (verify against current code)

Binding registry: `web-client-adapter/.../sm/fetcher/SecurityAttributeFetcherConfig.java`
(each `CompositeAttributeBinding` maps a `CompositeSecurityAttribute` → SMS response → CE domain
type → mapper). Treat this table as a starting map, then **confirm it against the current
`SecurityAttributeFetcherConfig` and the named mapper** — bindings drift as the codebase evolves.

| Data need in the ticket | SMS attribute | Mapper |
|---|---|---|
| Monthly total-return series | `MONTHLY_RETURNS` | `MonthlyReturnsMapper` |
| Risk-free / T-Bill series | (T-Bill fetch) | `SmsTreasuryBillsFetcherImpl` (`/treasury-rates/{currency}`) |
| Management fee / MER | `FEES` | `FeesMapper` (carries per-field provider tags) |
| Sales charge | `SALES_CHARGE` | `SalesChargeMapper` |
| Income / dividend yield | `INCOME` | `YieldMapper` |
| Asset allocation | `ASSET_ALLOCATION` | `AssetAllocationSecurityMasterMapper` |
| Equity sector weights | `EQUITY_SECTOR_ALLOCATION` | `EquitySectorAllocationMapper` |
| Fixed-income sector alloc. | `FIXED_INCOME_SECTOR_ALLOCATION` | `FixedIncomeSectorAllocationMapper` |
| Equity country allocation | `EQUITY_COUNTRY_ALLOCATION` | `EquityCountryAllocationMapper` |
| Geographic allocation | `EQUITY/FIXED_INCOME_GEOGRAPHIC_ALLOCATION` | `GeographicAllocationMapper` |
| Country exposure | `COUNTRY_ALLOCATION` | `CountryExposureMapper` |
| Classification allocation | `SECURITY_CLASSIFICATION_ALLOCATION` | `ClassificationAllocationMapper` |
| Equity market cap | `EQUITY_MARKET_CAPITALIZATION` | `EquityMarketCapitalizationMapper` |
| Equity style-box | `EQUITY_STYLEBOX` | `EquityStyleboxExposureMapper` |
| Fixed-income style-box | `FIXED_INCOME_STYLEBOX` | `FixedIncomeStyleboxExposureMapper` |
| Credit-quality buckets | `CREDIT_QUALITY_RATINGS` | `CreditQualityMapper` |
| Maturity buckets | `MATURITIES` | `MaturityAllocationMapper` |
| Top / underlying holdings | `TOP_HOLDINGS` | `TopHoldingsMapper` |

**Provider-specific facts this repo actually encodes** (cite these when relevant, and re-read them
to confirm they still hold):

- **US fees / MER:** `application/.../calculation/service/fee/UsFeeResolutionStrategy.java` — US
  Morningstar/FMP-sourced fee data populates MER only for certain US fund types (e.g. `ETF_US`).
  A ticket about missing/zero MER on US funds lives here.
- **Morningstar is the preferred holdings identifier:** `application.yml` sets
  `holdings-identifier-type: MORNINGSTAR_ID` ("best data availability"); identifier priority is
  `MORNINGSTAR_ID → TICKER → FUNDSERV → ISIN → CUSIP` (see `TopHoldingsMapper`,
  `domain/.../holding/CommonHolding.java`).
- **Morningstar feed quirk:** `application/.../service/allocation/AbstractAssetAllocationService.java`
  clamps near-zero residual allocation values that Morningstar reports.

### Locating the Morningstar CSV data (the source of truth for Step 2)

The bundled SMS instance loads Morningstar CSV snapshots from `MORNINGSTAR_CSV_BASE_PATH`
(declared in `ce-environment/.env` — note that value is a machine-specific absolute path). To find
the CSVs from this repo, resolve the base directory in this order and use the first that exists:

```bash
# 1. Sibling SMS checkout (usual local layout)
ls -d ../security-master-service-v2/file-storage-adapter/src/main/resources/morningstar 2>/dev/null
# 2. Whatever ce-environment/.env points at (strip the MORNINGSTAR_CSV_BASE_PATH= prefix; it may be
#    a Windows path that does not resolve on this machine)
grep MORNINGSTAR_CSV_BASE_PATH ce-environment/.env
# 3. Last resort: search for a 'morningstar' resources dir under the parent
find .. -maxdepth 6 -type d -iname morningstar 2>/dev/null
```

Inside the base dir, data is snapshotted **per date**: subfolders named `YYYYMMDD` (e.g. `20251024`).
**Use the latest dated folder** unless the ticket names a date. Files are named
`entity_<fund-type>_<region>[_<purpose>]<date>.csv`, plus `monthly_return_*`, `*_performance*`,
`*_growth10k*`, and `entity_all_*_holdings*` variants — roughly 100 CSVs per snapshot. Columns are
Morningstar's raw field names (e.g. `MStarID, FundName, Ticker, Return1Yr, ReturnYTD,
ReturnSinceInception, AnnualReturnYear1..10, MonthEndDate, CategoryName, ...`).

**If no base dir resolves**, say so explicitly and fall back to reasoning from the SMS
fetchers/mappers only — and mark Step 2's verdict as **unverified against source data**.

---

## Step 0 — Preconditions

Confirm a ticket is actually in the conversation context (a real description and/or acceptance
criteria — not just a ticket ID). If it is **not** present, **stop** and tell the developer:

> No ticket context found. Load the ticket first (paste its description, acceptance criteria, and
> relevant comments), then re-run `/validate-ticket`.

Do **not** guess or invent the requirement. Everything below depends on the real ticket text.

Restate the requirement in one or two sentences in your own words so the developer can confirm you
understood it correctly before you analyze.

---

## Step 1 — Should the implementation be made?

Assess the requirement as a senior analyst who knows correct metric / returns / fee semantics:

- **Clarity:** Is the requirement unambiguous? Are acceptance criteria testable? Note any
  ambiguity that must be resolved first.
- **Financial soundness:** Does the expected behavior match correct financial semantics (e.g.
  error-vs-null-vs-zero for a missing input, period conventions, currency handling, fee
  definitions)? Flag anything that would produce a misleading number.
- **Already covered?** Search the codebase (metric enum `CalculationMetric`, the relevant
  `CalculationService`, validators, existing tests) to see whether this is already implemented or
  partially handled.
- **Conflicts:** Would it contradict existing behavior, another metric, or a validation rule?

**Verdict (pick one, with reasoning):**

- **Implement** — clear, sound, not yet covered.
- **Needs clarification** — list the exact questions to ask before coding.
- **Already covered** — cite where; recommend closing or narrowing the ticket.
- **Reject / rework** — financially unsound or contradictory as written; explain.

---

## Step 2 — Do we have the data? (analyze the Morningstar CSVs)

The goal here is not just "is a field wired up" but **"does the actual source data support what the
ticket claims / requires"** — so you must open and inspect the real Morningstar CSVs, not only
reason about bindings. A ticket's stated requirement can be *wrong* about what the data looks like;
this step confirms or refutes it against the source of truth.

**A. Wiring trace (fast, from code):**

1. Which metric / command / `CalculationService` does the ticket touch? (map the metric name via
   `CalculationMetric`).
2. Which SMS **attribute** supplies the field, is it **bound** in `SecurityAttributeFetcherConfig`,
   and which **mapper** turns it into a CE domain type? Which upstream would SMS resolve it from,
   and does the repo encode a provider quirk (fees/MER, identifier priority, residual clamps)?

**B. Source-data analysis (the new, primary check — inspect the CSVs):**

3. Resolve the Morningstar base dir and pick the latest `YYYYMMDD` snapshot (see *Locating the
   Morningstar CSV data* above).
4. Identify the CSV(s) that match the ticket's **fund type + region + purpose** (e.g. US ETF MER →
   `entity_etf_us*`; monthly returns for Canadian mutual funds → `monthly_return_mutual_fund_canada*`
   or the relevant `*_performance*`). List which files you inspected.
5. **Read the header** to confirm the required column(s) actually exist, and read a sample of rows
   (and, where it matters, count populated vs empty cells) to confirm the field is **actually
   populated** for the relevant securities — not present-but-blank. Prefer targeted reads:

   ```bash
   head -1 <file.csv>                                   # column names
   # populated vs blank for a column (find its index N from the header first):
   awk -F',' 'NR>1{t++; if($N!="") p++} END{print p"/"t" populated"}' <file.csv>
   ```

6. Cross-check the CSV reality against the ticket's assumption and against how the mapper reads it
   (column name, units — percent vs decimal, currency, date format `MonthEndDate`). Call out
   mismatches: column missing, systematically blank for this fund type/region, wrong units, stale
   snapshot date.
7. **Show 3–5 concrete example rows** you actually looked at, so the developer can see the data
   behind the verdict. For each example include the **`Ticker`** (and `MStarID`/`FundName` when
   helpful to identify the security) plus **only the raw column(s) relevant to the ticket** — do
   not dump the whole ~60-column row. Deliberately pick examples that illustrate the finding:
   include both populated and blank/edge cases when the point is a gap, or a few representative
   securities when the data is present. Present them as a small table, e.g.:

   | Ticker | FundName | <relevant column> | <relevant column> |
   |--------|----------|-------------------|-------------------|
   | ABC    | ...      | 12.34             | (blank)           |

   Quote the values verbatim from the CSV (state the file + snapshot date they came from). If you
   summarized or rounded, say so.

Give an **evidence chain**: `file:line` for the wiring (fetcher/binding/mapper/service) **and** the
concrete CSV file names + columns + populated-ratio you observed, anchored by the 3–5 example rows
above.

**Verdict (pick one):**

- **Confirmed — data available:** the CSV(s) contain the required column, populated for the relevant
  securities, and the wiring maps it. The ticket's premise holds.
- **Partially available:** present but with gaps (blank for some fund types/regions, only certain
  snapshots, unit/currency caveats). Quantify from the CSV.
- **Missing / ticket premise wrong:** the column is absent or systematically empty in the source
  CSVs, **or** the CSV data contradicts what the ticket assumes. State this plainly with the file
  evidence — this is often the most valuable outcome, since it means the requirement itself needs
  revisiting before any code is written.
- **Unverified against source data:** no Morningstar base dir resolved on this machine; verdict is
  wiring-only. Tell the developer how to make the CSVs available.

---

## Step 3 — Options to solve it

Base the options on **what Step 2 actually found in the CSVs**, not on assumptions. If Step 2
concluded the ticket premise is wrong or the data is missing, the first "option" is to fix or
reject the ticket — do not propose building on data that is not there.

Offer **2–3 concrete approaches**. For each:

- **What it does** — the change in this service (which service/validator/mapper), and whether it
  needs anything **from SMS** (a new/extended attribute, a provider-list change) or new Morningstar
  columns that are not in the current snapshots.
- **Provider / data angle** — which upstream (Morningstar vs FMP) supplies the field and the
  **concrete coverage you observed in the CSVs** (which fund types/regions have it populated, which
  are blank), plus units/currency/date caveats.
- **Trade-offs** — data completeness per the CSV evidence, fund-type/region coverage, fallback
  behavior, and the **error-vs-null-vs-zero** semantics (this system distinguishes them
  deliberately — see the MER work on the current branch).
- **Effort / blast radius** — roughly how much changes and what it risks.

End Step 3 with a **recommended option** and why.

---

## Step 4 — Summary

Print a compact verdict table, then offer next steps (do **not** execute them automatically):

```
Ticket validation — <ticket id / short title>
| Question                        | Verdict                       | Reason (one line)      |
|---------------------------------|-------------------------------|------------------------|
| 1. Should we implement?         | Implement / Clarify / Covered / Reject | ...           |
| 2. Do we have the data? (CSV)   | Confirmed / Partial / Missing-or-premise-wrong / Unverified | ... |
| 3. Recommended option           | <option name>                 | ...                    |
```

Then:

- If the verdict is **Implement** and data is **Confirmed** or **Partial**, offer to hand off to
  implementation (the `brainstorming` skill for a spec, or the `coder` skill to build it) — **only
  if the developer says so**.
- If the verdict is **Needs clarification**, data is **Missing / premise wrong**, or **Unverified**,
  list the exact blocking questions / SMS or CSV dependencies the developer should resolve first —
  do not offer to implement.

Never start implementing from within this command.
