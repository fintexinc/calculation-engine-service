package com.fintex.ce.service.impl.calculation;

import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.config.enumeration.HoldingType;
import com.fintex.ce.dto.AverageManagementExpenseCalculationDTO;
import com.fintex.ce.dto.ParamHolderDTO;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.request.AverageMerRequestDTO;
import com.fintex.ce.dto.response.ManagementFeeResponse;
import com.fintex.ce.exception.FdsDataValidationException;
import com.fintex.ce.exception.notification.pattern.Notification;
import com.fintex.ce.service.impl.cache.ManagementFeeCacheStorage;
import com.fintex.ce.util.FilterUtils;
import com.fintex.ce.util.validation.request.AverageMerRequestValidator;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.config.constant.BigDecimalConstants.ONE;
import static com.fintex.ce.config.enumeration.ExceptionCode.ERR_MF_MF_001;
import static com.fintex.ce.config.enumeration.HoldingType.*;
import static com.fintex.ce.config.enumeration.ParameterType.ABSOLUTE;
import static com.fintex.ce.config.enumeration.ParameterType.SCALED;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
class ManagementFeeCalculationServiceImplTest {

    @Test
    void perform_checkResult() {
        //SETUP
        final var managementFeeCacheStorage = mock(ManagementFeeCacheStorage.class);
        final var requestValidator = mock(AverageMerRequestValidator.class);
        final var sut = mock(ManagementFeeCalculationServiceImpl.class, withSettings()
                .useConstructor(managementFeeCacheStorage, requestValidator));

        final var resDto = mock(ManagementFeeResponse.class);

        when(sut.calculateAverageValue(any(), any())).thenReturn(resDto);

        doCallRealMethod().when(sut).perform(any());
        //ACT
        final var actual = sut.perform(mock(AverageMerRequestDTO.class));

        //VERIFY
        assertSame(resDto, actual);
    }

    @Test
    void perform_verifyLoad() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            //SETUP
            final var managementFeeCacheStorage = mock(ManagementFeeCacheStorage.class);
            final var requestValidator = mock(AverageMerRequestValidator.class);
            final var sut = mock(ManagementFeeCalculationServiceImpl.class, withSettings()
                    .useConstructor(managementFeeCacheStorage, requestValidator));

            final var reqDTO = mock(AverageMerRequestDTO.class);
            final var holdings = mock(List.class);
            final var resDto = mock(ManagementFeeResponse.class);
            final var defaultProviders = mock(List.class);

            mockedFilterUtils.when(() -> FilterUtils.getSpecifiedIfEmpty(anyList(), any())).thenReturn(defaultProviders);
            when(reqDTO.getHoldings()).thenReturn(holdings);
            when(sut.calculateAverageValue(any(), any())).thenReturn(resDto);

            doCallRealMethod().when(sut).perform(any());
            doCallRealMethod().when(sut).loadDataFromCacheStorage(any());
            //ACT
            sut.perform(reqDTO);

            //VERIFY
            verify(managementFeeCacheStorage).load(holdings, defaultProviders, List.of(), new ParamHolderDTO());
        }
    }

    @Test
    void perform_verifySetNullForScaledIfHoldingContainsNoFunds() {
        //SETUP
        final var managementFeeCacheStorage = mock(ManagementFeeCacheStorage.class);
        final var requestValidator = mock(AverageMerRequestValidator.class);
        final var sut = mock(ManagementFeeCalculationServiceImpl.class, withSettings()
                .useConstructor(managementFeeCacheStorage, requestValidator));

        final var resDto = mock(ManagementFeeResponse.class);
        final var reqDTO = mock(AverageMerRequestDTO.class);
        final var managementFee = mock(Map.class);

        when(resDto.getManagementFee()).thenReturn(managementFee);
        when(sut.calculateAverageValue(any(), any())).thenReturn(resDto);

        doCallRealMethod().when(sut).perform(any());
        doCallRealMethod().when(sut).setNullForScaledAndForcedReportFeeIfHoldingContainsNoFunds(any(ManagementFeeResponse.class), any());
        //ACT
        sut.perform(reqDTO);

        //VERIFY
        verify(sut).setNullForScaledIfHoldingContainsNoFunds(managementFee, reqDTO);

    }

    @Test
    void perform_verifyValidateHoldings() {
        //SETUP
        final var managementFeeCacheStorage = mock(ManagementFeeCacheStorage.class);
        final var requestValidator = mock(AverageMerRequestValidator.class);
        final var sut = mock(ManagementFeeCalculationServiceImpl.class, withSettings()
                .useConstructor(managementFeeCacheStorage, requestValidator));

        final var resDto = mock(ManagementFeeResponse.class);
        final var reqDto = mock(AverageMerRequestDTO.class);
        final List<Holding> holdings = List.of(mock(Holding.class));

        when(sut.calculateAverageValue(any(), any())).thenReturn(resDto);
        when(reqDto.getHoldings()).thenReturn(holdings);

        doCallRealMethod().when(sut).perform(any());
        doCallRealMethod().when(sut).validateRequest(any());
        //ACT
        sut.perform(reqDto);

        //VERIFY
        verify(requestValidator).validate(reqDto);
    }

    @Test
    void perform_verifyCalculateAverageValue() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            //SETUP
            final var managementFeeCacheStorage = mock(ManagementFeeCacheStorage.class);
            final var requestValidator = mock(AverageMerRequestValidator.class);
            final var sut = mock(ManagementFeeCalculationServiceImpl.class, withSettings()
                    .useConstructor(managementFeeCacheStorage, requestValidator));

            final HashMap<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> map = new HashMap<>();
            final var reqDTO = mock(AverageMerRequestDTO.class);
            final var parameterTypes = mock(List.class);

            when(managementFeeCacheStorage.load(any(), anyList(), anyList(), any())).thenReturn(map);
            mockedFilterUtils.when(() -> FilterUtils.getSpecifiedIfEmpty(anyList(), any())).thenReturn(parameterTypes);
            when(sut.calculateAverageValue(any(), any())).thenReturn(mock(ManagementFeeResponse.class));

            doCallRealMethod().when(sut).perform(any());
            doCallRealMethod().when(sut).calculateAverageValue(any(), any());
            //ACT
            sut.perform(reqDTO);

            //VERIFY
            verify(sut).calculateAverageValue(parameterTypes, map);
        }
    }

    @Test
    void perform_verifyGetSpecifiedIfEmpty() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            //SETUP
            final var managementFeeCacheStorage = mock(ManagementFeeCacheStorage.class);
            final var requestValidator = mock(AverageMerRequestValidator.class);
            final var sut = mock(ManagementFeeCalculationServiceImpl.class, withSettings()
                    .useConstructor(managementFeeCacheStorage, requestValidator));

            final HashMap<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> map = new HashMap<>();
            final var reqDTO = mock(AverageMerRequestDTO.class);
            final var parameterTypes = mock(List.class);

            when(reqDTO.getParameterTypes()).thenReturn(parameterTypes);
            when(managementFeeCacheStorage.load(any(), anyList(), anyList(), any())).thenReturn(map);
            when(sut.calculateAverageValue(any(), any())).thenReturn(mock(ManagementFeeResponse.class));

            doCallRealMethod().when(sut).perform(any());
            //ACT
            sut.perform(reqDTO);

            //VERIFY
            mockedFilterUtils.verify(() -> FilterUtils.getSpecifiedIfEmpty(parameterTypes, SCALED, ABSOLUTE));
        }
    }

    @Test
    void perform_verifyGetSpecifiedIfEmptyDEFAULT_DATAPROVIDERS() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            //SETUP
            final var managementFeeCacheStorage = mock(ManagementFeeCacheStorage.class);
            final var requestValidator = mock(AverageMerRequestValidator.class);
            final var sut = mock(ManagementFeeCalculationServiceImpl.class, withSettings()
                    .useConstructor(managementFeeCacheStorage, requestValidator));

            final HashMap<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> map = new HashMap<>();
            final var reqDTO = mock(AverageMerRequestDTO.class);
            final var providers = mock(List.class);

            when(reqDTO.getDataProviders()).thenReturn(providers);
            when(managementFeeCacheStorage.load(any(), anyList(), anyList(), any())).thenReturn(map);
            when(sut.calculateAverageValue(any(), any())).thenReturn(mock(ManagementFeeResponse.class));

            doCallRealMethod().when(sut).perform(any());
            doCallRealMethod().when(sut).loadDataFromCacheStorage(any());
            //ACT
            sut.perform(reqDTO);

            //VERIFY
            mockedFilterUtils.verify(() -> FilterUtils.getSpecifiedIfEmpty(providers, DataProvider.DEFAULT_PROVIDERS));
        }
    }

    @Test
    void setInitialFeeAndModifiedFeeValues_checkResult() {
        //SETUP
        final var sut = mock(ManagementFeeCalculationServiceImpl.class);

        final var calculationDtoMap = getCalculationDtoMap();
        calculationDtoMap.get(CANADA_MUTUAL_FUNDS).forEach((key, value) -> value.setActualManagementFee(TEN));
        calculationDtoMap.get(US_ETF).forEach((key, value) -> value.setActualManagementFee(ONE));
        calculationDtoMap.get(CANADA_ETF).forEach((key, value) -> value.setActualManagementFee(ZERO));

        doCallRealMethod().when(sut).setFeeValues(any(), any());
        doCallRealMethod().when(sut).setInitialFeeAndModifiedFeeValues(any());
        doCallRealMethod().when(sut).validateManagementFee(any(), any(), any());
        //ACT
        sut.setInitialFeeAndModifiedFeeValues(calculationDtoMap);

        //VERIFY
        calculationDtoMap.get(CANADA_MUTUAL_FUNDS).forEach((key, value) -> {
            assertEquals(TEN, value.getInitialFee());
            assertEquals(TEN, value.getModifiedFee());
        });
        calculationDtoMap.get(US_ETF).forEach((key, value) -> {
            assertEquals(ONE, value.getInitialFee());
            assertEquals(ONE, value.getModifiedFee());
        });
        calculationDtoMap.get(CANADA_ETF).forEach((key, value) -> {
            assertEquals(ZERO, value.getInitialFee());
            assertEquals(ZERO, value.getModifiedFee());
        });
    }

    @Test
    void calculateAverageValue_checkResult() {
        //SETUP
        final var sut = mock(ManagementFeeCalculationServiceImpl.class);

        final var parameterTypes = List.of(ABSOLUTE, SCALED);
        final var averageMerCalculationDtoMap = getCalculationDtoMap();
        final var expected = new ManagementFeeResponse();
        expected.setManagementFee(Map.of(SCALED, TEN, ABSOLUTE, ONE));

        when(sut.getAbsoluteAverageMer(averageMerCalculationDtoMap)).thenReturn(ONE);
        when(sut.getScaledAverageMer(averageMerCalculationDtoMap)).thenReturn(TEN);

        doCallRealMethod().when(sut).calculateAverageValue(any(), any());
        //ACT
        final var actual = sut.calculateAverageValue(parameterTypes, averageMerCalculationDtoMap);

        //VERIFY
        assertEquals(expected, actual);
    }

    @Test
    void calculateAverageValue_verifyGetAbsoluteAverageMer() {
        //SETUP
        final var sut = mock(ManagementFeeCalculationServiceImpl.class);

        final var parameterTypes = List.of(ABSOLUTE);
        final var averageMerCalculationDtoMap = getCalculationDtoMap();

        when(sut.getAbsoluteAverageMer(averageMerCalculationDtoMap)).thenReturn(ONE);

        doCallRealMethod().when(sut).calculateAverageValue(any(), any());
        //ACT
        sut.calculateAverageValue(parameterTypes, averageMerCalculationDtoMap);

        //VERIFY
        verify(sut).getAbsoluteAverageMer(averageMerCalculationDtoMap);
    }

    @Test
    void calculateAverageValue_verifyGetScaledAverageMer() {
        //SETUP
        final var sut = mock(ManagementFeeCalculationServiceImpl.class);

        final var parameterTypes = List.of(SCALED);
        final var averageMerCalculationDtoMap = getCalculationDtoMap();

        when(sut.getScaledAverageMer(averageMerCalculationDtoMap)).thenReturn(TEN);

        doCallRealMethod().when(sut).calculateAverageValue(any(), any());
        //ACT
        sut.calculateAverageValue(parameterTypes, averageMerCalculationDtoMap);

        //VERIFY
        verify(sut).getScaledAverageMer(averageMerCalculationDtoMap);
    }

    private Map<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> getCalculationDtoMap() {
        final AverageManagementExpenseCalculationDTO averageManagementExpenseCalculationDTO1 = new AverageManagementExpenseCalculationDTO();
        averageManagementExpenseCalculationDTO1.setMarketValue(new BigDecimal("10"));
        final AverageManagementExpenseCalculationDTO averageManagementExpenseCalculationDTO2 = new AverageManagementExpenseCalculationDTO();
        averageManagementExpenseCalculationDTO2.setMarketValue(new BigDecimal("20"));
        final AverageManagementExpenseCalculationDTO averageManagementExpenseCalculationDTO3 = new AverageManagementExpenseCalculationDTO();
        averageManagementExpenseCalculationDTO3.setMarketValue(new BigDecimal("30"));
        final AverageManagementExpenseCalculationDTO averageManagementExpenseCalculationDTO4 = new AverageManagementExpenseCalculationDTO();
        averageManagementExpenseCalculationDTO4.setMarketValue(new BigDecimal("40"));
        final AverageManagementExpenseCalculationDTO averageManagementExpenseCalculationDTO5 = new AverageManagementExpenseCalculationDTO();
        averageManagementExpenseCalculationDTO5.setMarketValue(new BigDecimal("50"));
        final AverageManagementExpenseCalculationDTO averageManagementExpenseCalculationDTO6 = new AverageManagementExpenseCalculationDTO();
        averageManagementExpenseCalculationDTO6.setMarketValue(new BigDecimal("60"));

        return Map.of(CANADA_MUTUAL_FUNDS, Map.of(mock(Holding.class), averageManagementExpenseCalculationDTO1),
                US_ETF, Map.of(mock(Holding.class), averageManagementExpenseCalculationDTO2),
                CANADA_ETF, Map.of(mock(Holding.class), averageManagementExpenseCalculationDTO3),
                HoldingType.CANADA_STOCKS, Map.of(mock(Holding.class), averageManagementExpenseCalculationDTO4),
                HoldingType.US_STOCKS, Map.of(mock(Holding.class), averageManagementExpenseCalculationDTO5),
                HoldingType.CASH, Map.of(mock(Holding.class), averageManagementExpenseCalculationDTO6));
    }

    @Test
    void setInitialFeeAndModifiedFeeValues_expectExceptionWhenHoldingIsFundAndManagementFeeIsEmpty() {
        //SETUP
        var sut = mock(ManagementFeeCalculationServiceImpl.class);
        var holding = new Holding();
        var averageCalculationDto = new AverageManagementExpenseCalculationDTO().setActualManagementFee(null);
        var expected = ERR_MF_MF_001.error(holding, HttpStatus.BAD_REQUEST);

        Map<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> map = new HashMap<>();
        map.put(HoldingType.CANADA_MUTUAL_FUNDS, Map.of(holding, averageCalculationDto));

        doCallRealMethod().when(sut).validateManagementFee(any(), any(), any());
        doCallRealMethod().when(sut).setInitialFeeAndModifiedFeeValues(any());
        //ACT
        var actualException = assertThrows(FdsDataValidationException.class, () -> sut.setInitialFeeAndModifiedFeeValues(map));

        //VERIFY
        assertTrue(actualException.getExceptionList().stream().anyMatch(e -> e.getCode().equals(expected.getCode())));
    }

    @Test
    void setInitialFeeAndModifiedFeeValues_nothingHappensIfHoldingsIsOnlyCashAndStocks() {
        //SETUP
        var sut = mock(ManagementFeeCalculationServiceImpl.class);
        var notification = new Notification();
        var holding = new Holding();
        var averageCalculationDto = new AverageManagementExpenseCalculationDTO().setActualManagementFee(null);

        Map<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> map = new HashMap<>();
        map.put(HoldingType.US_STOCKS, Map.of(holding, averageCalculationDto));

        doCallRealMethod().when(sut).validateManagementFee(any(), any(), any());
        doCallRealMethod().when(sut).setInitialFeeAndModifiedFeeValues(any());
        //ACT
        sut.setInitialFeeAndModifiedFeeValues(map);

        //VERIFY
        assertFalse(notification.hasErrors());
    }

    @Test
    void setInitialFeeAndModifiedFeeValues_nothingHappensIfHoldingsIsFundAndContainsManagementFee() {
        //SETUP
        var sut = mock(ManagementFeeCalculationServiceImpl.class);
        var notification = new Notification();
        var holding = new Holding();
        var averageCalculationDto = new AverageManagementExpenseCalculationDTO().setActualManagementFee(TEN);

        Map<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> map = new HashMap<>();
        map.put(HoldingType.US_ETF, Map.of(holding, averageCalculationDto));

        doCallRealMethod().when(sut).validateManagementFee(any(), any(), any());
        doCallRealMethod().when(sut).setInitialFeeAndModifiedFeeValues(any());
        //ACT
        sut.setInitialFeeAndModifiedFeeValues(map);

        //VERIFY
        assertFalse(notification.hasErrors());
    }

}