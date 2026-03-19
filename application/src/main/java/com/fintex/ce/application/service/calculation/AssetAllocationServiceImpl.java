package com.fintex.ce.application.service.calculation;

import com.fintex.ce.application.mapper.AssetAllocationDataMapper;
import com.fintex.ce.application.mapper.response.AssetAllocationResponseMapper;
import com.fintex.ce.application.service.calculation.breakdown.BreakdownAbstractService;
import com.fintex.ce.domain.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.domain.model.HoldingAssetAllocation;
import com.fintex.ce.domain.model.calculation.AssetAllocationRegion;
import com.fintex.ce.domain.model.calculation.AssetAllocationRegionType;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.enumeration.DataProvider;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.result.AssetAllocationResult;
import com.fintex.ce.port.sm.SecurityDataFetcher;
import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.fintex.ce.domain.model.enumeration.DataProvider.DEFAULT_PROVIDERS;
import static com.fintex.ce.util.FilterUtils.getSpecifiedIfEmpty;

@Service
@RequiredArgsConstructor
public class AssetAllocationServiceImpl extends BreakdownAbstractService<AssetAllocationResult, AssetAllocationRegion> {

  private final AssetAllocationDataMapper assetAllocationDataMapper;
  private final AssetAllocationResponseMapper responseMapper;
  private final SecurityDataFetcher<HoldingAssetAllocation> securityDataPort;

  @Override
  public AssetAllocationResult calculate(Map<Holding, Map<AssetAllocationRegion, BigDecimal>> exposures,
      List<Holding> holdings,
      List<Warning> warnings) {
    final Map<AssetAllocationRegion, BigDecimal> netProducts = calculateNetProducts(exposures, holdings,
        AssetAllocationRegion.values());
    return responseMapper.fromNetProducts(netProducts, warnings);
  }

  @Override
  public Map<Holding, Map<AssetAllocationRegion, BigDecimal>> fetchExposures(
      PortfolioHoldingsCommand reqDTO,
      List<Warning> warnings) {
    List<DataProvider> providers = getSpecifiedIfEmpty(reqDTO.getDataProviders(), DEFAULT_PROVIDERS);
    Map<Holding, HoldingAssetAllocation> allocations = securityDataPort.fetch(reqDTO.getHoldings(), providers);
    return assetAllocationDataMapper.toRegionExposures(allocations);
  }

  public Map<AssetAllocationRegionType, BigDecimal> calculateAssetAllocationResponse(
      Map<AssetAllocationRegion, BigDecimal> allocationPerType) {
    final Map<AssetAllocationRegionType, BigDecimal> result = new EnumMap<>(AssetAllocationRegionType.class);
    for (Map.Entry<AssetAllocationRegion, BigDecimal> al : allocationPerType.entrySet()) {
      result.putIfAbsent(al.getKey().getAssetAllocationRegionType(), BigDecimal.ZERO);
      result.computeIfPresent(al.getKey().getAssetAllocationRegionType(), (type, sum) -> sum.add(al.getValue()));
    }
    return result;
  }
}
