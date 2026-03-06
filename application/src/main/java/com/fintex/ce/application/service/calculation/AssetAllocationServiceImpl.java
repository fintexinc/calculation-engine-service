package com.fintex.ce.application.service.calculation;

import com.fintex.ce.application.mapper.response.AssetAllocationResponseMapper;
import com.fintex.ce.domain.enumeration.calculation.AssetAllocationRegion;
import com.fintex.ce.domain.enumeration.calculation.AssetAllocationRegionType;
import com.fintex.ce.domain.model.calculation.AssetAllocationDataDTO;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.application.mapper.AssetAllocationDataMapper;
import com.fintex.ce.port.input.command.PortfolioHoldingsCommand;
import com.fintex.ce.port.input.result.AssetAllocationResult;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.port.output.cache.AssetAllocationCachePort;
import com.fintex.ce.application.service.calculation.breakdown.BreakdownAbstractService;
import com.fintex.ce.util.validation.data.AssetAllocationDataValidator;
import com.fintex.ce.util.validation.data.DataProviderChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.domain.enumeration.DataProvider.DEFAULT_PROVIDERS;
import static com.fintex.ce.util.FilterUtils.getSpecifiedIfEmpty;

@Service
public class AssetAllocationServiceImpl extends BreakdownAbstractService<AssetAllocationResult, AssetAllocationRegion> {

  private final AssetAllocationCachePort assetAllocationCachePort;
  private final AssetAllocationDataValidator assetAllocationDataValidator;
  private final AssetAllocationDataMapper assetAllocationDataMapper;
  private final DataProviderChecker dataProviderChecker;
  private final AssetAllocationResponseMapper responseMapper;

  @Autowired
  public AssetAllocationServiceImpl(final AssetAllocationCachePort assetAllocationCachePort,
      final AssetAllocationDataValidator assetAllocationDataValidator,
      final AssetAllocationDataMapper assetAllocationDataMapper,
      final DataProviderChecker dataProviderChecker,
      final AssetAllocationResponseMapper responseMapper) {
    super();
    this.assetAllocationCachePort = assetAllocationCachePort;
    this.assetAllocationDataValidator = assetAllocationDataValidator;
    this.assetAllocationDataMapper = assetAllocationDataMapper;
    this.dataProviderChecker = dataProviderChecker;
    this.responseMapper = responseMapper;
  }

  @Override
  public AssetAllocationResult calculate(final Map<Holding, Map<AssetAllocationRegion, BigDecimal>> exposures,
      final List<Holding> holdings,
      final List<Warning> warnings) {
    final Map<AssetAllocationRegion, BigDecimal> netProducts = calculateNetProducts(exposures, holdings,
        AssetAllocationRegion.values());
    return responseMapper.fromNetProducts(netProducts, warnings);
  }

  @Override
  public Map<Holding, Map<AssetAllocationRegion, BigDecimal>> getLoadFromCacheStorage(
      final PortfolioHoldingsCommand reqDTO,
      final List<Warning> warnings) {
    final AssetAllocationDataDTO assetAllocationDataDto = assetAllocationCachePort.loadWithDataProvidersCheck(
        reqDTO.getHoldings(),
        getSpecifiedIfEmpty(reqDTO.getDataProviders(), DEFAULT_PROVIDERS),
        warnings);

    dataProviderChecker.check(getSpecifiedIfEmpty(reqDTO.getDataProviders(), DEFAULT_PROVIDERS),
        assetAllocationDataDto);
    assetAllocationDataValidator.validate(assetAllocationDataDto, warnings);
    return assetAllocationDataMapper.mapForAA(assetAllocationDataDto);
  }

  public Map<AssetAllocationRegionType, BigDecimal> calculateAssetAllocationResponse(
      final Map<AssetAllocationRegion, BigDecimal> allocationPerType) {
    final Map<AssetAllocationRegionType, BigDecimal> result = new EnumMap<>(AssetAllocationRegionType.class);
    for (Map.Entry<AssetAllocationRegion, BigDecimal> al : allocationPerType.entrySet()) {
      result.putIfAbsent(al.getKey().getAssetAllocationRegionType(), BigDecimal.ZERO);
      result.computeIfPresent(al.getKey().getAssetAllocationRegionType(), (type, sum) -> sum.add(al.getValue()));
    }
    return result;
  }
}
