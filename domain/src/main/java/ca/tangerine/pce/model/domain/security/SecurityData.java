package ca.tangerine.pce.model.domain.security;

import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.wm.commons.domain.enumeration.CompositeSecurityAttribute;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Immutable per-holding Market Investment Catalogue data passed into calculation services. Portfolio and benchmark data
 * live in two separate sections — the same security may appear in both the portfolio and the benchmark holdings, so the
 * two sides must never share one lookup table. Each section holds one per-holding map per fetched
 * {@link CompositeSecurityAttribute}; a missing attribute yields an empty map, so consumers treat "attribute not
 * fetched" and "no data returned" uniformly. The values are the CE domain objects the attribute maps to (e.g.
 * {@code HoldingAssetAllocation} for {@code ASSET_ALLOCATION}); the accessors cast to the caller's expected domain
 * type, which is guaranteed by the adapter-side attribute binding registry.
 */
public final class SecurityData {

  public static final SecurityData EMPTY = new SecurityData(Map.of(), Map.of());

  private final Map<CompositeSecurityAttribute, Map<PortfolioHolding, Object>> portfolioData;
  private final Map<CompositeSecurityAttribute, Map<PortfolioHolding, Object>> benchmarkData;

  private SecurityData(Map<CompositeSecurityAttribute, Map<PortfolioHolding, Object>> portfolioData,
      Map<CompositeSecurityAttribute, Map<PortfolioHolding, Object>> benchmarkData) {
    this.portfolioData = Map.copyOf(portfolioData);
    this.benchmarkData = Map.copyOf(benchmarkData);
  }

  public static SecurityData of(Map<CompositeSecurityAttribute, Map<PortfolioHolding, Object>> portfolioData) {
    return of(portfolioData, Map.of());
  }

  public static SecurityData of(Map<CompositeSecurityAttribute, Map<PortfolioHolding, Object>> portfolioData,
      Map<CompositeSecurityAttribute, Map<PortfolioHolding, Object>> benchmarkData) {
    Map<CompositeSecurityAttribute, Map<PortfolioHolding, Object>> portfolio = portfolioData == null
        ? Map.of()
        : portfolioData;
    Map<CompositeSecurityAttribute, Map<PortfolioHolding, Object>> benchmark = benchmarkData == null
        ? Map.of()
        : benchmarkData;
    return portfolio.isEmpty() && benchmark.isEmpty() ? EMPTY : new SecurityData(portfolio, benchmark);
  }

  public static SecurityData ofAttribute(CompositeSecurityAttribute attribute, Map<PortfolioHolding, ?> values) {
    return builder().with(attribute, values).build();
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {

    private final Map<CompositeSecurityAttribute, Map<PortfolioHolding, Object>> portfolioData = new HashMap<>();
    private final Map<CompositeSecurityAttribute, Map<PortfolioHolding, Object>> benchmarkData = new HashMap<>();

    public Builder with(CompositeSecurityAttribute attribute, Map<PortfolioHolding, ?> values) {
      portfolioData.put(attribute, new HashMap<>(values));
      return this;
    }

    public Builder withBenchmark(CompositeSecurityAttribute attribute, Map<PortfolioHolding, ?> values) {
      benchmarkData.put(attribute, new HashMap<>(values));
      return this;
    }

    public SecurityData build() {
      return of(portfolioData, benchmarkData);
    }
  }

  @SuppressWarnings("unchecked")
  public <D> Map<PortfolioHolding, D> get(CompositeSecurityAttribute attribute) {
    return (Map<PortfolioHolding, D>) portfolioData.getOrDefault(attribute, Map.of());
  }

  @SuppressWarnings("unchecked")
  public <D> Map<PortfolioHolding, D> getBenchmark(CompositeSecurityAttribute attribute) {
    return (Map<PortfolioHolding, D>) benchmarkData.getOrDefault(attribute, Map.of());
  }

  /**
   * Returns the portfolio attribute data restricted to the given holdings, mirroring what a per-holding fetch for
   * exactly that subset would have produced. The returned map is mutable.
   */
  public <D> Map<PortfolioHolding, D> get(CompositeSecurityAttribute attribute,
      List<? extends PortfolioHolding> holdings) {
    Map<PortfolioHolding, D> all = get(attribute);
    if (holdings == null) {
      return new HashMap<>();
    }
    return holdings.stream()
        .filter(all::containsKey)
        .collect(Collectors.toMap(Function.identity(), all::get,
            (existing, duplicate) -> existing, HashMap::new));
  }

  public Map<CompositeSecurityAttribute, Map<PortfolioHolding, Object>> asMap() {
    return portfolioData;
  }

}
