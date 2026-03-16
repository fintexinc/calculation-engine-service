package com.fintex.ce.adapter.webclient.sm.fetcher;

import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.model.AssetAllocation;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.port.output.sm.SecurityDataPort;
import com.fintex.ce.port.output.sm.dto.AssetAllocationDto;
import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Wrapper that adapts the new AssetAllocationDto-based SecurityDataPort
 * to the deprecated AssetAllocation domain model for cache layer compatibility.
 */
@Component
public class AssetAllocationSecurityDataPortDeprecated implements SecurityDataPort<AssetAllocation> {

  private static final Map<FinancialInstrumentType, HoldingType> TYPE_MAPPING = new EnumMap<>(FinancialInstrumentType.class);

  static {
    TYPE_MAPPING.put(FinancialInstrumentType.MUTUAL_FUND_CANADA, HoldingType.CANADA_MUTUAL_FUNDS);
    TYPE_MAPPING.put(FinancialInstrumentType.ETF_US, HoldingType.US_ETF);
    TYPE_MAPPING.put(FinancialInstrumentType.ETF_CANADA, HoldingType.CANADA_ETF);
    TYPE_MAPPING.put(FinancialInstrumentType.STOCK_CANADA, HoldingType.CANADA_STOCKS);
    TYPE_MAPPING.put(FinancialInstrumentType.STOCK_US, HoldingType.US_STOCKS);
    TYPE_MAPPING.put(FinancialInstrumentType.CASH, HoldingType.CASH);
    TYPE_MAPPING.put(FinancialInstrumentType.BENCHMARK_INDEX, HoldingType.BENCHMARK_INDEX);
    TYPE_MAPPING.put(FinancialInstrumentType.GIC, HoldingType.GIC);
    TYPE_MAPPING.put(FinancialInstrumentType.MUTUAL_FUND_US, HoldingType.US_MUTUAL_FUNDS);
    TYPE_MAPPING.put(FinancialInstrumentType.HEDGE_FUND_CANADA, HoldingType.CANADA_HEDGE_FUNDS);
    TYPE_MAPPING.put(FinancialInstrumentType.POOLED_FUND_CANADA, HoldingType.CANADA_POOLED_FUNDS);
    TYPE_MAPPING.put(FinancialInstrumentType.SEGREGATED_FUND_CANADA, HoldingType.SEGREGATED_FUND_CANADA);
    TYPE_MAPPING.put(FinancialInstrumentType.FIXED_INCOME, HoldingType.FIXED_INCOME);
    TYPE_MAPPING.put(FinancialInstrumentType.SEPARATELY_MANAGED_ACCOUNT, HoldingType.SEPARATELY_MANAGED_ACCOUNT);
  }

  private final SecurityDataPort<AssetAllocationDto> delegate;

  public AssetAllocationSecurityDataPortDeprecated(SecurityDataPort<AssetAllocationDto> delegate) {
    this.delegate = delegate;
  }

  @Override
  public Map<Holding, AssetAllocation> fetch(List<? extends Holding> holdings, List<DataProvider> providers) {
    Map<Holding, AssetAllocationDto> dtoResult = delegate.fetch(holdings, providers);
    return dtoResult.entrySet().stream()
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            e -> toDeprecatedDomain(e.getValue())
        ));
  }

  private AssetAllocation toDeprecatedDomain(AssetAllocationDto dto) {
    AssetAllocation domain = new AssetAllocation();
    domain.setHoldingType(toHoldingType(dto.getHoldingType()));
    domain.setAssetAllocation(dto.getAssetAllocation());
    domain.setHoldingId(dto.getHoldingId());
    domain.setProvider(dto.getProvider());
    return domain;
  }

  private HoldingType toHoldingType(FinancialInstrumentType type) {
    if (type == null) {
      return null;
    }
    return TYPE_MAPPING.get(type);
  }
}
