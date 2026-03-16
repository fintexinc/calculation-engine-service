package com.fintex.ce.adapter.cache;

import com.fintex.ce.adapter.cache.AverageMERCacheStorage;
import com.fintex.ce.adapter.cache.repository.averagemer.AverageMerRepository;
import com.fintex.ce.port.output.sm.SecurityDataPort;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.AverageMer;
import com.fintex.ce.domain.model.AverageManagementExpenseCalculationDTO;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
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
    // SETUP
    final var queryRepository = mock(SecurityDataPort.class);
    final var averageMerRepository = mock(AverageMerRepository.class);

    final var sut = mock(AverageMERCacheStorage.class, withSettings()
        .useConstructor(queryRepository, null, averageMerRepository));

    final var merDTO = mock(AverageManagementExpenseCalculationDTO.class);
    final var fundSeriesHolding = new FundSeriesHolding();

    when(merDTO.setActualManagementFee(any())).thenReturn(merDTO);
    when(merDTO.setManagementExpenseRatio(any())).thenReturn(merDTO);
    when(sut.preBuildAverageMerDto(any())).thenReturn(merDTO);

    doCallRealMethod().when(sut).mapperForCanadaMutualFund(any(), any());
    // ACT
    final AverageManagementExpenseCalculationDTO actual = sut.mapperForCanadaMutualFund(fundSeriesHolding, mock(
        AverageMer.class));

    // VERIFY
    verify(sut).preBuildAverageMerDto(fundSeriesHolding);
    assertSame(merDTO, actual);
  }

  @Test
  void mapperForUsEtf_verifyPreBuildAverageMerDto() {
    // SETUP
    final var sut = mock(AverageMERCacheStorage.class);

    final var merDTO = mock(AverageManagementExpenseCalculationDTO.class);
    final var etfHolding = new EtfHolding();

    when(merDTO.setNetExpenseRatio(any())).thenReturn(merDTO);
    when(merDTO.setGrossExpenseRatio(any())).thenReturn(merDTO);
    when(sut.preBuildAverageMerDto(any())).thenReturn(merDTO);

    doCallRealMethod().when(sut).mapperForEtfUs(any(), any());
    // ACT
    final AverageManagementExpenseCalculationDTO actual = sut.mapperForEtfUs(etfHolding, mock(AverageMer.class));

    // VERIFY
    verify(sut).preBuildAverageMerDto(etfHolding);
    assertSame(merDTO, actual);
  }

  @Test
  void mapperForCanadaEtf_verifyPreBuildAverageMerDto() {
    // SETUP
    final var sut = mock(AverageMERCacheStorage.class);

    final var merDTO = mock(AverageManagementExpenseCalculationDTO.class);
    final var etfHolding = new EtfHolding();

    when(merDTO.setManagementExpenseRatio(any())).thenReturn(merDTO);
    when(merDTO.setActualManagementFee(any())).thenReturn(merDTO);
    when(sut.preBuildAverageMerDto(any())).thenReturn(merDTO);

    doCallRealMethod().when(sut).mapperForEtfCanada(any(), any());
    // ACT
    final AverageManagementExpenseCalculationDTO actual = sut.mapperForEtfCanada(etfHolding, mock(AverageMer.class));

    // VERIFY
    verify(sut).preBuildAverageMerDto(etfHolding);
    assertSame(merDTO, actual);
  }

  @Test
  void mapperForCanadaEtf_checkResult() {
    // SETUP
    final var sut = mock(AverageMERCacheStorage.class);

    final var merDTO = mock(AverageManagementExpenseCalculationDTO.class);
    final var etfHolding = new EtfHolding();
    final var averageMerEtfCanada = mock(AverageMer.class);

    when(sut.preBuildAverageMerDto(any())).thenReturn(merDTO);
    when(merDTO.setManagementExpenseRatio(any())).thenReturn(merDTO);
    when(merDTO.setActualManagementFee(any())).thenReturn(merDTO);
    when(averageMerEtfCanada.getMer()).thenReturn(BigDecimal.TEN);
    when(averageMerEtfCanada.getActualManagementFee()).thenReturn(BigDecimal.ONE);

    doCallRealMethod().when(sut).mapperForEtfCanada(any(), any());
    // ACT
    sut.mapperForEtfCanada(etfHolding, averageMerEtfCanada);

    // VERIFY
    verify(merDTO).setManagementExpenseRatio(averageMerEtfCanada.getMer());
    verify(merDTO).setActualManagementFee(averageMerEtfCanada.getActualManagementFee());
  }

  @Test
  void mapperForUsEtf_checkResult() {
    // SETUP
    final var sut = mock(AverageMERCacheStorage.class);

    final var merDTO = mock(AverageManagementExpenseCalculationDTO.class);
    final var etfHolding = new EtfHolding();

    when(merDTO.setNetExpenseRatio(any())).thenReturn(merDTO);
    when(merDTO.setGrossExpenseRatio(any())).thenReturn(merDTO);
    when(sut.preBuildAverageMerDto(any())).thenReturn(merDTO);

    final var averageMerEtfUs = mock(AverageMer.class);
    when(averageMerEtfUs.getNetExpenseRatio()).thenReturn(BigDecimal.TEN);
    when(averageMerEtfUs.getGrossExpenseRatio()).thenReturn(BigDecimal.ONE);

    doCallRealMethod().when(sut).mapperForEtfUs(any(), any());
    // ACT
    sut.mapperForEtfUs(etfHolding, averageMerEtfUs);

    // VERIFY
    verify(merDTO).setNetExpenseRatio(averageMerEtfUs.getNetExpenseRatio());
    verify(merDTO).setGrossExpenseRatio(averageMerEtfUs.getGrossExpenseRatio());
  }

  @Test
  void mapperForCanadaMutualFund_checkResult() {
    // SETUP
    final var sut = mock(AverageMERCacheStorage.class);

    final var merDTO = mock(AverageManagementExpenseCalculationDTO.class);
    when(merDTO.setActualManagementFee(any())).thenReturn(merDTO);
    when(merDTO.setManagementExpenseRatio(any())).thenReturn(merDTO);

    when(sut.preBuildAverageMerDto(any())).thenReturn(merDTO);

    final var fundSeriesHolding = new FundSeriesHolding();

    final var averageMerFundCanada = mock(AverageMer.class);
    when(averageMerFundCanada.getActualManagementFee()).thenReturn(BigDecimal.TEN);
    when(averageMerFundCanada.getMer()).thenReturn(BigDecimal.ONE);

    doCallRealMethod().when(sut).mapperForCanadaMutualFund(any(), any());
    // ACT
    sut.mapperForCanadaMutualFund(fundSeriesHolding, averageMerFundCanada);

    // VERIFY
    verify(merDTO).setActualManagementFee(averageMerFundCanada.getActualManagementFee());
    verify(merDTO).setManagementExpenseRatio(averageMerFundCanada.getMer());
  }

  @Test
  void dataProviderCheckerForCanadaMutualFund_verifyDataProviderCheckValidation() {
    try (var mockedDataProviderRequestHandlingValidator = Mockito.mockStatic(
        DataProviderRequestHandlingValidator.class)) {
      // SETUP
      final var sut = mock(AverageMERCacheStorage.class);

      final var providers = List.of(DataProvider.MORNINGSTAR);
      final Collection<AverageMer> responseFromFds = List.of();

      doCallRealMethod().when(sut).dataProviderCheckerForCanadaMutualFund(anyList(), any());
      // ACT
      sut.dataProviderCheckerForCanadaMutualFund(providers, responseFromFds);

      // VERIFY
      mockedDataProviderRequestHandlingValidator.verify(
          () -> DataProviderRequestHandlingValidator.dataProviderCheckValidation(eq(providers), eq(responseFromFds),
              any(), any()),
          Mockito.times(2));
    }
  }

  @Test
  void dataProviderCheckerForEtfCanada_verifyDataProviderCheckValidation() {
    try (var mockedDataProviderRequestHandlingValidator = Mockito.mockStatic(
        DataProviderRequestHandlingValidator.class)) {
      // SETUP
      final var sut = mock(AverageMERCacheStorage.class);

      final var providers = List.of(DataProvider.MORNINGSTAR);
      final Collection<AverageMer> responseFromFds = List.of();

      doCallRealMethod().when(sut).dataProviderCheckerForEtfCanada(anyList(), any());
      // ACT
      sut.dataProviderCheckerForEtfCanada(providers, responseFromFds);

      // VERIFY
      mockedDataProviderRequestHandlingValidator.verify(
          () -> DataProviderRequestHandlingValidator.dataProviderCheckValidation(eq(providers), eq(responseFromFds),
              any(), any()),
          Mockito.times(2));
    }
  }

  @Test
  void dataProviderCheckerForEtfUs_verifyDataProviderCheckValidation() {
    try (var mockedDataProviderRequestHandlingValidator = Mockito.mockStatic(
        DataProviderRequestHandlingValidator.class)) {
      // SETUP
      final var sut = mock(AverageMERCacheStorage.class);

      final var providers = List.of(DataProvider.MORNINGSTAR);
      final Collection<AverageMer> responseFromFds = List.of();

      doCallRealMethod().when(sut).dataProviderCheckerForEtfUs(anyList(), any());
      // ACT
      sut.dataProviderCheckerForEtfUs(providers, responseFromFds);

      // VERIFY
      mockedDataProviderRequestHandlingValidator.verify(
          () -> DataProviderRequestHandlingValidator.dataProviderCheckValidation(eq(providers), eq(responseFromFds),
              any(), any()),
          Mockito.times(2));
    }
  }

}