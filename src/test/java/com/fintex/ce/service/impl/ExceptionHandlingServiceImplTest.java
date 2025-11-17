package com.fintex.ce.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fintex.ce.config.enumeration.HoldingType;
import com.fintex.ce.dto.holding.CashHolding;
import com.fintex.ce.dto.holding.EtfHolding;
import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.holding.HoldingsDTO;
import com.fintex.ce.dto.request.MultiplePortfoliosReqDTO;
import com.fintex.ce.exception.DataErrorException;
import com.fintex.ce.model.redis.RBusinessCountry;
import com.fintex.ce.repository.redis.FxRatesRepository;
import com.fintex.ce.repository.redis.businesscountry.BusinessCountryRepository;
import com.fintex.ce.util.ComparisonUtils;
import com.fintex.ce.util.JacksonUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.fintex.ce.config.constant.BigDecimalConstants.ONE;
import static com.fintex.ce.config.constant.GeneralConstants.DELIMITER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class ExceptionHandlingServiceImplTest {

    @Test
    void removeRedisCacheForRequestedHoldings_verifyGetRequestBody() throws JsonProcessingException {
        //SETUP
        final HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        final DataErrorException reqErrorException = mock(DataErrorException.class);
        when(httpServletRequest.getMethod()).thenReturn("POST");
        final FxRatesRepository fxRatesRepository = mock(FxRatesRepository.class);
        final ExceptionHandlingServiceImpl sut = mock(ExceptionHandlingServiceImpl.class,
                withSettings().useConstructor(mock(List.class), fxRatesRepository));
        when(sut.getRequestBody(any())).thenReturn("body");

        doCallRealMethod().when(sut).removeRedisCacheForRequestedHoldings(any(), any(), any());

        //ACT
        sut.removeRedisCacheForRequestedHoldings(httpServletRequest, reqErrorException, "");

        //VERIFY
        verify(sut).getRequestBody(httpServletRequest);
    }

    @Test
    void removeRedisCacheForRequestedHoldings_verifyGetHoldingsIds() throws JsonProcessingException {
        //SETUP
        final HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        final DataErrorException reqErrorException = mock(DataErrorException.class);
        when(httpServletRequest.getMethod()).thenReturn("POST");
        final HoldingsDTO holdingsDTO = new HoldingsDTO();
        final FxRatesRepository fxRatesRepository = mock(FxRatesRepository.class);
        final ExceptionHandlingServiceImpl sut = mock(ExceptionHandlingServiceImpl.class,
                withSettings().useConstructor(mock(List.class), fxRatesRepository));
        when(sut.getRequestBody(any())).thenReturn(JacksonUtil.serialize(holdingsDTO));

        doCallRealMethod().when(sut).removeRedisCacheForRequestedHoldings(any(), any(), any());

        //ACT
        sut.removeRedisCacheForRequestedHoldings(httpServletRequest, reqErrorException, "");

        //VERIFY
        verify(sut).getHoldingsIds(holdingsDTO);
    }

    @Test
    void removeRedisCacheForRequestedHoldings_verifyRemoveDataFromRepositoriesByHoldingId() throws JsonProcessingException {
        //SETUP
        final HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        final DataErrorException reqErrorException = mock(DataErrorException.class);
        when(httpServletRequest.getMethod()).thenReturn("POST");
        final HoldingsDTO holdingsDTO = mock(HoldingsDTO.class);
        final FxRatesRepository fxRatesCacheStorage = mock(FxRatesRepository.class);
        final ExceptionHandlingServiceImpl sut = mock(ExceptionHandlingServiceImpl.class,
                withSettings().useConstructor(List.of(), fxRatesCacheStorage));
        when(sut.getRequestBody(any())).thenReturn(JacksonUtil.serialize(new HoldingsDTO()));
        when(sut.getHoldingsIds(any())).thenReturn(List.of("id1", "id2", "id3"));

        doCallRealMethod().when(sut).removeRedisCacheForRequestedHoldings(any(), any(), any());

        //ACT
        sut.removeRedisCacheForRequestedHoldings(httpServletRequest, reqErrorException, "");

        //VERIFY
        verify(sut, times(3)).removeDataFromRepositoriesByHoldingId(any());
    }

    @Test
    void getHoldingsIds_verifyCheckAndMergeHoldings() {
        //SETUP
        final ExceptionHandlingServiceImpl reqErrorHandlingService = mock(ExceptionHandlingServiceImpl.class);
        final HoldingsDTO holdingsDTO = mock(HoldingsDTO.class);
        doCallRealMethod().when(reqErrorHandlingService).getHoldingsIds(any());

        //ACT
        reqErrorHandlingService.getHoldingsIds(holdingsDTO);

        //VERIFY
        verify(reqErrorHandlingService).mergeHoldings(holdingsDTO);
    }

    @Test
    void getHoldingsIds_checkResult() {
        //SETUP
        final ExceptionHandlingServiceImpl sut = mock(ExceptionHandlingServiceImpl.class);
        final HoldingsDTO holdingsDTO = mock(HoldingsDTO.class);
        final FundSeriesHolding holding = mock(FundSeriesHolding.class);
        final EtfHolding etfHolding = mock(EtfHolding.class);
        when(holding.getType()).thenReturn(HoldingType.CANADA_MUTUAL_FUNDS);
        when(etfHolding.getType()).thenReturn(HoldingType.US_ETF);
        final String fundServCode = "FundServCode";
        final String ticker = "Ticker";
        final String echangeId = "EchangeId";
        when(holding.getFundServCode()).thenReturn(fundServCode);
        when(etfHolding.getTicker()).thenReturn(ticker);
        when(etfHolding.getExchangeCode()).thenReturn(echangeId);
        when(holdingsDTO.getHoldings()).thenReturn(List.of(holding, etfHolding));
        doCallRealMethod().when(sut).getHoldingsIds(any());
        doCallRealMethod().when(etfHolding).generateUserIdentifier();
        doCallRealMethod().when(sut).mergeHoldings(holdingsDTO);

        //ACT
        final List<String> holdingsIds = sut.getHoldingsIds(holdingsDTO);

        //VERIFY
        assertEquals(2, holdingsIds.size());
        assertEquals(fundServCode, holdingsIds.get(0));
        assertEquals(HoldingType.US_ETF + DELIMITER + ticker + DELIMITER + echangeId, holdingsIds.get(1));
    }

    @Test
    void removeDataFromRepositoriesByHoldingId_verifyFindAllByHoldingId() throws JsonProcessingException {
        //SETUP
        final HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        final DataErrorException reqErrorException = mock(DataErrorException.class);
        when(httpServletRequest.getMethod()).thenReturn("POST");
        final String id = "id";
        final HoldingsDTO holdingsDTO = mock(HoldingsDTO.class);
        final BusinessCountryRepository businessCountryRepository = mock(BusinessCountryRepository.class);
        final FxRatesRepository fxRatesRepository = mock(FxRatesRepository.class);
        final ExceptionHandlingServiceImpl sut = mock(ExceptionHandlingServiceImpl.class,
                withSettings().useConstructor(List.of(businessCountryRepository), fxRatesRepository));
        doCallRealMethod().when(sut).removeDataFromRepositoriesByHoldingId(any());

        //ACT
        sut.removeDataFromRepositoriesByHoldingId(id);

        //VERIFY
        verify(businessCountryRepository).findAllByHoldingId(id);
    }

    @Test
    void removeDataFromRepositoriesByHoldingId_verifyDeleteById() throws JsonProcessingException {
        //SETUP
        final HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        final DataErrorException reqErrorException = mock(DataErrorException.class);
        when(httpServletRequest.getMethod()).thenReturn("POST");
        final String id = "id";
        final HoldingsDTO holdingsDTO = mock(HoldingsDTO.class);
        final BusinessCountryRepository businessCountryRepository = mock(BusinessCountryRepository.class);
        final RBusinessCountry redisId = mock(RBusinessCountry.class);
        when(redisId.getId()).thenReturn(id);
        when(businessCountryRepository.findAllByHoldingId(id)).thenReturn(List.of(redisId));
        final FxRatesRepository fxRatesRepository = mock(FxRatesRepository.class);
        final ExceptionHandlingServiceImpl sut = mock(ExceptionHandlingServiceImpl.class,
                withSettings().useConstructor(List.of(businessCountryRepository), fxRatesRepository));
        doCallRealMethod().when(sut).removeDataFromRepositoriesByHoldingId(any());

        //ACT
        sut.removeDataFromRepositoriesByHoldingId(id);

        //VERIFY
        verify(businessCountryRepository).deleteById(id);
    }

    @Test
    void removeFxRatesFromRedisCache_verifyFxRateRepositoryDeleteAll() {
        //SETUP
        final FxRatesRepository fxRatesRepository = mock(FxRatesRepository.class);
        final var sut = mock(ExceptionHandlingServiceImpl.class,
                withSettings().useConstructor(mock(List.class), fxRatesRepository));

        doCallRealMethod().when(sut).removeFxRatesFromRedisCache();
        //ACT
        sut.removeFxRatesFromRedisCache();

        //VERIFY
        verify(fxRatesRepository).deleteAll();
    }

    @Test
    void mergeHoldings_checkResult() {
        //SETUP
        final var sut = mock(ExceptionHandlingServiceImpl.class);

        final var holdingsDto = new HoldingsDTO();
        final var holdings = new ArrayList<>(List.of(mock(Holding.class)));
        final var benchmarks = new ArrayList<>(List.of(mock(Holding.class)));
        holdingsDto.setHoldings(holdings);
        holdingsDto.setBenchmarkHoldings(benchmarks);

        doCallRealMethod().when(sut).mergeHoldings(any());
        //ACT
        final List<Holding> actual = sut.mergeHoldings(holdingsDto);

        //VERIFY
        final List<Holding> expected = List.of(holdings.get(0), benchmarks.get(0));
        Assertions.assertNotNull(actual);
        ComparisonUtils.compareCollections(expected, actual);
    }

    @Test
    void mergeHoldings_holdingsAndBenchmarksAndPortfoliosAreNotEmpty_expectedResultHoldingsAndBenchmarksAndPortfolios() {
        //SETUP
        final var sut = mock(ExceptionHandlingServiceImpl.class);

        final var holdingsDto = new HoldingsDTO();
        final var holdings = List.of(
                new CashHolding(ONE, HoldingType.CASH),
                new FundSeriesHolding(BigDecimal.valueOf(10), "FundServCode"),
                new EtfHolding(BigDecimal.ONE, HoldingType.CANADA_ETF, "exchangeCode", "ticker")
        );
        final var benchmarks = List.of(
                new CashHolding(BigDecimal.valueOf(11), HoldingType.CASH),
                new FundSeriesHolding(BigDecimal.valueOf(20), "FundServCode1"),
                new EtfHolding(BigDecimal.valueOf(100), HoldingType.US_ETF, "exchangeCode1", "ticker1")
        );

        final var portfolio1 = new MultiplePortfoliosReqDTO.Portfolio();
        portfolio1.setHoldings(List.of(new EtfHolding(BigDecimal.valueOf(123), HoldingType.US_ETF, "exCode", "ticker")));
        final var portfolio2 = new MultiplePortfoliosReqDTO.Portfolio();
        portfolio2.setHoldings(List.of(new EtfHolding(BigDecimal.valueOf(1213), HoldingType.CANADA_ETF, "exCodeCanada", "ticker3")));

        holdingsDto.setHoldings(holdings);
        holdingsDto.setBenchmarkHoldings(benchmarks);
        holdingsDto.setPortfolios(Set.of(portfolio1, portfolio2));

        final var expected = new ArrayList<Holding>();
        expected.addAll(holdings);
        expected.addAll(benchmarks);
        expected.addAll(portfolio1.getHoldings());
        expected.addAll(portfolio2.getHoldings());

        doCallRealMethod().when(sut).mergeHoldings(any());
        //ACT
        final List<Holding> actual = sut.mergeHoldings(holdingsDto);

        //VERIFY
        Assertions.assertNotNull(actual);
        ComparisonUtils.compareCollections(expected, actual);
    }

    @Test
    void mergeHoldings_holdingsIsNotEmpty_expectedResultHoldings() {
        //SETUP
        final var sut = mock(ExceptionHandlingServiceImpl.class);

        final var holdingsDto = new HoldingsDTO();
        final var holdings = List.of(
                new CashHolding(ONE, HoldingType.CASH),
                new FundSeriesHolding(BigDecimal.valueOf(10), "FundServCode"),
                new EtfHolding(BigDecimal.ONE, HoldingType.CANADA_ETF, "exchangeCode", "ticker")
        );
        holdingsDto.setHoldings(holdings);

        doCallRealMethod().when(sut).mergeHoldings(any());

        //ACT
        final List<Holding> actual = sut.mergeHoldings(holdingsDto);

        //VERIFY
        Assertions.assertNotNull(actual);
        ComparisonUtils.compareCollections(holdings, actual);
    }

    @Test
    void mergeHoldings_holdingsAndBenchmarksAreNotEmpty_expectedResultHoldingsAndBenchmarks() {
        //SETUP
        final var sut = mock(ExceptionHandlingServiceImpl.class);

        final var holdingsDto = new HoldingsDTO();
        final var holdings = List.of(
                new CashHolding(ONE, HoldingType.CASH),
                new FundSeriesHolding(BigDecimal.valueOf(10), "FundServCode"),
                new EtfHolding(BigDecimal.ONE, HoldingType.CANADA_ETF, "exchangeCode", "ticker")
        );
        final var benchmarks = List.of(
                new CashHolding(BigDecimal.valueOf(11), HoldingType.CASH),
                new FundSeriesHolding(BigDecimal.valueOf(20), "FundServCode1"),
                new EtfHolding(BigDecimal.valueOf(100), HoldingType.US_ETF, "exchangeCode1", "ticker1")
        );
        holdingsDto.setHoldings(holdings);
        holdingsDto.setBenchmarkHoldings(benchmarks);

        final var expected = new ArrayList<Holding>();
        expected.addAll(holdings);
        expected.addAll(benchmarks);

        doCallRealMethod().when(sut).mergeHoldings(any());
        //ACT
        final List<Holding> actual = sut.mergeHoldings(holdingsDto);

        //VERIFY
        Assertions.assertNotNull(actual);
        ComparisonUtils.compareCollections(expected, actual);
    }

    @Test
    void getRequestBody_checkResult() {
        //SETUP
        final var sut = mock(ExceptionHandlingServiceImpl.class);
        final var request = mock(ContentCachingRequestWrapper.class);
        final var expectedString = "String";

        when(request.getContentAsByteArray()).thenReturn(expectedString.getBytes());
        doCallRealMethod().when(sut).getRequestBody(any());

        //ACT
        final String actual = sut.getRequestBody(request);

        //VERIFY
        assertEquals(expectedString, actual);
    }

}
