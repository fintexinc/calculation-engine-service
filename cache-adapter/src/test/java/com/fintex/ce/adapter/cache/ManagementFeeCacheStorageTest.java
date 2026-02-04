package com.fintex.ce.adapter.cache;

import com.fintex.ce.adapter.cache.ManagementFeeCacheStorage;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.AverageManagementExpenseCalculationDTO;
import com.fintex.ce.domain.model.ManagementFee;
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
import static org.mockito.Mockito.*;

class ManagementFeeCacheStorageTest {

  @Test
  void mapperForCanadaMutualFund_verifyPreBuildAverageMerDto() {
    // SETUP
    var sut = mock(ManagementFeeCacheStorage.class);

    var holding = mock(FundSeriesHolding.class);
    var merDTO = mock(AverageManagementExpenseCalculationDTO.class);

    when(merDTO.setActualManagementFee(any())).thenReturn(merDTO);
    when(sut.preBuildAverageMerDto(any())).thenReturn(merDTO);

    doCallRealMethod().when(sut).mapperForCanadaMutualFund(any(), any());
    // ACT
    var actual = sut.mapperForCanadaMutualFund(holding, mock(ManagementFee.class));

    // VERIFY
    verify(sut).preBuildAverageMerDto(holding);
    assertSame(merDTO, actual);
  }

  @Test
  void mapperForUsEtf_verifyPreBuildAverageMerDto() {
    // SETUP
    var sut = mock(ManagementFeeCacheStorage.class);

    var holding = mock(EtfHolding.class);
    var merDTO = mock(AverageManagementExpenseCalculationDTO.class);

    when(merDTO.setActualManagementFee(any())).thenReturn(merDTO);
    when(sut.preBuildAverageMerDto(any())).thenReturn(merDTO);

    doCallRealMethod().when(sut).mapperForEtfUs(any(), any());
    // ACT
    var actual = sut.mapperForEtfUs(holding, mock(ManagementFee.class));

    // VERIFY
    verify(sut).preBuildAverageMerDto(holding);
    assertSame(merDTO, actual);
  }

  @Test
  void mapperForCanadaEtf_verifyPreBuildAverageMerDto() {
    // SETUP
    var sut = mock(ManagementFeeCacheStorage.class);

    var holding = mock(EtfHolding.class);
    var merDTO = mock(AverageManagementExpenseCalculationDTO.class);

    when(merDTO.setActualManagementFee(any())).thenReturn(merDTO);
    when(sut.preBuildAverageMerDto(any())).thenReturn(merDTO);

    doCallRealMethod().when(sut).mapperForEtfCanada(any(), any());
    // ACT
    var actual = sut.mapperForEtfCanada(holding, mock(ManagementFee.class));

    // VERIFY
    verify(sut).preBuildAverageMerDto(holding);
    assertSame(merDTO, actual);
  }

  @Test
  void mapperForCanadaEtf_checkResult() {
    // SETUP
    var sut = mock(ManagementFeeCacheStorage.class);

    var holding = mock(EtfHolding.class);
    var merRes = mock(ManagementFee.class);
    var merDTO = mock(AverageManagementExpenseCalculationDTO.class);

    when(merDTO.setActualManagementFee(any())).thenReturn(merDTO);
    when(sut.preBuildAverageMerDto(any())).thenReturn(merDTO);
    when(merRes.getManagementFee()).thenReturn(BigDecimal.TEN);

    doCallRealMethod().when(sut).mapperForEtfCanada(any(), any());
    // ACT
    sut.mapperForEtfCanada(holding, merRes);

    // VERIFY
    verify(merDTO).setActualManagementFee(merRes.getManagementFee());
  }

  @Test
  void mapperForUsEtf_checkResult() {
    // SETUP
    var sut = mock(ManagementFeeCacheStorage.class);

    var holding = mock(EtfHolding.class);
    var merRes = mock(ManagementFee.class);
    var merDTO = mock(AverageManagementExpenseCalculationDTO.class);

    when(merDTO.setActualManagementFee(any())).thenReturn(merDTO);
    when(sut.preBuildAverageMerDto(any())).thenReturn(merDTO);
    when(merRes.getManagementFee()).thenReturn(BigDecimal.TEN);

    doCallRealMethod().when(sut).mapperForEtfUs(any(), any());
    // ACT
    sut.mapperForEtfUs(holding, merRes);

    // VERIFY
    verify(merDTO).setActualManagementFee(merRes.getManagementFee());
  }

  @Test
  void mapperForCanadaMutualFund_checkResult() {
    // SETUP
    var sut = mock(ManagementFeeCacheStorage.class);

    var holding = mock(FundSeriesHolding.class);
    var merRes = mock(ManagementFee.class);
    var merDTO = mock(AverageManagementExpenseCalculationDTO.class);

    when(merDTO.setActualManagementFee(any())).thenReturn(merDTO);
    when(sut.preBuildAverageMerDto(any())).thenReturn(merDTO);
    when(merRes.getManagementFee()).thenReturn(BigDecimal.TEN);

    doCallRealMethod().when(sut).mapperForCanadaMutualFund(any(), any());
    // ACT
    sut.mapperForCanadaMutualFund(holding, merRes);

    // VERIFY
    verify(merDTO).setActualManagementFee(merRes.getManagementFee());
  }

  @Test
  void dataProviderCheckerForCanadaMutualFund_verifyDataProviderCheckValidation() {
    try (var mockedDataProviderRequestHandlingValidator = Mockito.mockStatic(
        DataProviderRequestHandlingValidator.class)) {
      // SETUP
      var sut = mock(ManagementFeeCacheStorage.class);

      var providers = List.of(DataProvider.MORNINGSTAR);
      Collection<ManagementFee> responseFromFds = List.of();

      doCallRealMethod().when(sut).dataProviderCheckerForCanadaMutualFund(anyList(), any());
      // ACT
      sut.dataProviderCheckerForCanadaMutualFund(providers, responseFromFds);

      // VERIFY
      mockedDataProviderRequestHandlingValidator.verify(
          () -> DataProviderRequestHandlingValidator.dataProviderCheckValidation(eq(providers), eq(responseFromFds),
              any()));
    }
  }

  @Test
  void dataProviderCheckerForEtfCanada_verifyDataProviderCheckValidation() {
    try (var mockedDataProviderRequestHandlingValidator = Mockito.mockStatic(
        DataProviderRequestHandlingValidator.class)) {
      // SETUP
      var sut = mock(ManagementFeeCacheStorage.class);

      var providers = List.of(DataProvider.MORNINGSTAR);
      Collection<ManagementFee> responseFromFds = List.of();

      doCallRealMethod().when(sut).dataProviderCheckerForEtfCanada(anyList(), any());
      // ACT
      sut.dataProviderCheckerForEtfCanada(providers, responseFromFds);

      // VERIFY
      mockedDataProviderRequestHandlingValidator.verify(
          () -> DataProviderRequestHandlingValidator.dataProviderCheckValidation(eq(providers), eq(responseFromFds),
              any()));
    }
  }

  @Test
  void dataProviderCheckerForEtfUs_verifyDataProviderCheckValidation() {
    try (var mockedDataProviderRequestHandlingValidator = Mockito.mockStatic(
        DataProviderRequestHandlingValidator.class)) {
      // SETUP
      var sut = mock(ManagementFeeCacheStorage.class);

      var providers = List.of(DataProvider.MORNINGSTAR);
      Collection<ManagementFee> responseFromFds = List.of();

      doCallRealMethod().when(sut).dataProviderCheckerForEtfUs(anyList(), any());
      // ACT
      sut.dataProviderCheckerForEtfUs(providers, responseFromFds);

      // VERIFY
      mockedDataProviderRequestHandlingValidator.verify(
          () -> DataProviderRequestHandlingValidator.dataProviderCheckValidation(eq(providers), eq(responseFromFds),
              any()));
    }
  }
}