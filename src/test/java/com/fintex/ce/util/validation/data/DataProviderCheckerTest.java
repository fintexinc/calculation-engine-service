package com.fintex.ce.util.validation.data;

import com.fintex.ce.dto.calculation.AssetAllocationDataDTO;
import com.fintex.ce.model.redis.RAssetAllocation;
import com.fintex.ce.util.validation.DataProviderRequestHandlingValidator;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DataProviderCheckerTest {

    DataProviderCheckerTest() {
    }

    @Test
    void check_verifyMethodCalls() {
        try (var mockedDataProviderRequestHandlingValidator = Mockito.mockStatic(DataProviderRequestHandlingValidator.class)) {
            //SETUP
            final var sut = new DataProviderChecker();
            final var map = mock(Map.class);
            final var list = mock(List.class);
            when(map.values()).thenReturn(list);

            final var assetAllocationData = mock(AssetAllocationDataDTO.class);
            when(assetAllocationData.getBenchmarkIndexFdsResponse()).thenReturn(map);
            when(assetAllocationData.getEtfCanadaFdsResponse()).thenReturn(map);
            when(assetAllocationData.getEtfUsFdsResponse()).thenReturn(map);
            when(assetAllocationData.getMutualFundFdsResponse()).thenReturn(map);

            //ACT
            sut.check(list, assetAllocationData);

            //VERIFY
            verify(map, times(4)).values();

            mockedDataProviderRequestHandlingValidator.verify(Mockito.times(4),
                    () -> DataProviderRequestHandlingValidator.dataProviderCheckValidation(eq(list), eq(list), any()));
        }
    }

    @Test
    void clearAssetAllocation_checkResult() {
        //SETUP
        final var sut = new DataProviderChecker();
        final RAssetAllocation assetAllocation = mock(RAssetAllocation.class);

        //ACT
        sut.clearAssetAllocation().apply(assetAllocation, null);

        //VERIFY
        verify(assetAllocation).setAssetAllocation(argThat(Map::isEmpty));
    }

}