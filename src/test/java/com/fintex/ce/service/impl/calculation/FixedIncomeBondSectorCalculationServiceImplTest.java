package com.fintex.ce.service.impl.calculation;

import com.fintex.ce.config.enumeration.calculation.AssetAllocationRegion;
import com.fintex.ce.config.enumeration.calculation.FixedIncomeSectorType;
import com.fintex.ce.dto.calculation.AssetAllocationDataDTO;
import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.mapper.AssetAllocationDataMapper;
import com.fintex.ce.dto.request.PortfolioHoldingsReqDTO;
import com.fintex.ce.dto.response.FixedIncomeSectorResDTO;
import com.fintex.ce.service.impl.cache.AssetAllocationCacheStorage;
import com.fintex.ce.service.impl.cache.FixedIncomeBondSectorCacheStorage;
import com.fintex.ce.util.PortfolioUtils;
import com.fintex.ce.util.validation.data.AssetAllocationDataValidator;
import com.fintex.ce.util.validation.request.PortfolioHoldingsReqDtoValidator;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static java.math.BigDecimal.TEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class FixedIncomeBondSectorCalculationServiceImplTest {

    @Test
    void calculate_verifyAreAllValuesZerosInMapOfExposure() {
        try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
            //SETUP
            final var requestValidator = mock(PortfolioHoldingsReqDtoValidator.class);
            final var fixedIncomeBondSectorCacheStorage = mock(FixedIncomeBondSectorCacheStorage.class);
            final AssetAllocationCacheStorage assetAllocationCacheStorage = mock(AssetAllocationCacheStorage.class);
            final AssetAllocationDataMapper assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
            final AssetAllocationDataValidator assetAllocationDataValidator = mock(AssetAllocationDataValidator.class);

            final var sut = mock(FixedIncomeBondSectorCalculationServiceImpl.class, withSettings()
                    .useConstructor(fixedIncomeBondSectorCacheStorage, requestValidator, assetAllocationCacheStorage, assetAllocationDataMapper, assetAllocationDataValidator));

            final var exposures = mock(Map.class);

            doCallRealMethod().when(sut).calculate(any(), any(), any());
            //ACT
            sut.calculate(exposures, List.of(), List.of());

            //VERIFY
            mockedPortfolioUtils.verify(() -> PortfolioUtils.areAllValuesZerosInMap(exposures));
        }
    }

    @Test
    void calculate_checkResultWhenExposureIsAllZeroValuesMap() {
        try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
            //SETUP
            final var requestValidator = mock(PortfolioHoldingsReqDtoValidator.class);
            final var fixedIncomeBondSectorCacheStorage = mock(FixedIncomeBondSectorCacheStorage.class);
            final AssetAllocationCacheStorage assetAllocationCacheStorage = mock(AssetAllocationCacheStorage.class);
            final AssetAllocationDataMapper assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
            final AssetAllocationDataValidator assetAllocationDataValidator = mock(AssetAllocationDataValidator.class);

            final var sut = mock(FixedIncomeBondSectorCalculationServiceImpl.class, withSettings()
                    .useConstructor(fixedIncomeBondSectorCacheStorage, requestValidator, assetAllocationCacheStorage, assetAllocationDataMapper, assetAllocationDataValidator));

            final var exposures = mock(Map.class);
            final var expected = new FixedIncomeSectorResDTO(FixedIncomeBondSectorCalculationServiceImpl.DEFAULT_MAP, List.of());

            mockedPortfolioUtils.when(() -> PortfolioUtils.areAllValuesZerosInMap(any())).thenReturn(true);

            doCallRealMethod().when(sut).calculate(any(), any(), any());
            //ACT
            final var actual = sut.calculate(exposures, List.of(), List.of());

            //VERIFY
            assertEquals(expected, actual);
        }
    }

    @Test
    void getLoadFromCacheStorage_checkResult() {
        try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
            //SETUP
            final var requestValidator = mock(PortfolioHoldingsReqDtoValidator.class);
            final var fixedIncomeBondSectorCacheStorage = mock(FixedIncomeBondSectorCacheStorage.class);
            final AssetAllocationCacheStorage assetAllocationCacheStorage = mock(AssetAllocationCacheStorage.class);
            final AssetAllocationDataMapper assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
            final AssetAllocationDataValidator assetAllocationDataValidator = mock(AssetAllocationDataValidator.class);

            final var sut = mock(FixedIncomeBondSectorCalculationServiceImpl.class, withSettings()
                    .useConstructor(fixedIncomeBondSectorCacheStorage, requestValidator, assetAllocationCacheStorage, assetAllocationDataMapper, assetAllocationDataValidator));

            final var holding = mock(Holding.class);
            final var exposures = Map.of(holding, Map.of(FixedIncomeSectorType.CORPORATE_BONDS, TEN));

            when(fixedIncomeBondSectorCacheStorage.load(any(), any(), any(), any())).thenReturn(exposures);
            doCallRealMethod().when(sut).getLoadFromCacheStorage(any(), any());
            //ACT
            final var actual = sut.getLoadFromCacheStorage(mock(PortfolioHoldingsReqDTO.class), List.of());

            //VERIFY
            assertEquals(exposures, actual);
        }
    }

    @Test
    void calculate_verifyResult() {
        try (var mockedPortfolioUtils = Mockito.mockStatic(PortfolioUtils.class)) {
            //SETUP
            final var requestValidator = mock(PortfolioHoldingsReqDtoValidator.class);
            final var fixedIncomeBondSectorCacheStorage = mock(FixedIncomeBondSectorCacheStorage.class);
            final AssetAllocationCacheStorage assetAllocationCacheStorage = mock(AssetAllocationCacheStorage.class);
            final AssetAllocationDataMapper assetAllocationDataMapper = mock(AssetAllocationDataMapper.class);
            final AssetAllocationDataValidator assetAllocationDataValidator = mock(AssetAllocationDataValidator.class);
            final FundSeriesHolding fundSeriesHolding = mock(FundSeriesHolding.class);
            final AssetAllocationDataDTO assetAllocationDataDTO = mock(AssetAllocationDataDTO.class);

            final var sut = mock(FixedIncomeBondSectorCalculationServiceImpl.class, withSettings()
                    .useConstructor(fixedIncomeBondSectorCacheStorage, requestValidator, assetAllocationCacheStorage, assetAllocationDataMapper, assetAllocationDataValidator));

            final var exposures = mock(Map.class);

            Mockito.when(assetAllocationCacheStorage.load(any(), any(), any(), any()))
                    .thenReturn(assetAllocationDataDTO);
            Mockito.when(assetAllocationDataMapper.mapForAA(assetAllocationDataDTO))
                    .thenReturn(Map.of(
                                    fundSeriesHolding,
                                    Map.of(
                                            AssetAllocationRegion.FIXED_INCOME, BigDecimal.ONE,
                                            AssetAllocationRegion.CASH, BigDecimal.ONE
                                    )
                            )
                    );

            doCallRealMethod().when(sut).calculate(any(), any(), any());
            //ACT
            final FixedIncomeSectorResDTO result = sut.calculate(exposures, List.of(fundSeriesHolding), List.of());

            //VERIFY
            mockedPortfolioUtils.verify(() -> PortfolioUtils.areAllValuesZerosInMap(exposures));
        }
    }

}
