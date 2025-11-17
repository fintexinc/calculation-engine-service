package com.fintex.ce.util.validation.request;

import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.request.MultiplePortfoliosReqDTO;
import com.fintex.ce.util.validation.request.chainofresponsibility.HoldingReqValidation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class CommonDatesRequestValidatorTest {

    @Test
    void validate_verifyHoldingReqValidationCheck() {
        //SETUP
        final var sut = mock(CommonDatesRequestValidator.class);

        final List<Holding> benchmarkHoldings = List.of(mock(Holding.class));
        final List<Holding> portfolioHoldings = List.of();
        final var portfolio = new MultiplePortfoliosReqDTO.Portfolio(portfolioHoldings);

        final HoldingReqValidation reqValidation = mock(HoldingReqValidation.class);
        doReturn(reqValidation).when(sut).buildHoldingReqValidation(benchmarkHoldings);

        doCallRealMethod().when(sut).validate(anyList(), anySet());
        //ACT
        sut.validate(benchmarkHoldings, Set.of(portfolio));

        //VERIFY
        verify(reqValidation).check();
    }

    @Test
    void validate_verifyPortfolioPreValidationForBenchmarks() {
        //SETUP
        final var sut = mock(CommonDatesRequestValidator.class);
        final List<Holding> benchmarkHoldings = List.of();
        final List<Holding> portfolioHoldings = List.of(mock(Holding.class));
        final var portfolio = new MultiplePortfoliosReqDTO.Portfolio(portfolioHoldings);

        final HoldingReqValidation reqValidation = mock(HoldingReqValidation.class);
        doReturn(reqValidation).when(sut).buildHoldingReqValidation(portfolioHoldings);

        doCallRealMethod().when(sut).validate(anyList(), anySet());
        //ACT
        sut.validate(benchmarkHoldings, Set.of(portfolio));

        //VERIFY
        verify(reqValidation).check();
    }

    @Test
    void buildHoldingReqValidation_checkResult() {
        //SETUP
        final var sut = mock(CommonDatesRequestValidator.class);

        final List<Holding> portfolioHoldings = List.of(mock(Holding.class));
        final var expected = new HoldingReqValidation(portfolioHoldings);

        doCallRealMethod().when(sut).buildHoldingReqValidation(anyList());
        //ACT
        final HoldingReqValidation actual = sut.buildHoldingReqValidation(portfolioHoldings);

        //VERIFY
        assertEquals(expected, actual);
    }
}
