package com.fintex.ce.service.impl.cache;

import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.AverageManagementExpenseCalculationDTO;
import com.fintex.ce.dto.holding.EtfHolding;
import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.model.redis.averagemer.RAverageMer;
import com.fintex.ce.repository.graphql.query.AverageMERSMRepository;
import com.fintex.ce.repository.redis.averagemer.AverageMerRepository;
import com.fintex.ce.service.interfaces.cache.statistic.CacheStatisticService;
import com.fintex.ce.util.validation.DataProviderRequestHandlingValidator;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class AverageMERCacheStorageTest {

    @Test
    void mapperForCanadaMutualFund_verifyPreBuildAverageMerDto() {
        //SETUP
        final var queryRepository = mock(AverageMERSMRepository.class);
        final var fundCanadaRepo = mock(AverageMerRepository.class);
        final var etfCanadaRepo = mock(AverageMerRepository.class);
        final var etfUsRepo = mock(AverageMerRepository.class);
        final var cacheStatisticService = mock(CacheStatisticService.class);

        final var sut = mock(AverageMERCacheStorage.class, withSettings()
                .useConstructor(queryRepository, fundCanadaRepo, etfCanadaRepo, etfUsRepo, cacheStatisticService));

        final var merDTO = mock(AverageManagementExpenseCalculationDTO.class);
        final var fundSeriesHolding = mock(FundSeriesHolding.class);

        when(merDTO.setActualManagementFee(any())).thenReturn(merDTO);
        when(merDTO.setManagementExpenseRatio(any())).thenReturn(merDTO);
        when(sut.preBuildAverageMerDto(any())).thenReturn(merDTO);


        doCallRealMethod().when(sut).mapperForCanadaMutualFund(any(), any());
        //ACT
        final AverageManagementExpenseCalculationDTO actual = sut.mapperForCanadaMutualFund(fundSeriesHolding, mock(RAverageMer.class));

        //VERIFY
        verify(sut).preBuildAverageMerDto(fundSeriesHolding);
        assertSame(merDTO, actual);
    }

    @Test
    void mapperForUsEtf_verifyPreBuildAverageMerDto() {
        //SETUP
        final var sut = mock(AverageMERCacheStorage.class);

        final var merDTO = mock(AverageManagementExpenseCalculationDTO.class);
        final var etfHolding = mock(EtfHolding.class);

        when(merDTO.setNetExpenseRatio(any())).thenReturn(merDTO);
        when(merDTO.setGrossExpenseRatio(any())).thenReturn(merDTO);
        when(sut.preBuildAverageMerDto(any())).thenReturn(merDTO);

        doCallRealMethod().when(sut).mapperForEtfUs(any(), any());
        //ACT
        final AverageManagementExpenseCalculationDTO actual = sut.mapperForEtfUs(etfHolding, mock(RAverageMer.class));

        //VERIFY
        verify(sut).preBuildAverageMerDto(etfHolding);
        assertSame(merDTO, actual);
    }

    @Test
    void mapperForCanadaEtf_verifyPreBuildAverageMerDto() {
        //SETUP
        final var sut = mock(AverageMERCacheStorage.class);

        final var merDTO = mock(AverageManagementExpenseCalculationDTO.class);
        final var etfHolding = mock(EtfHolding.class);

        when(merDTO.setManagementExpenseRatio(any())).thenReturn(merDTO);
        when(merDTO.setActualManagementFee(any())).thenReturn(merDTO);
        when(sut.preBuildAverageMerDto(any())).thenReturn(merDTO);

        doCallRealMethod().when(sut).mapperForEtfCanada(any(), any());
        //ACT
        final AverageManagementExpenseCalculationDTO actual = sut.mapperForEtfCanada(etfHolding, mock(RAverageMer.class));

        //VERIFY
        verify(sut).preBuildAverageMerDto(etfHolding);
        assertSame(merDTO, actual);
    }

    @Test
    void mapperForCanadaEtf_checkResult() {
        //SETUP
        final var sut = mock(AverageMERCacheStorage.class);

        final var merDTO = mock(AverageManagementExpenseCalculationDTO.class);
        final var etfHolding = mock(EtfHolding.class);
        final var averageMerEtfCanada = mock(RAverageMer.class);

        when(sut.preBuildAverageMerDto(any())).thenReturn(merDTO);
        when(merDTO.setManagementExpenseRatio(any())).thenReturn(merDTO);
        when(merDTO.setActualManagementFee(any())).thenReturn(merDTO);
        when(averageMerEtfCanada.getMer()).thenReturn(BigDecimal.TEN);
        when(averageMerEtfCanada.getActualManagementFee()).thenReturn(BigDecimal.ONE);

        doCallRealMethod().when(sut).mapperForEtfCanada(any(), any());
        //ACT
        sut.mapperForEtfCanada(etfHolding, averageMerEtfCanada);

        //VERIFY
        verify(merDTO).setManagementExpenseRatio(averageMerEtfCanada.getMer());
        verify(merDTO).setActualManagementFee(averageMerEtfCanada.getActualManagementFee());
    }

    @Test
    void mapperForUsEtf_checkResult() {
        //SETUP
        final var sut = mock(AverageMERCacheStorage.class);

        final var merDTO = mock(AverageManagementExpenseCalculationDTO.class);
        final var etfHolding = mock(EtfHolding.class);

        when(merDTO.setNetExpenseRatio(any())).thenReturn(merDTO);
        when(merDTO.setGrossExpenseRatio(any())).thenReturn(merDTO);
        when(sut.preBuildAverageMerDto(any())).thenReturn(merDTO);

        final var averageMerEtfUs = mock(RAverageMer.class);
        when(averageMerEtfUs.getNetExpenseRatio()).thenReturn(BigDecimal.TEN);
        when(averageMerEtfUs.getGrossExpenseRatio()).thenReturn(BigDecimal.ONE);

        doCallRealMethod().when(sut).mapperForEtfUs(any(), any());
        //ACT
        sut.mapperForEtfUs(etfHolding, averageMerEtfUs);

        //VERIFY
        verify(merDTO).setNetExpenseRatio(averageMerEtfUs.getNetExpenseRatio());
        verify(merDTO).setGrossExpenseRatio(averageMerEtfUs.getGrossExpenseRatio());
    }

    @Test
    void mapperForCanadaMutualFund_checkResult() {
        //SETUP
        final var sut = mock(AverageMERCacheStorage.class);

        final var merDTO = mock(AverageManagementExpenseCalculationDTO.class);
        when(merDTO.setActualManagementFee(any())).thenReturn(merDTO);
        when(merDTO.setManagementExpenseRatio(any())).thenReturn(merDTO);

        when(sut.preBuildAverageMerDto(any())).thenReturn(merDTO);

        final var fundSeriesHolding = mock(FundSeriesHolding.class);

        final var averageMerFundCanada = mock(RAverageMer.class);
        when(averageMerFundCanada.getActualManagementFee()).thenReturn(BigDecimal.TEN);
        when(averageMerFundCanada.getMer()).thenReturn(BigDecimal.ONE);

        doCallRealMethod().when(sut).mapperForCanadaMutualFund(any(), any());
        //ACT
        sut.mapperForCanadaMutualFund(fundSeriesHolding, averageMerFundCanada);

        //VERIFY
        verify(merDTO).setActualManagementFee(averageMerFundCanada.getActualManagementFee());
        verify(merDTO).setManagementExpenseRatio(averageMerFundCanada.getMer());
    }

    @Test
    void dataProviderCheckerForCanadaMutualFund_verifyDataProviderCheckValidation() {
        try (var mockedDataProviderRequestHandlingValidator = Mockito.mockStatic(DataProviderRequestHandlingValidator.class)) {
            //SETUP
            final var sut = mock(AverageMERCacheStorage.class);

            final var providers = List.of(DataProvider.MORNINGSTAR);
            final Collection<RAverageMer> responseFromFds = List.of();

            doCallRealMethod().when(sut).dataProviderCheckerForCanadaMutualFund(anyList(), any());
            //ACT
            sut.dataProviderCheckerForCanadaMutualFund(providers, responseFromFds);

            //VERIFY
            mockedDataProviderRequestHandlingValidator.verify(Mockito.times(2),
                    () -> DataProviderRequestHandlingValidator.dataProviderCheckValidation(eq(providers), eq(responseFromFds), any(), any()));
        }
    }

    @Test
    void dataProviderCheckerForEtfCanada_verifyDataProviderCheckValidation() {
        try (var mockedDataProviderRequestHandlingValidator = Mockito.mockStatic(DataProviderRequestHandlingValidator.class)) {
            //SETUP
            final var sut = mock(AverageMERCacheStorage.class);

            final var providers = List.of(DataProvider.MORNINGSTAR);
            final Collection<RAverageMer> responseFromFds = List.of();

            doCallRealMethod().when(sut).dataProviderCheckerForEtfCanada(anyList(), any());
            //ACT
            sut.dataProviderCheckerForEtfCanada(providers, responseFromFds);

            //VERIFY
            mockedDataProviderRequestHandlingValidator.verify(Mockito.times(2),
                    () -> DataProviderRequestHandlingValidator.dataProviderCheckValidation(eq(providers), eq(responseFromFds), any(), any()));
        }
    }

    @Test
    void dataProviderCheckerForEtfUs_verifyDataProviderCheckValidation() {
        try (var mockedDataProviderRequestHandlingValidator = Mockito.mockStatic(DataProviderRequestHandlingValidator.class)) {
            //SETUP
            final var sut = mock(AverageMERCacheStorage.class);

            final var providers = List.of(DataProvider.MORNINGSTAR);
            final Collection<RAverageMer> responseFromFds = List.of();

            doCallRealMethod().when(sut).dataProviderCheckerForEtfUs(anyList(), any());
            //ACT
            sut.dataProviderCheckerForEtfUs(providers, responseFromFds);

            //VERIFY
            mockedDataProviderRequestHandlingValidator.verify(Mockito.times(2),
                    () -> DataProviderRequestHandlingValidator.dataProviderCheckValidation(eq(providers), eq(responseFromFds), any(), any()));
        }
    }

}