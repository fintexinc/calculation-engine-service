package com.fintex.ce.repository.graphql.query.endpoint;

import com.fintex.ce.config.enumeration.HoldingType;
import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.model.redis.RSalesCharge;
import com.fintex.smclient.graphql.FundSeries;
import com.fintex.smclient.graphql.FundSeriesQuery;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.SalesCharge;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static com.fintex.ce.config.enumeration.ExceptionCode.ERR_SC_SC_001;
import static com.fintex.smclient.graphql.SalesChargeType.DEFERRED_SALES_CHARGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SalesChargeEndpointTest {

    @Test
    void getGetStocksByTickersAndExchangeIds_isPresent() {
        //SETUP
        final SalesChargeEndpoint sut = new SalesChargeEndpoint();

        final Query q = mock(Query.class);
        final ArrayList<FundSeries> expected = new ArrayList<>();

        when(q.getGetFundSeriesByHoldingCodes()).thenReturn(expected);

        //ACT
        final Function<Query, List<FundSeries>> actual = sut.getGetFDSEntityFunction();

        //VERIFY
        Assertions.assertSame(actual.apply(q), expected);
    }

    @Test
    void requestMapper_verify() {
        //SETUP
        final SalesChargeEndpoint sut = mock(SalesChargeEndpoint.class);

        final FundSeriesQuery fundSeriesQuery = mock(FundSeriesQuery.class);
        when(fundSeriesQuery.salesCharge(any())).thenReturn(fundSeriesQuery);
        when(fundSeriesQuery.externalIdentifiers(any())).thenReturn(fundSeriesQuery);

        doCallRealMethod().when(sut).requestMapper(any());

        //ACT
        final FundSeriesQuery actual = sut.requestMapper(fundSeriesQuery);

        //VERIFY
        verify(actual).salesCharge(any());
        verify(actual).externalIdentifiers(any());
    }

    @Test
    void responseMapper_checkResult() {
        //SETUP
        final SalesChargeEndpoint sut = mock(SalesChargeEndpoint.class);

        final FundSeries fundSeries = mock(FundSeries.class);
        final SalesCharge salesCharge = mock(SalesCharge.class);
        when(fundSeries.getSalesCharge()).thenReturn(salesCharge);

        final FundSeriesHolding holding = mock(FundSeriesHolding.class);
        final HoldingType type = HoldingType.CANADA_MUTUAL_FUNDS;
        when(holding.getType()).thenReturn(type);

        doCallRealMethod().when(sut).responseMapper(any(), any());
        //ACT
        var actual = sut.responseMapper(fundSeries, holding);

        //VERIFY
        assertTrue(actual.getErrors().stream().anyMatch(e -> e.getCode().equals(ERR_SC_SC_001)));
    }

    @Test
    void responseMapper_checkResult2() {

        //SETUP
        final SalesChargeEndpoint sut = mock(SalesChargeEndpoint.class);

        final FundSeries fundSeries = mock(FundSeries.class);
        final SalesCharge salesCharge = mock(SalesCharge.class);
        when(fundSeries.getSalesCharge()).thenReturn(salesCharge);
        when(salesCharge.getType()).thenReturn(DEFERRED_SALES_CHARGE);

        final FundSeriesHolding holding = mock(FundSeriesHolding.class);
        final HoldingType type = HoldingType.CANADA_MUTUAL_FUNDS;
        when(holding.getType()).thenReturn(type);

        doCallRealMethod().when(sut).responseMapper(any(), any());
        //ACT
        final RSalesCharge actual = sut.responseMapper(fundSeries, holding);

        //VERIFY
        assertEquals(DEFERRED_SALES_CHARGE.name(), actual.getValue());
    }

}
