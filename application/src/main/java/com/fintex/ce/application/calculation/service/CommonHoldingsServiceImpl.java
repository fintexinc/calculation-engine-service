package com.fintex.ce.application.calculation.service;

import com.fintex.ce.calculation.CalculationService;
import com.fintex.ce.model.domain.calculation.holding.CommonHoldings;
import com.fintex.ce.model.domain.calculation.holding.HoldingAggregator;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.Holding;
import com.fintex.ce.model.domain.result.correlation.HoldingsKeyResult;
import com.fintex.ce.model.domain.result.holding.TopCommonHoldingData;
import com.fintex.ce.model.domain.result.holding.TopCommonHoldingsResult;
import com.fintex.ce.model.dto.CommonHoldingsDTO;
import com.fintex.ce.model.dto.command.TopCommonHoldingsCommand;
import com.fintex.ce.model.error.Warning;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;

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

import static com.fintex.ce.util.CollectorUtils.toLinkedHashMap;
import static com.fintex.ce.util.DecimalUtils.toUserScale;
import static com.fintex.ce.util.PortfolioUtils.calculateInitialPortfolioWeight;
import static java.math.BigDecimal.ZERO;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

@Service
public class CommonHoldingsServiceImpl
    implements
      CalculationService<TopCommonHoldingsResult, TopCommonHoldingsCommand> {

  private static final int DEFAULT_NUMBER_OF_FUNDS_MIN = 1;
  private static final int DEFAULT_NUMBER_OF_TOP_COMMON_HOLDINGS = 10;
  final Set<String> defaultAccumulateTypes;
  private final SecurityDataFetcher<CommonHoldings> commonHoldingsSecurityDataFetcher;

  public CommonHoldingsServiceImpl(final SecurityDataFetcher<CommonHoldings> commonHoldingsSecurityDataFetcher,
      @Value("#{'${default.top-common-holdings.accumulate-types}'.split(',')}") final Set<String> defaultAccumulateTypes) {
    this.commonHoldingsSecurityDataFetcher = commonHoldingsSecurityDataFetcher;
    this.defaultAccumulateTypes = defaultAccumulateTypes;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.TOP_COMMON_HOLDINGS;
  }

  @Override
  public TopCommonHoldingsResult perform(final TopCommonHoldingsCommand reqDTO) {

    final List<Warning> warnings = new ArrayList<>();
    final Map<Holding, BigDecimal> allocations = calculateInitialPortfolioWeight(reqDTO.getHoldings());

    final Map<Holding, CommonHoldings> rawHoldings = commonHoldingsSecurityDataFetcher.fetch(
        reqDTO.getHoldings(), List.of());
    final Map<Holding, List<CommonHoldingsDTO>> holdings = mapToCommonHoldingsDTOs(rawHoldings);

    final int numberOfMin = getNumOfFundsMin(reqDTO);
    final int numberOfTopCommonHoldings = getTopCommonHoldingsNumber(reqDTO);
    final Set<String> accumulateTypes = getAccumulativeTypes(reqDTO);
    final Map<HoldingAggregator, List<CommonHoldingsDTO>> leaves = calculateTopCommonHoldings(holdings, allocations,
        accumulateTypes);

    final Map<HoldingAggregator, BigDecimal> sortedLeaves = filterTopCommon(numberOfMin, numberOfTopCommonHoldings,
        leaves);
    final List<TopCommonHoldingData> result = toFinalResult(leaves, sortedLeaves);

    TopCommonHoldingsResult response = new TopCommonHoldingsResult();
    response.setCommonHoldings(result);
    response.setWarnings(warnings);
    return response;
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
  public List<TopCommonHoldingData> toFinalResult(final Map<HoldingAggregator, List<CommonHoldingsDTO>> leaves,
      final Map<HoldingAggregator, BigDecimal> sortedLeaves) {
    return sortedLeaves.entrySet().stream().map(e -> mapToFinalResult(leaves, e)).collect(Collectors.toList());
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
  TopCommonHoldingData mapToFinalResult(final Map<HoldingAggregator, List<CommonHoldingsDTO>> leaves,
      final Map.Entry<HoldingAggregator, BigDecimal> sortedLeafEntry) {
    final List<CommonHoldingsDTO> sameLeaves = leaves.get(sortedLeafEntry.getKey());
    final CommonHoldingsDTO leaf = sameLeaves.stream().findFirst().orElseThrow();

    final Set<Holding> parentHoldings = sameLeaves
        .stream().map(CommonHoldingsDTO::getHolding).collect(Collectors.toSet());
    final Set<HoldingsKeyResult> parents = parentHoldings.stream()
        .map(h -> HoldingsKeyResult.buildFromHolding(h, calculateWeightWithinSameLeaves(sameLeaves, h))).collect(
            toSet());

    return new TopCommonHoldingData()
        .setName(sortedLeafEntry.getKey().getNameOrCompanyName())
        .setAllocation(toUserScale(sortedLeafEntry.getValue()))
        .setTicker(leaf.getTicker())
        .setExchangeCode(leaf.getExchangeCode())
        .setHoldingType(leaf.getType())
        .setNumOfFunds(parentHoldings.size())
        .setParentHolding(parents);
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
  BigDecimal calculateWeightWithinSameLeaves(final List<CommonHoldingsDTO> sameLeaves, final Holding parentHolding) {
    return toUserScale(sameLeaves.stream()
        .filter(dto -> dto.getHolding().equals(parentHolding))
        .map(CommonHoldingsDTO::getWeight)
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
  public Map<HoldingAggregator, List<CommonHoldingsDTO>> calculateTopCommonHoldings(
      final Map<Holding, List<CommonHoldingsDTO>> holdings,
      final Map<Holding, BigDecimal> allocations,
      final Set<String> accumulateTypes) {
    return holdings.entrySet().stream()
        // calculate leafs of first level(underlying holdings of 1 lvl which doesn't have 2 lvl underlying holdings)
        .flatMap(eParent -> firstLevelLeaves(allocations, eParent.getKey(), eParent.getValue()))
        // calculate leafs of second level
        .flatMap(this::secondLevelLeaves)
        // filter by accumulative types
        .filter(e -> accumulateTypes.contains(e.getType()))
        // all leaves/children
        .collect(Collectors.groupingBy(CommonHoldingsDTO::aggregator));
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
  Stream<CommonHoldingsDTO> firstLevelLeaves(final Map<Holding, BigDecimal> allocations,
      final Holding holding, final List<CommonHoldingsDTO> firstLevelChildren) {
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
  Stream<CommonHoldingsDTO> secondLevelLeaves(final CommonHoldingsDTO firstLevelChild) {
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
  private Predicate<CommonHoldingsDTO> isNotNullOrEmptyNameOrCompanyName() {
    return dto -> (!Strings.isNullOrEmpty(dto.getCompanyName()) || !Strings.isNullOrEmpty(dto.getName()));
  }

  /**
   * Method is used to check if a user entered numOfFundsMin param. If the number wasn't entered we just take the
   * default number which value is 1 in other case take the user's provided value.
   *
   * @param reqDTO
   *          request DTO.
   * @return numOfFundsMin.
   */
  public int getNumOfFundsMin(final TopCommonHoldingsCommand reqDTO) {
    return isNull(reqDTO.getNumOfFundsMin())
        ? DEFAULT_NUMBER_OF_FUNDS_MIN
        : reqDTO.getNumOfFundsMin();
  }

  /**
   * Method is used to check if a user entered numberOfTopCommonHoldings param. If the number wasn't entered we just
   * take the default number which value is 10 in other case take the user's provided value.
   *
   * @param reqDTO
   *          request DTO.
   * @return numberOfTopCommonHoldings.
   */
  public int getTopCommonHoldingsNumber(final TopCommonHoldingsCommand reqDTO) {
    return isNull(reqDTO.getNumOfTopCommonHoldings())
        ? DEFAULT_NUMBER_OF_TOP_COMMON_HOLDINGS
        : reqDTO.getNumOfTopCommonHoldings();
  }

  /**
   * Method is used to check if a user entered accumulateTypes param. If the types weren't entered we just take the
   * default types. in other case take the user's provided types.
   *
   * @param reqDTO
   *          request DTO.
   * @return accumulative types.
   */
  public Set<String> getAccumulativeTypes(final TopCommonHoldingsCommand reqDTO) {
    return CollectionUtils.isEmpty(reqDTO.getAccumulateHoldingTypes())
        ? defaultAccumulateTypes
        : reqDTO.getAccumulateHoldingTypes();
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
      final Map<HoldingAggregator, List<CommonHoldingsDTO>> leaves) {
    final Map<HoldingAggregator, BigDecimal> totalAllocations = new HashMap<>();
    for (final Map.Entry<HoldingAggregator, List<CommonHoldingsDTO>> entry : leaves.entrySet()) {
      for (final CommonHoldingsDTO dto : entry.getValue()) {
        totalAllocations.computeIfPresent(entry.getKey(), (s, value) -> value.add(dto.getWeight()));
        totalAllocations.putIfAbsent(entry.getKey(), dto.getWeight());
      }
    }

    final Map<HoldingAggregator, BigDecimal> sorted = totalAllocations.entrySet().stream()
        .sorted((o1, o2) -> o2.getValue().compareTo(o1.getValue()))
        .collect(toLinkedHashMap());

    final Map<HoldingAggregator, BigDecimal> filtered = sorted.entrySet().stream()
        .filter(e -> leaves.get(e.getKey()).stream().map(CommonHoldingsDTO::getHolding).distinct()
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
   * @return mapped DTO with calculated weight and set parent.
   */
  CommonHoldingsDTO setParentAndCalculateWeight(final Map<Holding, BigDecimal> allocations,
      final Holding parent,
      final CommonHoldingsDTO child) {
    if (isLeafStock(parent, child)) {
      return child.setHolding(parent).setWeight(toUserScale(child.getValue()));
    }
    final BigDecimal parentWeight = allocations.get(parent);
    final BigDecimal childInitWeight = Optional.ofNullable(child.getValue()).orElse(ZERO);
    final BigDecimal childWeight = parentWeight.multiply(childInitWeight);
    return child
        .setHolding(parent)
        .setWeight(toUserScale(childWeight));
  }

  boolean isLeafStock(final Holding parent, final CommonHoldingsDTO child) {
    return isStockHolding(parent) && child.getCompanyName() != null && child.getType().equals("E");
  }

  private boolean isStockHolding(final Holding holding) {
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
   * @return mapped DTO with calculated weight and set parent.
   */
  CommonHoldingsDTO setParentAndCalculateWeightSecondLvlLeaf(final CommonHoldingsDTO firstLvlParent,
      final CommonHoldingsDTO child) {
    final BigDecimal childInitWeight = Optional.ofNullable(child.getValue()).orElse(ZERO);
    final BigDecimal childWeight = firstLvlParent.getWeight().multiply(childInitWeight);
    return child
        .setHolding(firstLvlParent.getHolding())
        .setWeight(toUserScale(childWeight));
  }

  private Map<Holding, List<CommonHoldingsDTO>> mapToCommonHoldingsDTOs(
      final Map<Holding, CommonHoldings> rawData) {
    return rawData.entrySet().stream()
        .collect(toMap(Map.Entry::getKey, e -> mapHoldingsToDTO(e.getValue().getHoldings())));
  }

  private List<CommonHoldingsDTO> mapHoldingsToDTO(final List<CommonHoldings.CommonHolding> holdings) {
    if (holdings == null) {
      return List.of();
    }
    return holdings.stream()
        .map(h -> {
          var dto = new CommonHoldingsDTO();
          dto.setUuid(h.getUuid());
          dto.setName(h.getName());
          dto.setType(h.getType());
          dto.setValue(h.getValue());
          dto.setTicker(h.getTicker());
          dto.setExchangeCode(h.getExchangeCode());
          dto.setUnderlyingHoldings(mapHoldingsToDTO(h.getUnderlyingHoldings()));
          return dto;
        })
        .toList();
  }

}
