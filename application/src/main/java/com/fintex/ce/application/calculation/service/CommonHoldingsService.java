package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.calculation.service.HoldingCurrencyConverter.Conversion;
import com.fintex.ce.application.calculation.service.HoldingCurrencyConverter.CurrencyValue;
import com.fintex.ce.application.config.TopHoldingsProperties;
import com.fintex.ce.application.constant.HoldingTypeGroup;
import com.fintex.ce.application.util.SecurityDataValidator;
import com.fintex.ce.calculation.SingleAttributeCalculationService;
import com.fintex.ce.model.domain.calculation.holding.CommonHolding;
import com.fintex.ce.model.domain.calculation.holding.CommonTopHoldings;
import com.fintex.ce.model.domain.calculation.holding.CompositeHolding;
import com.fintex.ce.model.domain.calculation.holding.HoldingAggregator;
import com.fintex.ce.model.domain.calculation.holding.HoldingComponent;
import com.fintex.ce.model.domain.calculation.holding.LeafHolding;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.CashHolding;
import com.fintex.ce.model.domain.holding.GicHolding;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.correlation.HoldingsKeyResult;
import com.fintex.ce.model.domain.result.holding.TopCommonHoldingData;
import com.fintex.ce.model.domain.result.holding.TopCommonHoldingsResult;
import com.fintex.ce.model.dto.command.TopCommonHoldingsCommand;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.util.FilterUtils;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.CompositeSecurityAttribute;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.holding.HoldingType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;
import com.fintex.wm.commons.error.Notification;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.google.common.base.Strings;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

import static com.fintex.ce.application.util.CollectorUtils.toLinkedHashMap;
import static com.fintex.ce.application.util.DecimalUtils.toUserScale;
import static com.fintex.ce.application.util.PortfolioUtils.calculateInitialPortfolioWeightFromValues;
import static com.fintex.ce.model.util.BigDecimalConstants.MATH_CONTEXT;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

@Service
@RequiredArgsConstructor
public class CommonHoldingsService
    implements
      SingleAttributeCalculationService<TopCommonHoldingsCommand, CommonTopHoldings, TopCommonHoldingsResult> {

  private final HoldingCurrencyConverter currencyConverter;
  private final TopHoldingsProperties properties;

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.TOP_COMMON_HOLDINGS;
  }

  /**
   * Reads the holdings table decomposed through nested funds rather than the stored top-holdings column, so a leaf held
   * inside a lower-ranked ETF still contributes its share instead of being dropped with the fund that wraps it.
   */
  @Override
  public CompositeSecurityAttribute requiredAttribute() {
    return CompositeSecurityAttribute.LIMITED_HOLDINGS;
  }

  @Override
  public TopCommonHoldingsResult perform(TopCommonHoldingsCommand command,
      Map<PortfolioHolding, CommonTopHoldings> data) {
    Map<PortfolioHolding, CommonTopHoldings> rawHoldings = FilterUtils.restrictToHoldings(data, command.getHoldings());

    SecurityDataValidator.requireDataForEveryHolding(rawHoldings, command.getHoldings(), this::isSentToSms);
    requireUnderlyingHoldingsForEveryFund(rawHoldings);

    WeightedValues weighted = weightHoldingValuesByTargetCurrency(command.getHoldings(), rawHoldings);
    Map<PortfolioHolding, BigDecimal> allocations = calculateInitialPortfolioWeightFromValues(weighted.values());

    Set<HoldingType> accumulateTypes = getAccumulativeTypes(command);
    Map<HoldingAggregator, List<LeafHolding>> leaves = calculateTopCommonHoldings(rawHoldings, allocations,
        accumulateTypes);

    Map<HoldingAggregator, CommonHolding> representatives = leaves.entrySet().stream()
        .collect(toMap(Map.Entry::getKey, e -> pickRepresentative(e.getValue())));

    Map<HoldingAggregator, BigDecimal> sortedLeaves = filterTopCommon(getTopCommonHoldingsNumber(command), leaves,
        representatives);
    List<TopCommonHoldingData> result = toFinalResult(leaves, sortedLeaves, representatives);

    return TopCommonHoldingsResult.builder()
        .commonHoldings(result)
        .warnings(weighted.warnings())
        .build();
  }

  /**
   * Result of FX-weighting every requested holding's value into the default target currency, together with any
   * non-fatal warnings (e.g. BoC unavailability) emitted along the way.
   */
  private record WeightedValues(Map<PortfolioHolding, BigDecimal> values, List<Notification> warnings) {
  }

  /**
   * Converts each holding's request {@code value} from its source currency to the default target currency before the
   * portfolio-weight denominator is summed. Mirrors the Fees pipeline: currency comes from the holding for cash/GIC,
   * and from the SM {@code TopHoldings.currency} field for everything else. Missing currency on a non-cash/GIC holding
   * is a hard error, because summing mixed currencies as if they were the same would silently corrupt every weight in
   * the result.
   */
  private WeightedValues weightHoldingValuesByTargetCurrency(
      List<PortfolioHolding> requestedHoldings,
      Map<PortfolioHolding, CommonTopHoldings> rawHoldings) {
    Map<PortfolioHolding, CurrencyValue> input = new LinkedHashMap<>();
    for (PortfolioHolding holding : requestedHoldings) {
      input.put(holding, new CurrencyValue(currencyFor(holding, rawHoldings), holding.getValue()));
    }
    Conversion conversion = currencyConverter.convert(input);
    for (PortfolioHolding holding : conversion.missingCurrency()) {
      if (isSentToSms(holding)) {
        throw ErrorCode.HOLDING_MISSING_CURRENCY_FROM_FDS.toExceptionForHolding(holding);
      }
    }
    return new WeightedValues(conversion.converted(), List.copyOf(conversion.warnings()));
  }

  private Currency currencyFor(PortfolioHolding holding, Map<PortfolioHolding, CommonTopHoldings> rawHoldings) {
    return switch (holding) {
      case CashHolding cash -> cash.getCurrency();
      case GicHolding gic -> gic.getCurrency();
      default -> Optional.ofNullable(rawHoldings.get(holding))
          .map(CommonTopHoldings::getCurrency)
          .orElse(null);
    };
  }

  private List<TopCommonHoldingData> toFinalResult(Map<HoldingAggregator, List<LeafHolding>> leaves,
      Map<HoldingAggregator, BigDecimal> sortedLeaves,
      Map<HoldingAggregator, CommonHolding> representatives) {
    return sortedLeaves.entrySet().stream()
        .map(e -> mapToFinalResult(leaves.get(e.getKey()), representatives.get(e.getKey()), e))
        .toList();
  }

  private TopCommonHoldingData mapToFinalResult(List<LeafHolding> sameLeaves, CommonHolding representative,
      Map.Entry<HoldingAggregator, BigDecimal> sortedLeafEntry) {
    Set<PortfolioHolding> parentHoldings = sameLeaves.stream()
        .map(LeafHolding::holding)
        .collect(Collectors.toSet());
    Set<HoldingsKeyResult> parents = parentHoldings.stream()
        .map(h -> HoldingsKeyResult.buildFromHolding(h, toUserScale(weightWithinSameLeaves(sameLeaves, h))))
        .collect(toSet());

    return TopCommonHoldingData.builder()
        .name(sortedLeafEntry.getKey().nameOrCompanyName())
        .allocation(toUserScale(sortedLeafEntry.getValue()))
        .identifier(representative.getPrimaryIdentifier())
        .holdingType(representative.getType())
        .numOfFunds(parentHoldings.size())
        .parentHolding(parents)
        .build();
  }

  /**
   * Single canonical representative for an aggregator group: the leaf source with the lexicographically smallest
   * (idType, id) of its primary identifier (nulls-last). Used for both the sort tiebreak AND the displayed
   * identifier/holdingType so the row presented as #1 is consistent with the value that decided it should be #1.
   */
  private CommonHolding pickRepresentative(List<LeafHolding> sameLeaves) {
    return sameLeaves.stream()
        .map(LeafHolding::source)
        .min(Comparator
            .comparing(CommonHoldingsService::primaryIdType, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(CommonHoldingsService::primaryId, Comparator.nullsLast(Comparator.naturalOrder())))
        .orElseThrow();
  }

  private static String primaryIdType(CommonHolding holding) {
    SecurityIdentifier id = holding.getPrimaryIdentifier();
    return id == null || id.getIdType() == null ? null : id.getIdType().name();
  }

  private static String primaryId(CommonHolding holding) {
    SecurityIdentifier id = holding.getPrimaryIdentifier();
    return id == null ? null : id.getId();
  }

  /**
   * Full-precision sum of leaf weights from one portfolio parent. Rounding happens only at the result-builder boundary
   * so the per-parent allocations stay safe to re-aggregate against the leaf total.
   */
  private BigDecimal weightWithinSameLeaves(List<LeafHolding> sameLeaves, PortfolioHolding parentHolding) {
    return sameLeaves.stream()
        .filter(leaf -> leaf.holding().equals(parentHolding))
        .map(LeafHolding::weight)
        .reduce(BigDecimal::add).orElseThrow();
  }

  private Map<HoldingAggregator, List<LeafHolding>> calculateTopCommonHoldings(
      Map<PortfolioHolding, CommonTopHoldings> rawHoldings,
      Map<PortfolioHolding, BigDecimal> allocations,
      Set<HoldingType> accumulateTypes) {
    return rawHoldings.entrySet().stream()
        .flatMap(e -> {
          List<CommonHolding> firstLevel = Optional.ofNullable(e.getValue().getHoldings()).orElse(List.of());
          BigDecimal portfolioWeight = allocations.get(e.getKey());
          List<HoldingComponent> expanded = firstLevel.stream()
              .map(child -> expand(e.getKey(), portfolioWeight, child, 0, new HashSet<>()))
              .toList();
          return expanded.stream()
              .flatMap(HoldingComponent::leaves)
              .filter(leaf -> hasNameOrCompanyName(leaf.source()))
              .limit(properties.getMaxLeavesPerHolding());
        })
        .filter(leaf -> accumulateTypes.contains(leaf.type()))
        .collect(Collectors.groupingBy(LeafHolding::aggregator));
  }

  /**
   * Recursively expands a holding subtree, multiplying the inherited weight by each node's SM weighting. Returns the
   * {@link HoldingComponent} for the node: a {@link CompositeHolding} when the node is a descended fund (its
   * {@link HoldingComponent#leaves()} flattens to the terminal contributions) or a {@link LeafHolding} when the node is
   * terminal. Recursion stops at the configured max depth or when the same node identity is revisited on the current
   * branch (cycle guard for data we don't control).
   *
   * <p>
   * Leaf-stock short-circuit: when the request holding IS a stock, SM echoes the same equity back as its single
   * underlying. The portfolio allocation already represents the full weight of that equity, so the node's own ratio is
   * not a decomposition factor and is intentionally skipped — unlike the general case below, which multiplies through.
   */
  private HoldingComponent expand(PortfolioHolding portfolioHolding, BigDecimal inheritedWeight, CommonHolding node,
      int depth, Set<String> visited) {

    if (isLeafStock(portfolioHolding, node)) {
      return new LeafHolding(portfolioHolding, inheritedWeight, node);
    }

    if (node.getWeight() == null) {
      throw ErrorCode.HOLDING_MISSING_WEIGHTING_FROM_FDS.toExceptionForHolding(portfolioHolding,
          displayName(node));
    }
    BigDecimal effectiveWeight = inheritedWeight.multiply(node.getWeight(), MATH_CONTEXT);

    if (requiresUnderlyingHoldings(node, depth)) {
      if (CollectionUtils.isEmpty(node.getUnderlyingHoldings())) {
        throw ErrorCode.HOLDING_MISSING_UNDERLYING_HOLDINGS.toExceptionForHolding(portfolioHolding);
      }
      Set<String> nextVisited = new HashSet<>(visited);
      nextVisited.add(identityKey(node));
      List<HoldingComponent> children = node.getUnderlyingHoldings().stream()
          .filter(grand -> !nextVisited.contains(identityKey(grand)))
          .map(grand -> expand(portfolioHolding, effectiveWeight, grand, depth + 1, nextVisited))
          .toList();
      return new CompositeHolding(portfolioHolding, effectiveWeight, node, children);
    }

    return new LeafHolding(portfolioHolding, effectiveWeight, node);
  }

  /**
   * Asks the shared vocabulary instead of matching the shape of the code, because this predicate has to agree with the
   * one SM used when it filled the data in: its {@code HoldingsResolver} decides whether a holding carries nested
   * holdings through {@link HoldingType#isHasUnderlyingHoldings()}. A type CE expands but SM never resolves fails every
   * portfolio that holds it with {@link ErrorCode#HOLDING_MISSING_UNDERLYING_HOLDINGS}, and the spec's
   * <code>(FO|FE|FS|EX|[F].*$)</code> did exactly that to {@code FD} — an {@code F*} code that never nests in the
   * extracts — and would do it again for the next non-nesting {@code F*} code the vendor adds. A holding whose code is
   * outside the vocabulary arrives untyped and is a leaf here for the same reason: that is what SM assumed for it.
   */
  private boolean requiresUnderlyingHoldings(CommonHolding node, int depth) {
    return depth < properties.getMaxRecursionDepth()
        && node.getType() != null
        && node.getType().isHasUnderlyingHoldings();
  }

  /**
   * Cycle-guard identity. Prefers the primary SecurityIdentifier (set by the mapper from MORNINGSTAR_ID → TICKER →
   * FUNDSERV → ISIN → CUSIP); falls back to the display triple only when no identifier is populated.
   */
  private String identityKey(CommonHolding holding) {
    SecurityIdentifier id = holding.getPrimaryIdentifier();
    if (id != null && id.getIdType() != null && !Strings.isNullOrEmpty(id.getId())) {
      return id.getIdType().name() + ":" + id.getId();
    }
    return "name:" + String.join("|",
        Optional.ofNullable(holding.getName()).orElse(""),
        Optional.ofNullable(holding.getCompanyName()).orElse(""),
        Optional.ofNullable(holding.getType()).map(HoldingType::name).orElse(""));
  }

  private boolean hasNameOrCompanyName(CommonHolding holding) {
    return !Strings.isNullOrEmpty(holding.getCompanyName()) || !Strings.isNullOrEmpty(holding.getName());
  }

  private String displayName(CommonHolding holding) {
    return Strings.isNullOrEmpty(holding.getName()) ? holding.getCompanyName() : holding.getName();
  }

  private int getTopCommonHoldingsNumber(TopCommonHoldingsCommand command) {
    return isNull(command.getNumOfTopCommonHoldings())
        ? properties.getDefaultNumOfTopCommonHoldings()
        : command.getNumOfTopCommonHoldings();
  }

  /**
   * The request wins over the configured default when it names any type. A leaf whose vendor code is outside the
   * {@link HoldingType} vocabulary arrives untyped and can never match either set. See
   * {@link com.fintex.ce.application.config.TopHoldingsProperties#getAccumulateTypes()} for why the default is the
   * subset it is.
   */
  private Set<HoldingType> getAccumulativeTypes(TopCommonHoldingsCommand command) {
    return CollectionUtils.isEmpty(command.getAccumulateHoldingTypes())
        ? properties.getAccumulateTypes()
        : command.getAccumulateHoldingTypes();
  }

  private Map<HoldingAggregator, BigDecimal> filterTopCommon(int numberOfTopCommonHoldings,
      Map<HoldingAggregator, List<LeafHolding>> leaves,
      Map<HoldingAggregator, CommonHolding> representatives) {
    Map<HoldingAggregator, BigDecimal> totalAllocations = new LinkedHashMap<>();
    for (Map.Entry<HoldingAggregator, List<LeafHolding>> entry : leaves.entrySet()) {
      for (LeafHolding leaf : entry.getValue()) {
        totalAllocations.merge(entry.getKey(), leaf.weight(), BigDecimal::add);
      }
    }

    return totalAllocations.entrySet().stream()
        .sorted(topCommonComparator(representatives))
        .limit(numberOfTopCommonHoldings)
        .collect(toLinkedHashMap());
  }

  /**
   * Sort: allocation descending, then by the canonical representative's primary identifier (idType, id) ascending —
   * matching the ordering used by {@link #pickRepresentative} — then name as a final tiebreak. Reads from the
   * pre-computed {@code representatives} map so the displayed identifier is the one that actually decided the order.
   */
  private Comparator<Map.Entry<HoldingAggregator, BigDecimal>> topCommonComparator(
      Map<HoldingAggregator, CommonHolding> representatives) {
    return Map.Entry.<HoldingAggregator, BigDecimal>comparingByValue().reversed()
        .thenComparing(e -> primaryIdType(representatives.get(e.getKey())),
            Comparator.nullsLast(Comparator.naturalOrder()))
        .thenComparing(e -> primaryId(representatives.get(e.getKey())),
            Comparator.nullsLast(Comparator.naturalOrder()))
        .thenComparing(e -> e.getKey().nameOrCompanyName(), Comparator.nullsLast(Comparator.naturalOrder()));
  }

  private boolean isLeafStock(PortfolioHolding parent, CommonHolding child) {
    return isStockHolding(parent) && child.getCompanyName() != null && child.getType() == CommonHolding.EQUITY_TYPE;
  }

  /**
   * Generic stock check via the {@code STOCK} parent in the {@link FinancialInstrumentType} hierarchy. Country is
   * carried as a separate dimension, so this matches stocks of any country.
   */
  private boolean isStockHolding(PortfolioHolding holding) {
    return FilterUtils.isOfType(holding.getHoldingType(), FinancialInstrumentType.STOCK);
  }

  /**
   * Holdings the engine sends to Security Master and therefore expects back in the response. Cash and GIC are skipped
   * upstream (see {@link FilterUtils#LOCALLY_SOURCED_TYPES}); everything else (stocks, fixed income, all fund-like
   * instruments) must be returned.
   */
  private boolean isSentToSms(PortfolioHolding holding) {
    return !FilterUtils.LOCALLY_SOURCED_TYPES.contains(holding.getHoldingType());
  }

  /**
   * Spec rule: mutual funds / ETFs / pooled funds with an empty underlying-holdings list -> TCH-001 error. Restricted
   * to fund-like types because direct stock or fixed-income holdings legitimately have no underlying-holdings list. The
   * complementary case (the holding wasn't returned by SM at all) is covered by
   * {@link SecurityDataValidator#requireDataForEveryHolding}.
   */
  private void requireUnderlyingHoldingsForEveryFund(Map<PortfolioHolding, CommonTopHoldings> rawHoldings) {
    rawHoldings.entrySet().stream()
        .filter(e -> HoldingTypeGroup.MER_BEARING_TYPES.contains(e.getKey().getHoldingType()))
        .filter(e -> e.getValue() == null || CollectionUtils.isEmpty(e.getValue().getHoldings()))
        .findFirst()
        .ifPresent(e -> {
          throw ErrorCode.HOLDING_MISSING_UNDERLYING_HOLDINGS.toExceptionForHolding(e.getKey());
        });
  }

}
