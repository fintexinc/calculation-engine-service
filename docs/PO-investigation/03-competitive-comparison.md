# Competitive Comparison — Calculation Engine Service vs. Addepar & BlackRock Aladdin

**Subject ("our service"):** the **Calculation Engine Service** — an embeddable, API-first
portfolio analytics & risk engine (50 metrics, single REST dispatch endpoint, hexagonal
Spring Boot microservice), fed by the in-house Security Master Service (Morningstar + FMP)
and Bank of Canada FX.

**Comparators (well-known functional peers):**
- **Addepar** — the wealth-management analytics & reporting *platform* (RIAs, family offices,
  large wealth managers). The breadth & reporting benchmark.
- **BlackRock Aladdin** (specifically **Aladdin Wealth**) — the canonical enterprise
  investment-and-risk *analytics engine* that banks and wealth managers **embed** to deliver
  portfolio risk/return analytics to advisors and clients. The "analytics-as-infrastructure"
  benchmark — the same *category* as us, at the institutional extreme.

> Why these two: Addepar defines the *ceiling* for analytic breadth and client reporting in
> wealth-tech. Aladdin defines the *category we actually live in* — a headless analytics engine
> embedded inside someone else's product — which makes it the most honest mirror for our
> positioning. (Morningstar was excluded: it is our upstream *data supplier*, a data/research
> product, not a competing analytics service. Swap candidates if you want a lighter retail lens:
> Nitrogen/Riskalyze or Kwanti.)

---

## 1. Competitive category — what kind of product each one is

This framing matters more than any single feature:

| | Calculation Engine Service | Addepar | BlackRock Aladdin (Wealth) |
|---|---|---|---|
| **Product type** | Embeddable **API/microservice** (infrastructure) | Full SaaS **platform** (destination app) | Enterprise **analytics engine** embedded into bank/wealth platforms |
| **Who operates it** | We embed it in our own robo/PFM app | The client logs into Addepar | Wealth managers/banks embed it; advisors consume analytics |
| **UI / reporting** | **None** — JSON only | Best-in-class reporting & dashboards | Risk dashboards/widgets, embeddable components |
| **Analytics philosophy** | Historical, descriptive metrics | Reporting + performance analytics | **Forward-looking factor risk models** (VaR, stress, scenarios) |
| **Buyer** | Internal (we build the experience) | RIAs, family offices, wealth managers | Banks, large wealth managers, institutions |
| **Pricing** | Internal build/run cost (no per-seat) | High per-seat / AUM SaaS | **Very high** enterprise license + integration |
| **Scale fit for mass retail** | ✅ marginal cost ≈ 0 | ❌ expensive | ❌ enterprise-only economics |

**Takeaway:** Aladdin proves our *category* — a headless analytics engine embedded in a wealth
product — is real and valuable. We are the **lightweight, low-cost, Canada-retail-tailored**
point on that same spectrum; Aladdin is the institutional heavyweight. Against Addepar we
compete on **embeddability + cost + domain fit**, not breadth. We don't win or lose purely on a
feature checklist — but a missing metric becomes a felt product gap wherever a customer's
expectation was set by either peer.

---

## 2. Capability comparison matrix

✅ strong · 🟡 partial / limited · ❌ absent

| Capability | Calculation Engine | Addepar | Aladdin (Wealth) |
|---|---|---|---|
| **Time-weighted return (TWR)** | ✅ geometric, monthly | ✅ daily-valued | ✅ |
| **Money-weighted / IRR (MWR)** | ❌ | ✅ TWR+IRR side-by-side | ✅ |
| **Cash-flow handling (deposits/withdrawals)** | ❌ none | ✅ transaction-level | ✅ |
| **Net-of-fee returns** | ❌ gross only (fees separate) | ✅ gross & net | ✅ |
| **Core risk (σ, downside dev, max DD, tracking error)** | ✅ | ✅ | ✅ |
| **Risk-adjusted (Sharpe/Sortino/Treynor/IR/alpha/MAR)** | ✅ full core set | ✅ | ✅ |
| **Factor-based risk model** | ❌ (historical only) | 🟡 | ✅ **signature strength** |
| **VaR / CVaR (expected shortfall)** | ❌ | ✅ | ✅ |
| **Stress testing / scenario analysis** | ❌ | 🟡 | ✅ **signature strength** |
| **Monte-Carlo / goal projection** | ❌ (income-forecast only) | ✅ planning | ✅ |
| **Performance attribution (Brinson)** | ❌ | ✅ | ✅ |
| **Contribution to return / risk** | ❌ | ✅ | ✅ (risk contribution core) |
| **Composition / allocation (sector/geo/style/cap)** | ✅ 19 metrics | ✅ | ✅ |
| **Fund look-through** | 🟡 single-level | ✅ deep | ✅ deep |
| **Fixed-income roll-up (duration/YTM/convexity)** | ❌ (only buckets) | ✅ | ✅ |
| **Multi-currency reporting + FX attribution** | ❌ CAD-only, 8 pairs | ✅ | ✅ |
| **Alternatives / private assets** | ❌ | ✅ | ✅ (via eFront) |
| **Reporting UI / client statements** | ❌ (API only) | ✅ best-in-class | 🟡 widgets, not statements |
| **Embeddable / white-label API** | ✅ **our edge** | 🟡 platform-centric | ✅ (but heavyweight) |
| **Cost at scale (per end-client)** | ✅ marginal ≈ 0 | ❌ expensive | ❌ enterprise-only |
| **Customisation / control of methodology** | ✅ full source control | ❌ vendor-defined | ❌ vendor-defined |
| **Integration effort** | ✅ light (it's ours) | 🟡 | ❌ heavy enterprise programme |
| **GIPS-grade methodology rigor** | 🟡 partial | ✅ | ✅ |
| **Granularity** | 🟡 monthly | ✅ daily | ✅ daily/intraday |
| **Asset/geography breadth** | 🟡 US/CA funds & ETFs (stocks weak) | ✅ global, all asset classes | ✅ global, all asset classes |

---

## 3. Head-to-head analysis

### 3.1 vs. Addepar

**Where we lose (and customers will notice):**
- **Money-weighted return & cash-flow handling.** Addepar shows TWR *and* IRR over real
  transaction histories. For a funded robo account, our TWR-only number is provably not the
  client's dollar experience — Addepar's core selling point hits our weakest spot.
- **Attribution & contribution.** "What drove my return?" is answerable in Addepar, not in ours.
- **Alternatives / private markets & deep look-through.** Addepar's signature capability; we
  have none. Irrelevant for mass-retail, decisive for HNW/family-office.
- **Reporting.** Addepar *is* the report; we produce JSON and own the build.
- **Multi-currency & global breadth.** Addepar is global, multi-currency, all-asset; we're
  CAD-reporting, US/CA funds, with weak direct-equity and off-list-currency handling.

**Where we win:**
- **Cost & scale.** Addepar's per-seat / AUM pricing is prohibitive for mass-market retail at
  low ticket sizes; our marginal cost per end-client is ~zero.
- **Embeddability & UX control.** Analytics render natively inside our app — no "log into
  Addepar." For a consumer robo/PFM, owning the UX end-to-end is the product.
- **Methodology control.** We tune calculations, periods, and edge cases to our product;
  Addepar's methodology is fixed.

**Verdict:** Addepar is a *different tier* — a destination platform for advisor/HNW segments.
We don't compete for that buyer. The threat is **expectation-setting**: users who've seen
Addepar-style reports read the absence of MWR, attribution, and multi-currency as "incomplete."
**Closing MWR + net-of-fee is the highest-leverage move to neutralise the most visible Addepar
advantage at retail.**

### 3.2 vs. BlackRock Aladdin

This is the more instructive comparison because Aladdin is in **our category** — embedded
analytics infrastructure — not a destination app.

**Where we lose:**
- **Forward-looking risk science.** Aladdin's core is a **factor-based risk model** with VaR,
  CVaR, stress testing, and scenario/what-if analysis. Ours is purely **historical/descriptive**
  (realised volatility, realised drawdown). This is the defining capability gap, and it's the
  one Aladdin-aware buyers will immediately probe ("what's my VaR? stress this for a rate shock?").
- **Multi-asset, global, daily/intraday** vs our US/CA-funds, monthly, CAD scope.
- **Attribution, contribution-to-risk, alternatives** — all present in Aladdin, absent in ours.

**Where we win:**
- **Economics.** Aladdin is an enterprise license measured in seven figures plus a multi-month
  integration programme. It is economically impossible for a mass-retail robo at low ticket
  sizes. Our engine's marginal cost is ~zero — this is not a small advantage, it's the entire
  reason a build exists.
- **Speed, simplicity, control.** A single REST dispatch endpoint we own and can change in a
  sprint vs an enterprise risk platform we'd configure for quarters. We can shape metrics to our
  exact Canadian lineup, fee conventions (FUNDS_ONLY / WHOLE_PORTFOLIO, FundServ, sales-charge),
  and periods.
- **Right-sized.** Aladdin's risk depth is overkill for a mass-retail robo; we deliver the
  metrics retail users actually see (returns, allocation, Sharpe-class ratios) without the
  weight.

**Verdict:** Aladdin **validates our category** ("analytics engine as embedded infrastructure
is a real, valuable thing") while being unreachable and overkill for mass retail. We are the
lightweight, low-cost, domain-tailored point on the same spectrum. We cannot and should not
match its risk science for the stated use case — **but VaR / scenario analysis is exactly where
a customer who knows Aladdin will feel our gap most sharply.** If we ever move up-market or face
a risk-conscious partner, a basic VaR + stress capability is the credibility threshold.

---

## 4. Where we genuinely differentiate

1. **Embeddable, headless, API-first** — analytics as infrastructure, invisibly inside a
   consumer robo. Addepar is a destination; Aladdin is the same idea but enterprise-heavy.
2. **Marginal-cost economics** — no per-seat/AUM license per end-client; scales to mass retail
   where both peers are economically out of reach.
3. **Canada-first domain fit** — FundServ, segregated/pooled funds, GIC, CA/US MER &
   sales-charge conventions, CAD base, BoC FX. Generic global engines handle these clumsily.
4. **Full methodology control & auditability** — we own the source and can fix edge cases
   (recent null/zero-guard commits show this in action) and tune to product needs.

## 5. Where we're competitively exposed (priority order)

1. **No money-weighted return / cash-flow awareness** — most visible gap vs *both* peers for a
   funded-account robo. (Quick-to-mid effort, high impact.)
2. **No forward-looking risk (VaR/CVaR/stress/scenario)** — Aladdin's home turf; the credibility
   gap for any risk-conscious buyer.
3. **No attribution / contribution** — table-stakes at both peers ("why did I make/lose money").
4. **Monthly granularity** — understates drawdown, blocks VaR; both peers are daily.
5. **CAD-only / narrow FX & weak direct-equity support** — caps the addressable market beyond
   CA/US fund portfolios.
6. **No UI/reporting layer** — by design (we're an engine), but the whole client experience is
   our build burden; a competitor's polished output sets the bar.

## 6. Strategic recommendation — how to compete

- **Don't fight Addepar on breadth or Aladdin on risk science.** Compete on **embeddability +
  cost + Canadian fit**, and reach *functional parity on the retail-visible essentials*.
- **Sequence:** (1) MWR/IRR + cash flows, (2) net-of-fee toggle, (3) contribution-to-return,
  (4) multi-currency reporting. These close the most-noticed gaps vs both peers at modest effort.
- **Treat a basic VaR + stress capability as the up-market credibility threshold** — not needed
  for mass-retail today, but the first thing to add the moment we face a risk-conscious partner
  or move toward advisor/HNW segments where Aladdin sets expectations.

## 7. One-line competitive verdict

> We are **infrastructure, not a platform** — and Aladdin proves that category is real and
> valuable. Our defensible edge is *embeddable, low-cost, Canada-aware analytics owned
> end-to-end*. We will never out-breadth Addepar or out-risk-model Aladdin, and for mass retail
> we don't need to; but until the engine offers **money-weighted return and net-of-fee
> analytics**, it reads as "incomplete," and **forward-looking risk (VaR/scenario)** is the line
> we must cross before competing anywhere up-market.

---

*Assumptions: competitor capabilities reflect general product knowledge of Addepar and
BlackRock Aladdin / Aladdin Wealth as of early 2026, treated as positioning baselines, not
contract-verified specs. "Our service" facts are from the codebase scan in
`01-calc-engine-findings.md`.*
