package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.calculation.service.breakdown.BreakdownAbstractService;
import com.fintex.ce.application.config.DefaultDataProperties;
import com.fintex.ce.application.mapping.AssetAllocationDataMapper;
import com.fintex.ce.application.mapping.response.AssetAllocationResponseMapper;
import com.fintex.ce.model.domain.calculation.allocation.AssetAllocationRegion;
import com.fintex.ce.model.domain.calculation.allocation.AssetAllocationRegionType;
import com.fintex.ce.model.domain.calculation.allocation.HoldingAssetAllocation;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.allocation.AssetAllocationResult;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.ce.util.ExposureDataHolder;
import com.fintex.wm.commons.domain.DataProvider;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;

import static com.fintex.ce.util.FilterUtils.getSpecifiedIfEmpty;

@Service
@RequiredArgsConstructor
public class AssetAllocationServiceImpl extends BreakdownAbstractService<AssetAllocationResult, AssetAllocationRegion> {

  private final AssetAllocationDataMapper assetAllocationDataMapper;
  private final AssetAllocationResponseMapper responseMapper;
  private final SecurityDataFetcher<HoldingAssetAllocation> securityDataPort;
  private final DefaultDataProperties defaultDataProperties;

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.ASSET_ALLOCATIONS;
  }

  @Override
  public AssetAllocationResult calculate(ExposureDataHolder<AssetAllocationRegion> exposureData,
      List<PortfolioHolding> holdings) {
    var exposures = exposureData.allocations();
    var warnings = new ArrayList<>(exposureData.warnings());
    final Map<AssetAllocationRegion, BigDecimal> netProducts = calculateNetProducts(exposures, holdings,
        AssetAllocationRegion.values());
    return responseMapper.fromNetProducts(netProducts, warnings);
  }

  @Override
  public ExposureDataHolder<AssetAllocationRegion> fetchExposures(PortfolioHoldingsCommand reqDTO) {
    List<DataProvider> providers = getSpecifiedIfEmpty(reqDTO.getDataProviders(),
        defaultDataProperties.getDataProviders());
    Map<PortfolioHolding, HoldingAssetAllocation> allocations = securityDataPort.fetch(reqDTO.getHoldings(), providers);
    return new ExposureDataHolder<>(assetAllocationDataMapper.toRegionExposures(allocations), List.of());
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
