package com.fintex.ce.service.impl.calculation;

import com.fintex.ce.config.enumeration.calculation.AssetAllocationRegion;
import com.fintex.ce.config.enumeration.calculation.AssetAllocationRegionType;
import com.fintex.ce.dto.calculation.AssetAllocationDataDTO;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.mapper.AssetAllocationDataMapper;
import com.fintex.ce.dto.request.PortfolioHoldingsReqDTO;
import com.fintex.ce.dto.response.AssetAllocationResDTO;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.service.impl.cache.AssetAllocationCacheStorage;
import com.fintex.ce.service.impl.calculation.breakdown.BreakdownAbstractService;
import com.fintex.ce.util.validation.data.AssetAllocationDataValidator;
import com.fintex.ce.util.validation.data.DataProviderChecker;
import com.fintex.ce.util.validation.request.PortfolioHoldingsReqDtoValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.config.enumeration.DataProvider.DEFAULT_PROVIDERS;
import static com.fintex.ce.util.DecimalUtils.toUserScale;
import static com.fintex.ce.util.FilterUtils.getSpecifiedIfEmpty;

@Service
public class AssetAllocationServiceImpl extends BreakdownAbstractService<AssetAllocationResDTO, AssetAllocationRegion> {

    private final AssetAllocationCacheStorage assetAllocationCacheStorage;
    private final AssetAllocationDataValidator assetAllocationDataValidator;
    private final AssetAllocationDataMapper assetAllocationDataMapper;
    private final DataProviderChecker dataProviderChecker;

    @Autowired
    public AssetAllocationServiceImpl(final AssetAllocationCacheStorage assetAllocationCacheStorage,
                                      final AssetAllocationDataValidator assetAllocationDataValidator,
                                      final AssetAllocationDataMapper assetAllocationDataMapper,
                                      final DataProviderChecker dataProviderChecker,
                                      final PortfolioHoldingsReqDtoValidator requestValidator) {
        super(requestValidator);
        this.assetAllocationCacheStorage = assetAllocationCacheStorage;
        this.assetAllocationDataValidator = assetAllocationDataValidator;
        this.assetAllocationDataMapper = assetAllocationDataMapper;
        this.dataProviderChecker = dataProviderChecker;
    }

    @Override
    public AssetAllocationResDTO calculate(final Map<Holding, Map<AssetAllocationRegion, BigDecimal>> exposures,
                                           final List<Holding> holdings,
                                           final List<Warning> warnings) {
        final Map<AssetAllocationRegion, BigDecimal> netProducts = calculateNetProducts(exposures, holdings, AssetAllocationRegion.values());
        final Map<AssetAllocationRegionType, BigDecimal> assetAllocationResponse = calculateAssetAllocationResponse(netProducts);
        return new AssetAllocationResDTO(toUserScale(assetAllocationResponse), warnings);
    }

    @Override
    public Map<Holding, Map<AssetAllocationRegion, BigDecimal>> getLoadFromCacheStorage(final PortfolioHoldingsReqDTO reqDTO,
                                                                                        final List<Warning> warnings) {
        final AssetAllocationDataDTO assetAllocationDataDto = assetAllocationCacheStorage.loadWithDataProvidesCheck(
                reqDTO.getHoldings(),
                getSpecifiedIfEmpty(reqDTO.getDataProviders(), DEFAULT_PROVIDERS),
                warnings);

        dataProviderChecker.check(getSpecifiedIfEmpty(reqDTO.getDataProviders(), DEFAULT_PROVIDERS), assetAllocationDataDto);
        assetAllocationDataValidator.validate(assetAllocationDataDto, warnings);
        return assetAllocationDataMapper.mapForAA(assetAllocationDataDto);
    }

    Map<AssetAllocationRegionType, BigDecimal> calculateAssetAllocationResponse(final Map<AssetAllocationRegion, BigDecimal> allocationPerType) {
        final Map<AssetAllocationRegionType, BigDecimal> result = new EnumMap<>(AssetAllocationRegionType.class);
        for (Map.Entry<AssetAllocationRegion, BigDecimal> al : allocationPerType.entrySet()) {
            result.putIfAbsent(al.getKey().getAssetAllocationRegionType(), BigDecimal.ZERO);
            result.computeIfPresent(al.getKey().getAssetAllocationRegionType(), (type, sum) -> sum.add(al.getValue()));
        }
        return result;
    }
}
