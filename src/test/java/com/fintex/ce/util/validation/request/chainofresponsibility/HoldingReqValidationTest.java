package com.fintex.ce.util.validation.request.chainofresponsibility;

import com.fintex.ce.config.enumeration.Currency;
import com.fintex.ce.config.enumeration.HoldingIdentifierType;
import com.fintex.ce.config.enumeration.HoldingType;
import com.fintex.ce.config.enumeration.InterestFreq;
import com.fintex.ce.dto.holding.BenchmarkIndexHolding;
import com.fintex.ce.dto.holding.CashHolding;
import com.fintex.ce.dto.holding.EtfHolding;
import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.dto.holding.GicHolding;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.holding.StockHolding;
import com.fintex.ce.exception.ReqValidationException;
import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.Test;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static com.fintex.ce.config.enumeration.Currency.USD;
import static com.fintex.ce.config.enumeration.ExceptionCode.ERR_DH_001;
import static com.fintex.ce.config.enumeration.ExceptionCode.ERR_RRC_MC_002;
import static com.fintex.ce.util.validation.request.chainofresponsibility.HoldingReqValidation.throwException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@EqualsAndHashCode
class HoldingReqValidationTest {

    private static final LocalDate FIRST_DAY = LocalDate.of(2010, 1, 1);

    @Test
    void validateHoldings_duplicatedGicHoldingIsAllowed() {
        //SETUP

        final FundSeriesHolding f = mock(FundSeriesHolding.class);
        when(f.getFundServCode()).thenReturn("F");
        when(f.getType()).thenReturn(HoldingType.CANADA_MUTUAL_FUNDS);
        when(f.getValue()).thenReturn(BigDecimal.ONE);
        when(f.getHoldingIdentifier()).thenReturn(HoldingIdentifierType.FUNDSERV);

        final EtfHolding e = mock(EtfHolding.class);
        when(e.getTicker()).thenReturn("F2");
        when(e.getType()).thenReturn(HoldingType.US_ETF);
        when(e.getValue()).thenReturn(BigDecimal.ONE);
        when(e.getHoldingIdentifier()).thenReturn(HoldingIdentifierType.TICKER);

        final StockHolding s = mock(StockHolding.class);
        when(s.getTicker()).thenReturn("F2");
        when(s.getExchangeCode()).thenReturn("F22");
        when(s.getType()).thenReturn(HoldingType.US_ETF);
        when(s.getValue()).thenReturn(BigDecimal.ONE);
        when(s.getHoldingIdentifier()).thenReturn(HoldingIdentifierType.TICKER);

        final CashHolding c = getCashHolding();
        final GicHolding gic = new GicHolding(BigDecimal.ONE, HoldingType.GIC);
        gic.setInvestmentDate(LocalDate.now());
        gic.setClientIntRate(BigDecimal.valueOf(100));
        gic.setCurrency(USD);
        gic.setInterestFreq(InterestFreq.MONTHLY);

        final List<Holding> holdings = List.of(f, e, s, c, gic, gic, gic, gic);

        final var sut = new HoldingReqValidation(holdings);

        //ACT
        sut.check();

        //VERIFY
    }

    @Test
    void validateHoldings_currencyOfCashIsEmpty() {
        //SETUP
        final CashHolding cashHoldingWithoutCurrency = new CashHolding();
        cashHoldingWithoutCurrency.setType(HoldingType.CASH);
        cashHoldingWithoutCurrency.setValue(BigDecimal.ONE);
        cashHoldingWithoutCurrency.setHoldingIdentifier(HoldingIdentifierType.FUNDSERV);

        final CashHolding cashHoldingWithCurrencyCAD = new CashHolding();
        cashHoldingWithCurrencyCAD.setType(HoldingType.CASH);
        cashHoldingWithCurrencyCAD.setValue(BigDecimal.ONE);
        cashHoldingWithCurrencyCAD.setHoldingIdentifier(HoldingIdentifierType.FUNDSERV);
        cashHoldingWithCurrencyCAD.setCurrency(Currency.CAD);

        final List<Holding> holdings = List.of(cashHoldingWithoutCurrency, cashHoldingWithCurrencyCAD);
        final var sut = new HoldingReqValidation(holdings);

        final ReqValidationException expected = ERR_RRC_MC_002.reqValidationError();

        //ACT
        final ReqValidationException actual = assertThrows(ReqValidationException.class, () -> sut.check());

        //VERIFY
        assertEquals(expected, actual);
        assertEquals(expected.getMessage(), actual.getMessage());
    }

    @Test
    void validateHoldings_stockHoldingIdentifierIsNull() {
        //SETUP
        final StockHolding stockHolding = new StockHolding();
        stockHolding.setTicker("F");
        stockHolding.setType(HoldingType.CANADA_STOCKS);
        stockHolding.setValue(BigDecimal.ONE);
        stockHolding.setHoldingIdentifier(null);

        final List<Holding> holdings = List.of(stockHolding);
        final var sut = new HoldingReqValidation(holdings);

        final ReqValidationException expected = throwException(stockHolding, "Stock exchange code could not be null");

        //ACT
        final ReqValidationException actual = assertThrows(ReqValidationException.class, () -> sut.check());

        //VERIFY
        assertEquals(expected, actual);
        assertEquals(expected.getMessage(), actual.getMessage());
    }

    @Test
    void validateHoldings_etfHasHoldingIdentifierIsNull() {
        //SETUP
        final EtfHolding etfHolding = new EtfHolding();
        etfHolding.setTicker("F");
        etfHolding.setType(HoldingType.US_ETF);
        etfHolding.setValue(BigDecimal.ONE);
        etfHolding.setHoldingIdentifier(null);

        final List<Holding> holdings = List.of(etfHolding);
        final var sut = new HoldingReqValidation(holdings);

        final ReqValidationException expected = throwException(etfHolding, "Holding identifier could not be null");

        //ACT
        final ReqValidationException actual = assertThrows(ReqValidationException.class, () -> sut.check());

        //VERIFY
        assertEquals(expected, actual);
        assertEquals(expected.getMessage(), actual.getMessage());
    }

    @Test
    void validateHoldings_checkHoldingsIsUnique() {
        //SETUP
        final FundSeriesHolding f = new FundSeriesHolding();
        f.setFundServCode("F");
        f.setType(HoldingType.CANADA_MUTUAL_FUNDS);
        f.setValue(BigDecimal.ONE);
        f.setHoldingIdentifier(HoldingIdentifierType.FUNDSERV);

        final List<Holding> holdings = List.of(f, f);
        final var sut = new HoldingReqValidation(holdings);

        final ReqValidationException expected = ERR_DH_001.reqValidationError();

        //ACT
        final ReqValidationException actual = assertThrows(ReqValidationException.class, () -> sut.check());

        //VERIFY
        assertEquals(expected, actual);
        assertEquals(expected.getMessage(), actual.getMessage());
    }

    @Test
    void validateHoldings_fine() {
        //SETUP

        final FundSeriesHolding f = mock(FundSeriesHolding.class);
        when(f.getFundServCode()).thenReturn("F");
        when(f.getType()).thenReturn(HoldingType.CANADA_MUTUAL_FUNDS);
        when(f.getValue()).thenReturn(BigDecimal.ONE);
        when(f.getHoldingIdentifier()).thenReturn(HoldingIdentifierType.FUNDSERV);

        final EtfHolding e = mock(EtfHolding.class);
        when(e.getTicker()).thenReturn("F2");
        when(e.getType()).thenReturn(HoldingType.US_ETF);
        when(e.getValue()).thenReturn(BigDecimal.ONE);
        when(e.getHoldingIdentifier()).thenReturn(HoldingIdentifierType.TICKER);

        final StockHolding s = mock(StockHolding.class);
        when(s.getTicker()).thenReturn("F2");
        when(s.getExchangeCode()).thenReturn("F22");
        when(s.getType()).thenReturn(HoldingType.US_ETF);
        when(s.getValue()).thenReturn(BigDecimal.ONE);
        when(s.getHoldingIdentifier()).thenReturn(HoldingIdentifierType.TICKER);

        final CashHolding c = getCashHolding();

        final List<Holding> holdings = List.of(f, e, s, c);

        final var sut = new HoldingReqValidation(holdings);

        //ACT
        sut.check();

        //VERIFY
    }

    @Test
    void validate_fine() {
        //SETUP
        final FundSeriesHolding f = mock(FundSeriesHolding.class);
        when(f.getFundServCode()).thenReturn("F");
        when(f.getType()).thenReturn(HoldingType.CANADA_MUTUAL_FUNDS);
        when(f.getValue()).thenReturn(BigDecimal.ONE);
        when(f.getHoldingIdentifier()).thenReturn(HoldingIdentifierType.FUNDSERV);

        final EtfHolding e = mock(EtfHolding.class);
        when(e.getTicker()).thenReturn("F2");
        when(e.getType()).thenReturn(HoldingType.US_ETF);
        when(e.getValue()).thenReturn(BigDecimal.ONE);
        when(e.getHoldingIdentifier()).thenReturn(HoldingIdentifierType.TICKER);

        final StockHolding s = mock(StockHolding.class);
        when(s.getTicker()).thenReturn("F2");
        when(s.getExchangeCode()).thenReturn("F22");
        when(s.getType()).thenReturn(HoldingType.US_ETF);
        when(s.getValue()).thenReturn(BigDecimal.ONE);
        when(s.getHoldingIdentifier()).thenReturn(HoldingIdentifierType.TICKER);

        final CashHolding c = getCashHolding();

        final List<Holding> holdings = List.of(f, e, s, c);

        final var sut = new HoldingReqValidation(holdings);

        //ACT
        sut.check();

        //VERIFY
    }

    CashHolding getCashHolding() {
        final CashHolding c = new CashHolding();
        c.setType(HoldingType.CASH).setValue(BigDecimal.ONE);
        return c;
    }

    @Test
    void validateHoldings_CANADA_MUTUAL_FUNDS_fundCodeIsEmpty() {
        //SETUP
        final FundSeriesHolding f = mock(FundSeriesHolding.class);
        when(f.getFundServCode()).thenReturn("");
        when(f.getType()).thenReturn(HoldingType.CANADA_MUTUAL_FUNDS);
        when(f.getValue()).thenReturn(BigDecimal.ONE);
        when(f.getHoldingIdentifier()).thenReturn(HoldingIdentifierType.FUNDSERV);

        final List<Holding> holdings = List.of(f);

        final var sut = new HoldingReqValidation(holdings);

        final ReqValidationException expected = throwException(f, "Holding fundServCode could not be empty");

        //ACT
        final ReqValidationException actual = assertThrows(ReqValidationException.class, () -> sut.check());

        //VERIFY
        assertEquals(expected, actual);
        assertEquals(expected.getMessage(), actual.getMessage());
    }

    @Test
    void validateHoldings_typeIsNull() {
        //SETUP
        final FundSeriesHolding f = mock(FundSeriesHolding.class);
        when(f.getFundServCode()).thenReturn("F");
        when(f.getType()).thenReturn(null);
        when(f.getValue()).thenReturn(BigDecimal.ONE);
        when(f.getHoldingIdentifier()).thenReturn(HoldingIdentifierType.FUNDSERV);

        final List<Holding> holdings = List.of(f);

        final var sut = new HoldingReqValidation(holdings);

        final ReqValidationException expected = throwException(f, "Holding type could not be null");

        //ACT
        final ReqValidationException actual = assertThrows(ReqValidationException.class, () -> sut.check());

        //VERIFY
        assertEquals(expected, actual);
        assertEquals(expected.getMessage(), actual.getMessage());
    }

    @Test
    void validateHoldings_CANADA_MUTUAL_FUNDS_holdingIdentifierIsNull() {
        //SETUP
        final FundSeriesHolding f = mock(FundSeriesHolding.class);
        when(f.getFundServCode()).thenReturn("F");
        when(f.getType()).thenReturn(HoldingType.CANADA_MUTUAL_FUNDS);
        when(f.getValue()).thenReturn(BigDecimal.ONE);
        when(f.getHoldingIdentifier()).thenReturn(null);

        final List<Holding> holdings = List.of(f);
        final var sut = new HoldingReqValidation(holdings);

        final ReqValidationException expected = throwException(f, "Holding identifier could not be null");

        //ACT
        final ReqValidationException actual = assertThrows(ReqValidationException.class, () -> sut.check());

        //VERIFY
        assertEquals(expected, actual);
        assertEquals(expected.getMessage(), actual.getMessage());
    }

    @Test
    void validateHoldings_BENCHMARK_INDEX_holdingIdentifierIsNull() {
        //SETUP
        final BenchmarkIndexHolding f = mock(BenchmarkIndexHolding.class);
        when(f.getMrStarId()).thenReturn("F");
        when(f.getType()).thenReturn(HoldingType.BENCHMARK_INDEX);
        when(f.getValue()).thenReturn(BigDecimal.ONE);
        when(f.getHoldingIdentifier()).thenReturn(null);

        final List<Holding> holdings = List.of(f);
        final var sut = new HoldingReqValidation(holdings);

        final ReqValidationException expected = throwException(f, "Benchmark index holding identifier could only be specified as: " + HoldingIdentifierType.MORNINGSTAR_ID);

        //ACT
        final ReqValidationException actual = assertThrows(ReqValidationException.class, () -> sut.check());

        //VERIFY
        assertEquals(expected, actual);
        assertEquals(expected.getMessage(), actual.getMessage());
    }

    @Test
    void validateHoldings_BENCHMARK_INDEX_mrStarIdIsNull() {
        //SETUP
        final BenchmarkIndexHolding f = mock(BenchmarkIndexHolding.class);
        when(f.getMrStarId()).thenReturn(null);
        when(f.getType()).thenReturn(HoldingType.BENCHMARK_INDEX);
        when(f.getValue()).thenReturn(BigDecimal.ONE);
        when(f.getHoldingIdentifier()).thenReturn(HoldingIdentifierType.MORNINGSTAR_ID);

        final List<Holding> holdings = List.of(f);
        final var sut = new HoldingReqValidation(holdings);

        final ReqValidationException expected = throwException(f, "Benchmark index mrStarId could not be empty");

        //ACT
        final ReqValidationException actual = assertThrows(ReqValidationException.class, () -> sut.check());

        //VERIFY
        assertEquals(expected, actual);
        assertEquals(expected.getMessage(), actual.getMessage());
    }

    @Test
    void validateHoldings_BENCHMARK_INDEX_ok() {
        //SETUP
        final BenchmarkIndexHolding f = mock(BenchmarkIndexHolding.class);
        when(f.getMrStarId()).thenReturn("F");
        when(f.getType()).thenReturn(HoldingType.BENCHMARK_INDEX);
        when(f.getValue()).thenReturn(BigDecimal.ONE);
        when(f.getHoldingIdentifier()).thenReturn(HoldingIdentifierType.MORNINGSTAR_ID);

        final List<Holding> holdings = List.of(f);
        final var sut = new HoldingReqValidation(holdings);

        //ACT
        assertDoesNotThrow(sut::check);

        //VERIFY
    }

    @Test
    void validateHoldings_EtfHolding_tickerIsEmpty() {
        //SETUP
        final EtfHolding e = mock(EtfHolding.class);
        when(e.getTicker()).thenReturn("");
        when(e.getType()).thenReturn(HoldingType.US_ETF);
        when(e.getValue()).thenReturn(BigDecimal.ONE);
        when(e.getHoldingIdentifier()).thenReturn(HoldingIdentifierType.TICKER);

        final List<Holding> holdings = List.of(e);
        final var sut = new HoldingReqValidation(holdings);

        final ReqValidationException expected = throwException(e, "Holding ticker could not be empty");

        //ACT
        final ReqValidationException actual = assertThrows(ReqValidationException.class, () -> sut.check());

        //VERIFY
        assertEquals(expected, actual);
        assertEquals(expected.getMessage(), actual.getMessage());
    }

    @Test
    void validateHoldings_StockHolding_tickerIsEmpty() {
        //SETUP
        final StockHolding e = mock(StockHolding.class);
        when(e.getTicker()).thenReturn("");
        when(e.getExchangeCode()).thenReturn("F");
        when(e.getType()).thenReturn(HoldingType.US_ETF);
        when(e.getValue()).thenReturn(BigDecimal.ONE);
        when(e.getHoldingIdentifier()).thenReturn(null);

        final List<Holding> holdings = List.of(e);
        final var sut = new HoldingReqValidation(holdings);

        final ReqValidationException expected = throwException(e, "Stock ticker could not be empty");

        //ACT
        final ReqValidationException actual = assertThrows(ReqValidationException.class, () -> sut.check());

        //VERIFY
        assertEquals(expected, actual);
        assertEquals(expected.getMessage(), actual.getMessage());
    }

    @Test
    void validateHoldings_StockHolding_getExchangeCodeIsEmpty() {
        //SETUP
        final StockHolding s = mock(StockHolding.class);
        when(s.getTicker()).thenReturn("FF");
        when(s.getExchangeCode()).thenReturn("");
        when(s.getType()).thenReturn(HoldingType.US_ETF);
        when(s.getValue()).thenReturn(BigDecimal.ONE);
        when(s.getHoldingIdentifier()).thenReturn(HoldingIdentifierType.TICKER);

        final List<Holding> holdings = List.of(s);
        final var sut = new HoldingReqValidation(holdings);

        final ReqValidationException expected = throwException(s, "Stock exchange code could not be null");

        //ACT
        final ReqValidationException actual = assertThrows(ReqValidationException.class, () -> sut.check());

        //VERIFY
        assertEquals(expected, actual);
        assertEquals(expected.getMessage(), actual.getMessage());
    }

    @Test
    void testValidateGicHoldingForValidData() {
        HoldingReqValidation target = new HoldingReqValidation(List.of());
        GicHolding holding = mock(GicHolding.class);
        when(holding.getInvestmentDate()).thenReturn(LocalDate.now());
        when(holding.getType()).thenReturn(HoldingType.GIC);

        Method validateGicHolding = ReflectionUtils.findMethod(HoldingReqValidation.class, "validateGicHolding", Holding.class);
        ReflectionUtils.makeAccessible(validateGicHolding);
        assertDoesNotThrow(() -> validateGicHolding.invoke(target, holding));
    }

    @Test
    void testValidateGicHoldingForInvalidData() {
        HoldingReqValidation target = new HoldingReqValidation(List.of());
        GicHolding holding = mock(GicHolding.class);
        when(holding.getInvestmentDate()).thenReturn(LocalDate.of(1523, 6, 1));
        when(holding.getType()).thenReturn(HoldingType.GIC);

        Method validateGicHolding = ReflectionUtils.findMethod(HoldingReqValidation.class, "validateGicHolding", Holding.class);
        ReflectionUtils.makeAccessible(validateGicHolding);
        Exception exception = assertThrows(Exception.class, () -> validateGicHolding.invoke(target, holding));
        assertEquals(ReqValidationException.class, exception.getCause().getClass());
    }
}
