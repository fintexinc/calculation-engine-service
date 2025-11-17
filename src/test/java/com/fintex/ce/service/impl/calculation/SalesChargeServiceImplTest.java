package com.fintex.ce.service.impl.calculation;

import com.fintex.ce.domain.calculation.SalesChargeCalculation;
import com.fintex.ce.dto.request.PortfolioHoldingsReqDTO;
import com.fintex.ce.dto.response.SalesChargeResDtos;
import com.fintex.ce.service.impl.cache.SalesChargeCacheStorage;
import com.fintex.ce.util.validation.request.PortfolioHoldingsReqDtoValidator;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SalesChargeServiceImplTest {

    @Test
    void perform_verifyValidate() {
        //SETUP
        final var requestValidator = mock(PortfolioHoldingsReqDtoValidator.class);
        final var cacheStorage = mock(SalesChargeCacheStorage.class);
        final var sut = mock(SalesChargeServiceImpl.class, withSettings().
                useConstructor(cacheStorage, requestValidator));
        final var reqDTO = mock(PortfolioHoldingsReqDTO.class);

        final var salesCharge = mock(Map.class);
        when(cacheStorage.load(anyList(), anyList(), anyList(), any())).thenReturn(salesCharge);
        doNothing().when(requestValidator).validate(reqDTO);
        when(sut.getSalesChargeCalculation(salesCharge)).thenReturn(mock(SalesChargeCalculation.class));

        doCallRealMethod().when(sut).perform(any());
        //ACT
        sut.perform(reqDTO);

        //VERIFY
        verify(requestValidator).validate(reqDTO);
    }

    @Test
    void perform_verifyLoad() {
        //SETUP
        final var requestValidator = mock(PortfolioHoldingsReqDtoValidator.class);
        final var cacheStorage = mock(SalesChargeCacheStorage.class);
        final var sut = mock(SalesChargeServiceImpl.class, withSettings().
                useConstructor(cacheStorage, requestValidator));
        final var reqDTO = mock(PortfolioHoldingsReqDTO.class);

        final var salesCharge = mock(Map.class);
        when(cacheStorage.load(anyList(), anyList(), anyList(), any())).thenReturn(salesCharge);
        doNothing().when(requestValidator).validate(reqDTO);
        when(sut.getSalesChargeCalculation(salesCharge)).thenReturn(mock(SalesChargeCalculation.class));

        doCallRealMethod().when(sut).perform(any());
        //ACT
        sut.perform(reqDTO);

        //VERIFY
        verify(cacheStorage).load(anyList(), anyList(), anyList(), any());
    }

    @Test
    void perform_verifyGetSalesChargeCalculation() {
        //SETUP
        final var requestValidator = mock(PortfolioHoldingsReqDtoValidator.class);
        final var cacheStorage = mock(SalesChargeCacheStorage.class);
        final var sut = mock(SalesChargeServiceImpl.class, withSettings().
                useConstructor(cacheStorage, requestValidator));
        final var reqDTO = mock(PortfolioHoldingsReqDTO.class);

        final var salesCharge = mock(Map.class);
        when(cacheStorage.load(anyList(), anyList(), anyList(), any())).thenReturn(salesCharge);
        doNothing().when(requestValidator).validate(reqDTO);
        when(sut.getSalesChargeCalculation(salesCharge)).thenReturn(mock(SalesChargeCalculation.class));

        doCallRealMethod().when(sut).perform(any());
        //ACT
        sut.perform(reqDTO);

        //VERIFY
        verify(sut).getSalesChargeCalculation(salesCharge);
    }

    @Test
    void perform_checkResult() {
        //SETUP
        final var requestValidator = mock(PortfolioHoldingsReqDtoValidator.class);
        final var cacheStorage = mock(SalesChargeCacheStorage.class);
        final var sut = mock(SalesChargeServiceImpl.class, withSettings().
                useConstructor(cacheStorage, requestValidator));
        final var reqDTO = mock(PortfolioHoldingsReqDTO.class);
        final SalesChargeCalculation calculation = mock(SalesChargeCalculation.class);
        final SalesChargeResDtos expected = mock(SalesChargeResDtos.class);

        doNothing().when(requestValidator).validate(reqDTO);
        final var salesCharge = mock(Map.class);
        when(cacheStorage.load(anyList(), anyList(), anyList(), any())).thenReturn(salesCharge);
        when(sut.getSalesChargeCalculation(salesCharge)).thenReturn(calculation);
        when(calculation.calculate()).thenReturn(expected);

        doCallRealMethod().when(sut).perform(any());
        //ACT
        final SalesChargeResDtos actual = sut.perform(reqDTO);

        //VERIFY
        assertSame(expected, actual);
    }

}
