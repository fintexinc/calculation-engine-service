package com.fintex.ce.service.impl.calculation;

import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.config.enumeration.HoldingType;
import com.fintex.ce.dto.AverageManagementExpenseCalculationDTO;
import com.fintex.ce.dto.ParamHolderDTO;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.request.AverageMerRequestDTO;
import com.fintex.ce.dto.response.AverageMerResponse;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.exception.notification.pattern.Notification;
import com.fintex.ce.service.impl.cache.AverageMERCacheStorage;
import com.fintex.ce.util.ComparisonUtils;
import com.fintex.ce.util.FilterUtils;
import com.fintex.ce.util.validation.request.AverageMerRequestValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.fintex.ce.config.enumeration.ExceptionCode.WRN_MER_AMF_001;
import static com.fintex.ce.config.enumeration.ExceptionCode.WRN_MER_MER_001;
import static com.fintex.ce.config.enumeration.ParameterType.*;
import static java.math.BigDecimal.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
class MERCalculationServiceImplTest {

    @Test
    void perform_checkResult() {
        //SETUP
        final var requestValidator = mock(AverageMerRequestValidator.class);
        final var averageMERCacheStorage = mock(AverageMERCacheStorage.class);
        final var sut = mock(MERCalculationServiceImpl.class, withSettings().
                useConstructor(averageMERCacheStorage, requestValidator));

        final var resDto = mock(AverageMerResponse.class);

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
            final var requestValidator = mock(AverageMerRequestValidator.class);
            final var averageMERCacheStorage = mock(AverageMERCacheStorage.class);
            final var sut = mock(MERCalculationServiceImpl.class, withSettings().
                    useConstructor(averageMERCacheStorage, requestValidator));


            final var reqDTO = mock(AverageMerRequestDTO.class);
            final var holdings = mock(List.class);
            final var resDto = mock(AverageMerResponse.class);
            final var defaultProviders = mock(List.class);

            mockedFilterUtils.when(() -> FilterUtils.getSpecifiedIfEmpty(anyList(), any())).thenReturn(defaultProviders);
            when(reqDTO.getHoldings()).thenReturn(holdings);
            when(sut.calculateAverageValue(any(), any())).thenReturn(resDto);

            doCallRealMethod().when(sut).perform(any());
            doCallRealMethod().when(sut).loadDataFromCacheStorage(any());
            //ACT
            sut.perform(reqDTO);

            //VERIFY
            verify(averageMERCacheStorage).load(holdings, defaultProviders, List.of(), new ParamHolderDTO());
        }
    }

    @Test
    void perform_verifyResDTOSetWarnings() {
        //SETUP
        final var requestValidator = mock(AverageMerRequestValidator.class);
        final var averageMERCacheStorage = mock(AverageMERCacheStorage.class);
        final var sut = mock(MERCalculationServiceImpl.class, withSettings().
                useConstructor(averageMERCacheStorage, requestValidator));


        final var reqDTO = mock(AverageMerRequestDTO.class);
        final var resDTO = mock(AverageMerResponse.class);
        final var warnings = mock(List.class);

        when(sut.calculateAverageValue(any(), any())).thenReturn(resDTO);
        when(sut.setInitialFeeAndModifiedFeeValues(any())).thenReturn(warnings);

        doCallRealMethod().when(sut).perform(any());
        //ACT
        sut.perform(reqDTO);

        //VERIFY
        verify(resDTO).setWarnings(warnings);

    }

    @Test
    void perform_verifySetInitialFeeAndModifiedFeeValues() {
        //SETUP
        final var requestValidator = mock(AverageMerRequestValidator.class);
        final var averageMERCacheStorage = mock(AverageMERCacheStorage.class);
        final var sut = mock(MERCalculationServiceImpl.class, withSettings().
                useConstructor(averageMERCacheStorage, requestValidator));


        final var reqDTO = mock(AverageMerRequestDTO.class);
        final var resDTO = mock(AverageMerResponse.class);
        final var averageMerCalculationDtos = mock(Map.class);

        when(averageMERCacheStorage.load(any(), any(), any(), any())).thenReturn(averageMerCalculationDtos);
        when(sut.calculateAverageValue(any(), any())).thenReturn(resDTO);

        doCallRealMethod().when(sut).perform(any());
        doCallRealMethod().when(sut).loadDataFromCacheStorage(any());
        //ACT
        sut.perform(reqDTO);

        //VERIFY
        verify(sut).setInitialFeeAndModifiedFeeValues(averageMerCalculationDtos);

    }

    @Test
    void perform_verifyGetResultAndSetNullForScaledAndForcedReportFeeIfHoldingContainsNoFunds() {
        //SETUP
        final var requestValidator = mock(AverageMerRequestValidator.class);
        final var averageMERCacheStorage = mock(AverageMERCacheStorage.class);
        final var sut = mock(MERCalculationServiceImpl.class, withSettings().
                useConstructor(averageMERCacheStorage, requestValidator));


        final var resDto = mock(AverageMerResponse.class);
        final var reqDTO = mock(AverageMerRequestDTO.class);
        final var managementExpenseRatio = mock(Map.class);

        when(resDto.getManagementExpenseRatio()).thenReturn(managementExpenseRatio);
        when(sut.calculateAverageValue(any(), any())).thenReturn(resDto);

        doCallRealMethod().when(sut).perform(any());
        doCallRealMethod().when(sut).setNullForScaledAndForcedReportFeeIfHoldingContainsNoFunds(any(AverageMerResponse.class), any());
        //ACT
        sut.perform(reqDTO);

        //VERIFY
        verify(sut).setNullForScaledAndForcedReportFeeIfHoldingContainsNoFunds(managementExpenseRatio, reqDTO);

    }

    @Test
    void perform_verifyValidateHoldings() {
        //SETUP
        final var requestValidator = mock(AverageMerRequestValidator.class);
        final var averageMERCacheStorage = mock(AverageMERCacheStorage.class);
        final var sut = mock(MERCalculationServiceImpl.class, withSettings().
                useConstructor(averageMERCacheStorage, requestValidator));

        final AverageMerResponse resDto = mock(AverageMerResponse.class);
        when(sut.calculateAverageValue(any(), any())).thenReturn(resDto);

        final AverageMerRequestDTO reqDto = mock(AverageMerRequestDTO.class);
        final List<Holding> holdings = List.of(mock(Holding.class));
        when(reqDto.getHoldings()).thenReturn(holdings);

        doCallRealMethod().when(sut).perform(any());
        doCallRealMethod().when(sut).validateRequest(any());
        //ACT
        final AverageMerResponse actual = sut.perform(reqDto);

        //VERIFY
        verify(requestValidator).validate(reqDto);
    }

    @Test
    void perform_verifyCalculateAverageMER() {
        try (var mockedFilterUtils = Mockito.mockStatic(FilterUtils.class)) {
            //SETUP
            final var requestValidator = mock(AverageMerRequestValidator.class);
            final var averageMERCacheStorage = mock(AverageMERCacheStorage.class);
            final var sut = mock(MERCalculationServiceImpl.class, withSettings().
                    useConstructor(averageMERCacheStorage, requestValidator));

            final HashMap<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> map = new HashMap<>();
            final var reqDTO = mock(AverageMerRequestDTO.class);
            final var parameterTypes = mock(List.class);

            when(averageMERCacheStorage.load(any(), anyList(), anyList(), any())).thenReturn(map);
            mockedFilterUtils.when(() -> FilterUtils.getSpecifiedIfEmpty(any(), any())).thenReturn(parameterTypes);
            when(sut.calculateAverageValue(any(), any())).thenReturn(mock(AverageMerResponse.class));

            doCallRealMethod().when(sut).perform(any());
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
            final var requestValidator = mock(AverageMerRequestValidator.class);
            final var averageMERCacheStorage = mock(AverageMERCacheStorage.class);
            final var sut = mock(MERCalculationServiceImpl.class, withSettings().
                    useConstructor(averageMERCacheStorage, requestValidator));

            final HashMap<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> map = new HashMap<>();
            final var reqDTO = mock(AverageMerRequestDTO.class);
            final var parameterTypes = mock(List.class);

            when(reqDTO.getParameterTypes()).thenReturn(parameterTypes);
            when(averageMERCacheStorage.load(any(), anyList(), anyList(), any())).thenReturn(map);
            when(sut.calculateAverageValue(any(), any())).thenReturn(mock(AverageMerResponse.class));

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
            final var requestValidator = mock(AverageMerRequestValidator.class);
            final var averageMERCacheStorage = mock(AverageMERCacheStorage.class);
            final var sut = mock(MERCalculationServiceImpl.class, withSettings().
                    useConstructor(averageMERCacheStorage, requestValidator));


            final HashMap<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> map = new HashMap<>();
            final var reqDTO = mock(AverageMerRequestDTO.class);
            final var providers = mock(List.class);

            when(reqDTO.getDataProviders()).thenReturn(providers);
            when(averageMERCacheStorage.load(any(), anyList(), anyList(), any())).thenReturn(map);
            when(sut.calculateAverageValue(any(), any())).thenReturn(mock(AverageMerResponse.class));

            doCallRealMethod().when(sut).perform(any());
            doCallRealMethod().when(sut).loadDataFromCacheStorage(any());
            //ACT
            sut.perform(reqDTO);

            //VERIFY
            mockedFilterUtils.verify(() -> FilterUtils.getSpecifiedIfEmpty(providers, DataProvider.DEFAULT_PROVIDERS));
        }
    }

    @Test
    void setInitialFeeAndModifiedFeeValues_callsSetForCanadaEtfAndCanadaMutualFunds_WithCanadaEtfType() {
        //SETUP
        final MERCalculationServiceImpl merCalculationServiceMock = mock(MERCalculationServiceImpl.class);
        final Notification notification = new Notification();

        final Holding h = mock(Holding.class);
        final AverageManagementExpenseCalculationDTO a = new AverageManagementExpenseCalculationDTO();

        doCallRealMethod().when(merCalculationServiceMock).setInitialFeeAndModifiedFeeValues(anyMap());
        //ACT
        merCalculationServiceMock.setInitialFeeAndModifiedFeeValues(Map.of(HoldingType.CANADA_ETF, Map.of(h, a)));

        //VERIFY
        verify(merCalculationServiceMock).handleFeesForCanadaMutualHedgeFundsAndEtf(a, h, notification);
    }

    @Test
    void setInitialFeeAndModifiedFeeValues_callsSetForCanadaEtfAndCanadaMutualFunds_WithUsEtfType() {
        //SETUP
        final MERCalculationServiceImpl merCalculationServiceMock = mock(MERCalculationServiceImpl.class);
        final Notification notification = new Notification();

        final Holding h = mock(Holding.class);
        final AverageManagementExpenseCalculationDTO a = new AverageManagementExpenseCalculationDTO();

        doCallRealMethod().when(merCalculationServiceMock).setInitialFeeAndModifiedFeeValues(anyMap());
        //ACT
        merCalculationServiceMock.setInitialFeeAndModifiedFeeValues(Map.of(HoldingType.CANADA_MUTUAL_FUNDS, Map.of(h, a)));

        //VERIFY
        verify(merCalculationServiceMock).handleFeesForCanadaMutualHedgeFundsAndEtf(a, h, notification);
    }

    @Test
    void setInitialFeeAndModifiedFeeValues_callsSetForUsEtfType() {
        //SETUP
        final MERCalculationServiceImpl merCalculationServiceMock = mock(MERCalculationServiceImpl.class);
        final Notification notification = new Notification();

        final Holding h = mock(Holding.class);
        final AverageManagementExpenseCalculationDTO aDto = new AverageManagementExpenseCalculationDTO();

        doCallRealMethod().when(merCalculationServiceMock).setInitialFeeAndModifiedFeeValues(anyMap());
        //ACT
        merCalculationServiceMock.setInitialFeeAndModifiedFeeValues(Map.of(HoldingType.US_ETF, Map.of(h, aDto)));

        //VERIFY
        verify(merCalculationServiceMock).handleFeesForUsEtfAndMutualFund(aDto, h, notification);
    }

    @Test
    void setInitialFeeAndModifiedFeeValues_checkResult() {
        //SETUP
        final MERCalculationServiceImpl m = mock(MERCalculationServiceImpl.class);
        final Notification notification = new Notification();

        final Holding h1 = mock(Holding.class);
        final AverageManagementExpenseCalculationDTO aDto1 = mock(AverageManagementExpenseCalculationDTO.class);
        final Holding h2 = mock(Holding.class);
        final AverageManagementExpenseCalculationDTO aDto2 = mock(AverageManagementExpenseCalculationDTO.class);

        final Warning w1 = new Warning(null, "ANY1");
        final Warning w2 = new Warning(null, "ANY2");

        when(m.handleFeesForUsEtfAndMutualFund(aDto1, h1, notification)).thenReturn(Optional.of(w1));
        when(m.handleFeesForCanadaMutualHedgeFundsAndEtf(aDto2, h2, notification)).thenReturn(Optional.of(List.of(w2)));

        doCallRealMethod().when(m).setInitialFeeAndModifiedFeeValues(anyMap());
        //ACT
        final List<Warning> actual = m.setInitialFeeAndModifiedFeeValues(Map.of(
                HoldingType.US_ETF, Map.of(h1, aDto1),
                HoldingType.CANADA_ETF, Map.of(h2, aDto2),
                HoldingType.CANADA_STOCKS, Map.of(mock(Holding.class), mock(AverageManagementExpenseCalculationDTO.class)))
        );

        //VERIFY
        Assertions.assertNotNull(actual);
        ComparisonUtils.compareCollections(List.of(w2, w1), actual);
    }

    @Test
    void setForCanadaEtfAndCanadaMutualFundTypes_callsFillFeeValues_withManagementExpenseRation() {
        //SETUP
        final MERCalculationServiceImpl merCalculationServiceMock = mock(MERCalculationServiceImpl.class);
        final Notification notification = new Notification();

        final AverageManagementExpenseCalculationDTO etfHoldingDto = new AverageManagementExpenseCalculationDTO();
        etfHoldingDto.setHoldingType(HoldingType.CANADA_ETF);

        final BigDecimal mockManagementExpenseRatio = mock(BigDecimal.class);
        etfHoldingDto.setManagementExpenseRatio(mockManagementExpenseRatio);
        etfHoldingDto.setActualManagementFee(mock(BigDecimal.class));

        doCallRealMethod().when(merCalculationServiceMock).handleFeesForCanadaMutualHedgeFundsAndEtf(any(), any(), any());
        //ACT
        merCalculationServiceMock.handleFeesForCanadaMutualHedgeFundsAndEtf(etfHoldingDto, mock(Holding.class), notification);

        //VERIFY
        verify(merCalculationServiceMock).setFeeValues(etfHoldingDto, mockManagementExpenseRatio);
    }

    @Test
    void setForCanadaEtfAndCanadaMutualFundTypes_throwsException() {
        //SETUP
        final MERCalculationServiceImpl merCalculationServiceMock = mock(MERCalculationServiceImpl.class);
        final Notification notification = new Notification();
        final Holding h = mock(Holding.class);

        doCallRealMethod().when(merCalculationServiceMock).handleFeesForCanadaMutualHedgeFundsAndEtf(any(), any(), any());
        //ACT
        merCalculationServiceMock.handleFeesForCanadaMutualHedgeFundsAndEtf(mock(AverageManagementExpenseCalculationDTO.class), h, notification);

        //VERIFY
        assertEquals(1, notification.getErrors().stream().filter(e -> e.getMessage().equals("The holding is missing both MER and Management Fee")).count());
        verify(h).generateUserIdentifier();
    }

    @Test
    void setForUsEtfType_throwsException() {
        //SETUP
        final MERCalculationServiceImpl merCalculationServiceMock = mock(MERCalculationServiceImpl.class);
        final Notification notification = new Notification();
        final Holding h = mock(Holding.class);

        doCallRealMethod().when(merCalculationServiceMock).handleFeesForUsEtfAndMutualFund(any(), any(), any());
        //ACT
        merCalculationServiceMock.handleFeesForUsEtfAndMutualFund(mock(AverageManagementExpenseCalculationDTO.class), h, notification);

        //VERIFY
        assertEquals(1, notification.getErrors().stream().filter(e -> e.getMessage().equals("The holding is missing both Net Expense Ratio and Gross Expense Ratio")).count());
        verify(h).generateUserIdentifier();
//        assertEquals("The holding is missing both Net Expense Ratio and Gross Expense Ratio", e.getMessage());
    }

    @Test
    void setForCanadaEtfAndCanadaMutualFundTypes_merIsPresent() {
        //SETUP
        final MERCalculationServiceImpl merCalculationServiceMock = mock(MERCalculationServiceImpl.class);
        final Notification notification = new Notification();
        final Holding h = mock(Holding.class);

        final AverageManagementExpenseCalculationDTO a = mock(AverageManagementExpenseCalculationDTO.class);
        when(a.getManagementExpenseRatio()).thenReturn(ONE);

        doCallRealMethod().when(merCalculationServiceMock).handleFeesForCanadaMutualHedgeFundsAndEtf(any(), any(), any());
        //ACT
        final Optional<List<Warning>> warning = merCalculationServiceMock.handleFeesForCanadaMutualHedgeFundsAndEtf(a, h, notification);

        //VERIFY
        verify(a, times(3)).getManagementExpenseRatio();
        assertFalse(warning.isEmpty());
    }

    @Test
    void setForCanadaEtfAndCanadaMutualFundTypes_merIsNotPresent() {
        //SETUP
        final MERCalculationServiceImpl merCalculationServiceMock = mock(MERCalculationServiceImpl.class);
        final Notification notification = new Notification();
        final Holding h = mock(Holding.class);

        final AverageManagementExpenseCalculationDTO a = mock(AverageManagementExpenseCalculationDTO.class);
        when(a.getActualManagementFee()).thenReturn(ONE);

        doCallRealMethod().when(merCalculationServiceMock).handleFeesForCanadaMutualHedgeFundsAndEtf(any(), any(), any());
        //ACT
        final Optional<List<Warning>> warning = merCalculationServiceMock.handleFeesForCanadaMutualHedgeFundsAndEtf(a, h, notification);

        //VERIFY
        verify(h).generateUserIdentifier();
        verify(a, times(2)).getActualManagementFee();
        assertTrue(warning.isPresent());
        assertEquals(List.of(new Warning(null, "The holding is missing Management Expense Ratio", "WRN_MER_MER_001")), warning.get());
    }

    @Test
    void setForUsEtfType_netIsPresent() {
        //SETUP
        final MERCalculationServiceImpl merCalculationServiceMock = mock(MERCalculationServiceImpl.class);
        final Notification notification = new Notification();
        final Holding h = mock(Holding.class);

        final AverageManagementExpenseCalculationDTO a = mock(AverageManagementExpenseCalculationDTO.class);
        when(a.getNetExpenseRatio()).thenReturn(ONE);

        doCallRealMethod().when(merCalculationServiceMock).handleFeesForUsEtfAndMutualFund(any(), any(), any());
        //ACT
        final Optional<Warning> warning = merCalculationServiceMock.handleFeesForUsEtfAndMutualFund(a, h, notification);

        //VERIFY
        verify(a, times(3)).getNetExpenseRatio();
        assertFalse(warning.isEmpty());
    }

    @Test
    void setForUsEtfType_netIsNotPresent() {
        //SETUP
        final MERCalculationServiceImpl merCalculationServiceMock = mock(MERCalculationServiceImpl.class);
        final Notification notification = new Notification();
        final Holding h = mock(Holding.class);

        final AverageManagementExpenseCalculationDTO a = mock(AverageManagementExpenseCalculationDTO.class);
        when(a.getGrossExpenseRatio()).thenReturn(ONE);

        doCallRealMethod().when(merCalculationServiceMock).handleFeesForUsEtfAndMutualFund(any(), any(), any());
        //ACT
        final Optional<Warning> warning = merCalculationServiceMock.handleFeesForUsEtfAndMutualFund(a, h, notification);

        //VERIFY
        verify(h).generateUserIdentifier();
        verify(a, times(2)).getGrossExpenseRatio();
        assertTrue(warning.isPresent());
        assertEquals(new Warning(null, "The holding is missing Net Expense Ratio", "WRN_MER_NER_001"), warning.get());
    }

    @Test
    void setForCanadaEtfAndCanadaMutualFundTypes_callsFillFeeValues_withActualManagementFee() {
        //SETUP
        final MERCalculationServiceImpl merCalculationServiceMock = mock(MERCalculationServiceImpl.class);
        final Notification notification = new Notification();
        final AverageManagementExpenseCalculationDTO etfHoldingDto = new AverageManagementExpenseCalculationDTO();
        etfHoldingDto.setHoldingType(HoldingType.CANADA_ETF);
        final BigDecimal mockActualManagementFee = mock(BigDecimal.class);
        etfHoldingDto.setActualManagementFee(mockActualManagementFee);

        doCallRealMethod().when(merCalculationServiceMock).handleFeesForCanadaMutualHedgeFundsAndEtf(any(), any(), any());
        //ACT
        merCalculationServiceMock.handleFeesForCanadaMutualHedgeFundsAndEtf(etfHoldingDto, mock(Holding.class), notification);

        //VERIFY
        verify(merCalculationServiceMock).setFeeValues(etfHoldingDto, mockActualManagementFee);
    }

    @Test
    void calculateAverageMER_verifyGetScaledAverageMer() {
        //SETUP
        final var sut = mock(MERCalculationServiceImpl.class);
        final Notification notification = new Notification();

        final var parameterTypes = mock(List.class);
        final var averageMerCalculationDtos = mock(Map.class);

        when(parameterTypes.contains(SCALED)).thenReturn(true);

        doCallRealMethod().when(sut).calculateAverageValue(any(), any());
        //ACT
        sut.calculateAverageValue(parameterTypes, averageMerCalculationDtos);

        //VERIFY
        verify(sut).getScaledAverageMer(averageMerCalculationDtos);
    }

    @Test
    void calculateAverageMER_verifyGetAbsoluteAverageMer() {
        //SETUP
        final var sut = mock(MERCalculationServiceImpl.class);

        final var parameterTypes = mock(List.class);
        final var averageMerCalculationDtos = mock(Map.class);

        when(parameterTypes.contains(ABSOLUTE)).thenReturn(true);

        doCallRealMethod().when(sut).calculateAverageValue(any(), any());
        //ACT
        sut.calculateAverageValue(parameterTypes, averageMerCalculationDtos);

        //VERIFY
        verify(sut).getAbsoluteAverageMer(averageMerCalculationDtos);
    }

    @Test
    void calculateAverageMER_verifyGetForceReportFeeAverageMer() {
        //SETUP
        final var sut = mock(MERCalculationServiceImpl.class);

        final var parameterTypes = mock(List.class);
        final var averageMerCalculationDtos = mock(Map.class);

        when(parameterTypes.contains(FORCE_REPORT_FEE)).thenReturn(true);

        doCallRealMethod().when(sut).calculateAverageValue(any(), any());
        //ACT
        sut.calculateAverageValue(parameterTypes, averageMerCalculationDtos);

        //VERIFY
        verify(sut).getForceReportFeeAverageMer(averageMerCalculationDtos);
    }

    @Test
    void calculateAverageMER_checkResult1() {
        //SETUP
        final var sut = mock(MERCalculationServiceImpl.class);

        final var expected = new AverageMerResponse();
        expected.getManagementExpenseRatio().putAll(Map.of(SCALED, ZERO, ABSOLUTE, ONE, FORCE_REPORT_FEE, TEN));

        final var parameterTypes = mock(List.class);
        final var averageMerCalculationDtos = mock(Map.class);

        when(parameterTypes.contains(SCALED)).thenReturn(true);
        when(parameterTypes.contains(ABSOLUTE)).thenReturn(true);
        when(parameterTypes.contains(FORCE_REPORT_FEE)).thenReturn(true);

        when(sut.getScaledAverageMer(averageMerCalculationDtos)).thenReturn(ZERO);
        when(sut.getAbsoluteAverageMer(averageMerCalculationDtos)).thenReturn(ONE);
        when(sut.getForceReportFeeAverageMer(averageMerCalculationDtos)).thenReturn(TEN);

        doCallRealMethod().when(sut).calculateAverageValue(any(), any());
        //ACT
        final var actual = sut.calculateAverageValue(parameterTypes, averageMerCalculationDtos);

        //VERIFY
        assertEquals(expected, actual);
    }

    @Test
    void calculateAverageMER_checkResult2() {
        //SETUP
        final var sut = mock(MERCalculationServiceImpl.class);

        final var expected = new AverageMerResponse();
        expected.getManagementExpenseRatio().putAll(Map.of(SCALED, ZERO, ABSOLUTE, ONE));

        final var parameterTypes = mock(List.class);
        final var averageMerCalculationDtos = mock(Map.class);

        when(parameterTypes.contains(SCALED)).thenReturn(true);
        when(parameterTypes.contains(ABSOLUTE)).thenReturn(true);

        when(sut.getScaledAverageMer(averageMerCalculationDtos)).thenReturn(ZERO);
        when(sut.getAbsoluteAverageMer(averageMerCalculationDtos)).thenReturn(ONE);

        doCallRealMethod().when(sut).calculateAverageValue(any(), any());
        //ACT
        final var actual = sut.calculateAverageValue(parameterTypes, averageMerCalculationDtos);

        //VERIFY
        assertEquals(expected, actual);
    }

    @Test
    void calculateAverageMER_checkResult3() {
        //SETUP
        final var sut = mock(MERCalculationServiceImpl.class);

        final var expected = new AverageMerResponse();
        expected.getManagementExpenseRatio().putAll(Map.of(SCALED, ZERO));

        final var parameterTypes = mock(List.class);
        final var averageMerCalculationDtos = mock(Map.class);

        when(parameterTypes.contains(SCALED)).thenReturn(true);

        when(sut.getScaledAverageMer(averageMerCalculationDtos)).thenReturn(ZERO);

        doCallRealMethod().when(sut).calculateAverageValue(any(), any());
        //ACT
        final var actual = sut.calculateAverageValue(parameterTypes, averageMerCalculationDtos);

        //VERIFY
        assertEquals(expected, actual);
    }

    @Test
    void handleFeesForUsEtf_verifySetFeeValues() {
        //SETUP
        final var sut = mock(MERCalculationServiceImpl.class);
        final Notification notification = new Notification();

        final var holding = mock(Holding.class);
        final var input = mock(AverageManagementExpenseCalculationDTO.class);

        final BigDecimal bigDecimal = mock(BigDecimal.class);
        when(input.getNetExpenseRatio()).thenReturn(bigDecimal);
        when(input.getGrossExpenseRatio()).thenReturn(mock(BigDecimal.class));

        doCallRealMethod().when(sut).handleFeesForUsEtfAndMutualFund(any(), any(), any());

        //ACT
        sut.handleFeesForUsEtfAndMutualFund(input, holding, notification);

        //VERIFY
        verify(sut).setFeeValues(eq(input), same(bigDecimal));
    }

    @Test
    void handleFeesForUsEtf_checkResult() {
        //SETUP
        final var sut = mock(MERCalculationServiceImpl.class);
        final Notification notification = new Notification();

        final var holding = mock(Holding.class);
        final var input = mock(AverageManagementExpenseCalculationDTO.class);

        final BigDecimal bigDecimal = mock(BigDecimal.class);
        when(input.getNetExpenseRatio()).thenReturn(bigDecimal);
        when(input.getGrossExpenseRatio()).thenReturn(mock(BigDecimal.class));

        doCallRealMethod().when(sut).handleFeesForUsEtfAndMutualFund(any(), any(), any());

        //ACT
        final Optional<Warning> actual = sut.handleFeesForUsEtfAndMutualFund(input, holding, notification);

        //VERIFY
        assertEquals(Optional.empty(), actual);
    }

    @Test
    void handleFeesForCanadaMutualHedgeFundsAndEtf_returnsTwoWarningsInCaseOfAbsentDataForCanadaHedgeFund() {
        //SETUP
        final var sut = mock(MERCalculationServiceImpl.class);
        final Notification notification = new Notification();

        final var holding = new Holding();
        holding.setType(HoldingType.CANADA_HEDGE_FUNDS);
        final var input = mock(AverageManagementExpenseCalculationDTO.class);
        final Optional<List<Warning>> expected = Optional.of(List.of(WRN_MER_MER_001.warning(holding), WRN_MER_AMF_001.warning(holding)));

        doCallRealMethod().when(sut).handleFeesForCanadaMutualHedgeFundsAndEtf(any(), any(), any());

        //ACT
        final Optional<List<Warning>> actual = sut.handleFeesForCanadaMutualHedgeFundsAndEtf(input, holding, notification);

        //VERIFY
        assertEquals(expected, actual);
    }
}