package com.fintex.ce.application.calculation.service;

import com.fintex.ce.calculation.CalculationService;
import com.fintex.ce.model.domain.calculation.holding.CommonHolding;
import com.fintex.ce.model.domain.calculation.holding.CommonTopHoldings;
import com.fintex.ce.model.domain.calculation.holding.HoldingAggregator;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.correlation.HoldingsKeyResult;
import com.fintex.ce.model.domain.result.holding.TopCommonHoldingData;
import com.fintex.ce.model.domain.result.holding.TopCommonHoldingsResult;
import com.fintex.ce.model.dto.command.TopCommonHoldingsCommand;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;
import com.fintex.wm.commons.error.Notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.google.common.base.Strings;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.fintex.ce.application.util.CollectorUtils.toLinkedHashMap;
import static com.fintex.ce.application.util.DecimalUtils.toUserScale;
import static com.fintex.ce.application.util.PortfolioUtils.calculateInitialPortfolioWeight;
import static java.math.BigDecimal.ZERO;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

@Service
public class CommonHoldingsServiceImpl
    implements
      CalculationService<TopCommonHoldingsCommand, TopCommonHoldingsResult> {

  private static final int DEFAULT_NUMBER_OF_FUNDS_MIN = 1;
  private static final int DEFAULT_NUMBER_OF_TOP_COMMON_HOLDINGS = 10;
  final Set<String> defaultAccumulateTypes;
  private final SecurityDataFetcher<CommonTopHoldings> commonHoldingsSecurityDataFetcher;

  public CommonHoldingsServiceImpl(final SecurityDataFetcher<CommonTopHoldings> commonHoldingsSecurityDataFetcher,
      @Value("#{'${default.top-common-holdings.accumulate-types}'.split(',')}") final Set<String> defaultAccumulateTypes) {
    this.commonHoldingsSecurityDataFetcher = commonHoldingsSecurityDataFetcher;
    this.defaultAccumulateTypes = defaultAccumulateTypes;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.TOP_COMMON_HOLDINGS;
  }

  @Override
  public TopCommonHoldingsResult perform(final TopCommonHoldingsCommand command) {

    final List<Notification> warnings = new ArrayList<>();
    final Map<PortfolioHolding, BigDecimal> allocations = calculateInitialPortfolioWeight(command.getHoldings());

    final Map<PortfolioHolding, CommonTopHoldings> rawHoldings = commonHoldingsSecurityDataFetcher.fetch(
        command.getHoldings(), List.of());
    final Map<PortfolioHolding, List<CommonHolding>> holdings = mapToCommonHoldingsDtos(rawHoldings);

    final int numberOfMin = getNumOfFundsMin(command);
    final int numberOfTopCommonHoldings = getTopCommonHoldingsNumber(command);
    final Set<String> accumulateTypes = getAccumulativeTypes(command);
    final Map<HoldingAggregator, List<CommonHolding>> leaves = calculateTopCommonHoldings(holdings, allocations,
        accumulateTypes);

    final Map<HoldingAggregator, BigDecimal> sortedLeaves = filterTopCommon(numberOfMin, numberOfTopCommonHoldings,
        leaves);
    final List<TopCommonHoldingData> result = toFinalResult(leaves, sortedLeaves);

    return TopCommonHoldingsResult.builder()
        .commonHoldings(result)
        .warnings(warnings)
        .build();
  }

  /**
   * Method is used to map the common holding to the final view.
   *
   * @param leaves
   *          all the leaves.
   * @param sortedLeaves
   *          all sorted(applicable) leaves.
   * @return final result.
   */
  public List<TopCommonHoldingData> toFinalResult(final Map<HoldingAggregator, List<CommonHolding>> leaves,
      final Map<HoldingAggregator, BigDecimal> sortedLeaves) {
    return sortedLeaves.entrySet().stream().map(e -> mapToFinalResult(leaves, e)).toList();
  }

  /**
   * Method is used to map the leaf to the final view.
   *
   * @param leaves
   *          all the leaves.
   * @param sortedLeafEntry
   *          sorted leaves.
   * @return mapped to final result "leaf".
   */
  TopCommonHoldingData mapToFinalResult(final Map<HoldingAggregator, List<CommonHolding>> leaves,
      final Map.Entry<HoldingAggregator, BigDecimal> sortedLeafEntry) {
    final List<CommonHolding> sameLeaves = leaves.get(sortedLeafEntry.getKey());
    final CommonHolding leaf = sameLeaves.stream().findFirst().orElseThrow();

    final Set<PortfolioHolding> parentHoldings = sameLeaves
        .stream().map(CommonHolding::getHolding).collect(Collectors.toSet());
    final Set<HoldingsKeyResult> parents = parentHoldings.stream()
        .map(h -> HoldingsKeyResult.buildFromHolding(h, calculateWeightWithinSameLeaves(sameLeaves, h))).collect(
            toSet());

    return TopCommonHoldingData.builder()
        .name(sortedLeafEntry.getKey().getNameOrCompanyName())
        .allocation(toUserScale(sortedLeafEntry.getValue()))
        .ticker(leaf.getTicker())
        .exchangeCode(leaf.getExchangeCode())
        .holdingType(leaf.getType())
        .numOfFunds(parentHoldings.size())
        .parentHolding(parents)
        .build();
  }

  /**
   * Calculates weight if the same leaves have the same parent, we just sum up their values.
   *
   * @param sameLeaves
   *          same leaves.
   * @param parentHolding
   *          parent.
   * @return calculated weight.
   */
  BigDecimal calculateWeightWithinSameLeaves(final List<CommonHolding> sameLeaves,
      final PortfolioHolding parentHolding) {
    return toUserScale(sameLeaves.stream()
        .filter(holding -> holding.getHolding().equals(parentHolding))
        .map(CommonHolding::getWeight)
        .reduce(BigDecimal::add).orElseThrow());
  }

  /**
   * Method is used to find all the first and second level leafs and aggregate them using {@HoldingAggregator}. We
   * aggregate it by Name. But if the companyName and the holding type is "E" then aggregate by these params.
   *
   * @param holdings
   *          map of holdings where the key is parent holding(request holding), the value - list of underlying holdings
   *          where each underlying holding(first level) can have any amount of underlying holdings(second level).
   * @param allocations
   *          allocations for parent holdings, where the key - parent holding, the value - allocated value. This
   *          calculates in the way where the value of certain holding divides on the sum of values of all request
   *          holdings.
   * @param accumulateTypes
   *          accumulate types.
   * @return map of aggregated holdings where the key is aggregator and the value - List of common holdings;
   */
  public Map<HoldingAggregator, List<CommonHolding>> calculateTopCommonHoldings(
      final Map<PortfolioHolding, List<CommonHolding>> holdings,
      final Map<PortfolioHolding, BigDecimal> allocations,
      final Set<String> accumulateTypes) {
    return holdings.entrySet().stream()
        // calculate leafs of first level(underlying holdings of 1 lvl which doesn't have 2 lvl underlying holdings)
        .flatMap(eParent -> firstLevelLeaves(allocations, eParent.getKey(), eParent.getValue()))
        // calculate leafs of second level
        .flatMap(this::secondLevelLeaves)
        // filter by accumulative types
        .filter(e -> accumulateTypes.contains(e.getType()))
        // all leaves/children
        .collect(Collectors.groupingBy(CommonHolding::aggregator));
  }

  /**
   * Method is used to calculate all the "leafs" of first level.
   *
   * @param allocations
   *          allocations for parent holdings, where the key - parent holding, the value - allocated value. This
   *          calculates in the way where the value of certain holding divides on the sum of values of all request
   *          holdings.
   * @param holding
   *          parent(request) holding.
   * @param firstLevelChildren
   *          all the underlying holdings of parent holding.
   * @return stream of grouped first lvl holdings. The first level holding is a "leaf" if it doesn't have second lvl
   *         underlying holdings.
   */
  Stream<CommonHolding> firstLevelLeaves(final Map<PortfolioHolding, BigDecimal> allocations,
      final PortfolioHolding holding, final List<CommonHolding> firstLevelChildren) {
    return firstLevelChildren.stream().peek(child -> setParentAndCalculateWeight(allocations, holding, child));
  }

  /**
   * Method is used to calculate all the "leafs" of second level.
   *
   * @param firstLevelChild
   *          first level holding. "Parent" of second lvl underlying holdings.
   * @return stream of grouped second lvl holdings. The second level holding is the final "leaf" since we have only
   *         three levels: (0lvl - parent(request holding), 1lvl - first lvl of underlying holdings of (0)parent
   *         holding, 2 lvl - second lvl of underlying holdings of 1lvl "parent" holding)
   */
  Stream<CommonHolding> secondLevelLeaves(final CommonHolding firstLevelChild) {
    if (CollectionUtils.isEmpty(firstLevelChild.getUnderlyingHoldings())) {
      // weight is already calculated for this one
      return Stream.of(firstLevelChild);
    }
    return firstLevelChild.getUnderlyingHoldings().stream()
        .filter(isNotNullOrEmptyNameOrCompanyName())
        .peek(child2 -> setParentAndCalculateWeightSecondLvlLeaf(firstLevelChild, child2));
  }

  /**
   * Method is used to check whether name or companyName is present.
   *
   * @return predicate.
   */
  private Predicate<CommonHolding> isNotNullOrEmptyNameOrCompanyName() {
    return holding -> (!Strings.isNullOrEmpty(holding.getCompanyName()) || !Strings.isNullOrEmpty(holding.getName()));
  }

  /**
   * Method is used to check if a user entered numOfFundsMin param. If the number wasn't entered we just take the
   * default number which value is 1 in other case take the user's provided value.
   *
   * @param command
   *          command.
   * @return numOfFundsMin.
   */
  public int getNumOfFundsMin(final TopCommonHoldingsCommand command) {
    return isNull(command.getNumOfFundsMin())
        ? DEFAULT_NUMBER_OF_FUNDS_MIN
        : command.getNumOfFundsMin();
  }

  /**
   * Method is used to check if a user entered numberOfTopCommonHoldings param. If the number wasn't entered we just
   * take the default number which value is 10 in other case take the user's provided value.
   *
   * @param command
   *          command.
   * @return numberOfTopCommonHoldings.
   */
  public int getTopCommonHoldingsNumber(final TopCommonHoldingsCommand command) {
    return isNull(command.getNumOfTopCommonHoldings())
        ? DEFAULT_NUMBER_OF_TOP_COMMON_HOLDINGS
        : command.getNumOfTopCommonHoldings();
  }

  /**
   * Method is used to check if a user entered accumulateTypes param. If the types weren't entered we just take the
   * default types. in other case take the user's provided types.
   *
   * @param command
   *          command.
   * @return accumulative types.
   */
  public Set<String> getAccumulativeTypes(final TopCommonHoldingsCommand command) {
    return CollectionUtils.isEmpty(command.getAccumulateHoldingTypes())
        ? defaultAccumulateTypes
        : command.getAccumulateHoldingTypes();
  }

  /**
   * Method is used to filter Top Common Holdings based on numberOfTopCommonHoldings parameter. Common holdings are the
   * ones which amount of parent holdings is more or equal to the numberOfFundsMin. Default numberOfTopCommonHoldings is
   * 10 If there are less than 10 applicable Top Common Holdings, sho up to 10 common holdings.
   *
   * @param numberOfMin
   *          numberOfFundsMin param.
   * @param numberOfTopCommonHoldings
   *          numberOfTopCommonHoldings param.
   * @param leaves
   *          all the applicable leaves(1 or 2 lvl common holdings)
   * @return filtered Top 10 Common Holdings.
   */
  public Map<HoldingAggregator, BigDecimal> filterTopCommon(final int numberOfMin,
      final int numberOfTopCommonHoldings,
      final Map<HoldingAggregator, List<CommonHolding>> leaves) {
    final Map<HoldingAggregator, BigDecimal> totalAllocations = new HashMap<>();
    for (final Map.Entry<HoldingAggregator, List<CommonHolding>> entry : leaves.entrySet()) {
      for (final CommonHolding holding : entry.getValue()) {
        totalAllocations.computeIfPresent(entry.getKey(), (s, value) -> value.add(holding.getWeight()));
        totalAllocations.putIfAbsent(entry.getKey(), holding.getWeight());
      }
    }

    final Map<HoldingAggregator, BigDecimal> sorted = totalAllocations.entrySet().stream()
        .sorted((o1, o2) -> o2.getValue().compareTo(o1.getValue()))
        .collect(toLinkedHashMap());

    final Map<HoldingAggregator, BigDecimal> filtered = sorted.entrySet().stream()
        .filter(e -> leaves.get(e.getKey()).stream().map(CommonHolding::getHolding).distinct()
            .count() >= numberOfMin)
        .limit(numberOfTopCommonHoldings)
        .collect(toLinkedHashMap());

    if (filtered.size() < numberOfTopCommonHoldings && sorted.size() > 0) {
      int size = numberOfTopCommonHoldings - filtered.size();
      final LinkedHashMap<HoldingAggregator, BigDecimal> missingLeaves = sorted.entrySet().stream()
          .filter(e -> !filtered.containsKey(e.getKey())).limit(size).collect(toLinkedHashMap());
      filtered.putAll(missingLeaves);
    }

    return filtered;
  }

  /**
   * Method is used to set the parent of underlying holding of first level and calculate its weight. Calculates in next
   * way: 0lvl holding * 1lvl child
   *
   * @param allocations
   *          allocations for parent holdings, where the key - parent holding, the value - allocated value. This
   *          calculates in the way where the value of certain holding divides on the sum of values of all request
   *          holdings.
   * @param parent
   *          parent holding. Can be 0lvl holding(as parent) for 1lvl underlying holding or 1lvl holding(as parent) for
   *          1lvl underlying holding.
   * @param child
   *          child holding. Can be 1lvl of 2lvl holding.
   * @return mapped CommonHolding with calculated weight and set parent.
   */
  CommonHolding setParentAndCalculateWeight(final Map<PortfolioHolding, BigDecimal> allocations,
      final PortfolioHolding parent,
      final CommonHolding child) {
    if (isLeafStock(parent, child)) {
      child.setHolding(parent);
      child.setWeight(toUserScale(child.getValue()));
      return child;
    }
    final BigDecimal parentWeight = allocations.get(parent);
    final BigDecimal childInitWeight = Optional.ofNullable(child.getValue()).orElse(ZERO);
    final BigDecimal childWeight = parentWeight.multiply(childInitWeight);
    child.setHolding(parent);
    child.setWeight(toUserScale(childWeight));
    return child;
  }

  boolean isLeafStock(final PortfolioHolding parent, final CommonHolding child) {
    return isStockHolding(parent) && child.getCompanyName() != null && child.getType().equals("E");
  }

  private boolean isStockHolding(final PortfolioHolding holding) {
    return FinancialInstrumentType.STOCK_CANADA.equals(holding.getHoldingType())
        || FinancialInstrumentType.STOCK_US.equals(holding.getHoldingType());
  }

  /**
   * Method is used to set the parent of underlying holding of second level and calculate its weight. Calculates in next
   * way: 1lvl holding * 2lvl child
   *
   * @param firstLvlParent
   *          parent holding. Can be 0lvl holding(as parent) for 1lvl underlying holding or 1lvl holding(as parent) for
   *          1lvl underlying holding.
   * @param child
   *          child holding. Can be 1lvl of 2lvl holding.
   * @return mapped CommonHolding with calculated weight and set parent.
   */
  CommonHolding setParentAndCalculateWeightSecondLvlLeaf(final CommonHolding firstLvlParent,
      final CommonHolding child) {
    final BigDecimal childInitWeight = Optional.ofNullable(child.getValue()).orElse(ZERO);
    final BigDecimal childWeight = firstLvlParent.getWeight().multiply(childInitWeight);
    child.setHolding(firstLvlParent.getHolding());
    child.setWeight(toUserScale(childWeight));
    return child;
  }

  private Map<PortfolioHolding, List<CommonHolding>> mapToCommonHoldingsDtos(
      final Map<PortfolioHolding, CommonTopHoldings> rawData) {
    return rawData.entrySet().stream()
        .collect(toMap(Map.Entry::getKey, e -> mapToCommonHoldings(e.getValue().getHoldings())));
  }

  private String extractIdentifier(CommonTopHoldings.CommonTopHolding holding, FiIdentifierType type) {
    return Optional.ofNullable(holding.getIdentifiers())
        .orElse(List.of())
        .stream()
        .filter(id -> type.equals(id.getIdType()))
        .map(SecurityIdentifier::getId)
        .findFirst()
        .orElse(null);
  }

  private List<CommonHolding> mapToCommonHoldings(final List<CommonTopHoldings.CommonTopHolding> holdings) {
    if (holdings == null) {
      return List.of();
    }
    return holdings.stream()
        .map(h -> {
          var holding = new CommonHolding();
          holding.setName(h.getName());
          holding.setType(h.getType());
          holding.setValue(h.getValue());
          holding.setTicker(extractIdentifier(h, FiIdentifierType.TICKER));
          holding.setExchangeCode(extractIdentifier(h, FiIdentifierType.EXCHANGE_ID));
          holding.setUnderlyingHoldings(mapToCommonHoldings(h.getUnderlyingHoldings()));
          return holding;
        })
        .toList();
  }

}
