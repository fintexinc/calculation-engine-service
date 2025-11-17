package com.fintex.ce.repository.graphql.query.endpoint.commonholdings;

import com.fintex.smclient.graphql.ExternalIdentifierTypeValue;
import com.fintex.smclient.graphql.ExternalIdentifiers;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.Stock;
import com.fintex.smclient.graphql.StockQuery;
import com.fintex.smclient.graphql.StringDatapoint;
import com.fintex.ce.dto.holding.StockHolding;
import com.fintex.ce.model.redis.topcommonholdings.RCommonHoldingsStock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static com.fintex.smclient.graphql.ExternalIdentifierType.EXCHANGE_ID;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.STRING_DATAPOINT_QUERY_DEFINITION;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CommonHoldingsStockEndpointTest {

    @Test
    void getGetUsFundsByTickers_isPresent() {
        //SETUP
        final CommonHoldingsStockEndpoint sut = new CommonHoldingsStockEndpoint();

        final Query q = mock(Query.class);
        final ArrayList<Stock> expected = new ArrayList<>();

        when(q.getGetStocksByTickersAndExchangeIds()).thenReturn(expected);

        //ACT
        final Function<Query, List<Stock>> actual = sut.getGetFDSEntityFunction();

        //VERIFY
        Assertions.assertSame(actual.apply(q), expected);
    }

    @Test
    void requestMapper_verify() {
        //SETUP
        final CommonHoldingsStockEndpoint sut = Mockito.mock(CommonHoldingsStockEndpoint.class);

        final StockQuery stockQuery = mock(StockQuery.class);
        when(stockQuery.companyName(any())).thenReturn(stockQuery);
        when(stockQuery.externalIdentifiers(any())).thenReturn(stockQuery);

        doCallRealMethod().when(sut).requestMapper(any());

        //ACT
        final StockQuery actual = sut.requestMapper(stockQuery);

        //VERIFY
        verify(actual).companyName(STRING_DATAPOINT_QUERY_DEFINITION);
        verify(actual).externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    }

    @Test
    void responseMapper_verify() {
        //SETUP
        final CommonHoldingsStockEndpoint sut = Mockito.mock(CommonHoldingsStockEndpoint.class);
        final String name = "Fintex";

        final StockHolding holding = mock(StockHolding.class);
        final StringDatapoint companyName = mock(StringDatapoint.class);
        final ExternalIdentifiers externalIdentifiers = mock(ExternalIdentifiers.class);
        final ExternalIdentifierTypeValue externalIdentifierTypeValue = mock(ExternalIdentifierTypeValue.class);

        final Stock entity = mock(Stock.class);
        when(entity.getCompanyName()).thenReturn(companyName);
        when(companyName.getValue()).thenReturn(name);
        when(entity.getExternalIdentifiers()).thenReturn(externalIdentifiers);
        when(externalIdentifierTypeValue.getType()).thenReturn(EXCHANGE_ID);
        when(externalIdentifierTypeValue.getValue()).thenReturn("EXCHANGE_ID");
        when(externalIdentifiers.getCodes()).thenReturn(List.of(externalIdentifierTypeValue));

        doCallRealMethod().when(sut).responseMapper(any(), any());

        //ACT
        final RCommonHoldingsStock result = sut.responseMapper(entity, holding);

        //VERIFY
        Assertions.assertNotNull(result);
        Assertions.assertEquals(name, result.getCompanyName());
    }

    @Test
    void responseMapper_verifyNullFields() {
        //SETUP
        final CommonHoldingsStockEndpoint sut = Mockito.mock(CommonHoldingsStockEndpoint.class);

        final StockHolding holding = mock(StockHolding.class);
        final StringDatapoint companyName = mock(StringDatapoint.class);
        final ExternalIdentifiers externalIdentifiers = mock(ExternalIdentifiers.class);
        final ExternalIdentifierTypeValue externalIdentifierTypeValue = mock(ExternalIdentifierTypeValue.class);

        final Stock entity = mock(Stock.class);
        when(entity.getCompanyName()).thenReturn(null);
        when(companyName.getValue()).thenReturn(null);
        when(entity.getExternalIdentifiers()).thenReturn(externalIdentifiers);
        when(externalIdentifierTypeValue.getType()).thenReturn(null);
        when(externalIdentifierTypeValue.getValue()).thenReturn(null);
        when(externalIdentifiers.getCodes()).thenReturn(List.of());

        doCallRealMethod().when(sut).responseMapper(any(), any());

        //ACT
        final RCommonHoldingsStock result = sut.responseMapper(entity, holding);

        //VERIFY
        Assertions.assertNotNull(result);
        Assertions.assertNull(result.getCompanyName());
    }

}
