package com.fintex.ce.repository.graphql.query.endpoint;

import com.fintex.ce.config.enumeration.Country;
import com.fintex.ce.config.enumeration.HoldingType;
import com.fintex.ce.dto.holding.StockHolding;
import com.fintex.ce.exception.SystemException;
import com.fintex.ce.model.redis.RBusinessCountry;
import com.fintex.smclient.graphql.DataProvider;
import com.fintex.smclient.graphql.EquityIdentifiers;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.QueryQuery;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.smclient.graphql.Stock;
import com.fintex.smclient.graphql.StockQuery;
import com.fintex.smclient.graphql.StringDatapoint;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.STRING_WITH_DATA_PROVIDER_DEFINITION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BusinessCountryEndpointTest {

    @Test
    void getGetStocksByTickersAndExchangeIds_isPresent() {
        //SETUP
        final BusinessCountryEndpoint m = new BusinessCountryEndpoint();

        final Query q = mock(Query.class);
        final ArrayList<Stock> expected = new ArrayList<>();

        when(q.getGetStocksByTickersAndExchangeIds()).thenReturn(expected);

        //ACT
        final Function<Query, List<Stock>> actual = m.getGetSMEntityFunction();

        //VERIFY
        Assertions.assertSame(actual.apply(q), expected);
    }

    @Test
    void queryDefinition_verify() {
        //SETUP
        final BusinessCountryEndpoint m = mock(BusinessCountryEndpoint.class);

        final QueryQuery qq = mock(QueryQuery.class);

        final EquityIdentifiers identifiers = mock(EquityIdentifiers.class);
        final List<EquityIdentifiers> equityIdentifiers = List.of(identifiers);

        doCallRealMethod().when(m).queryDefinition(any(), any());
        //ACT
        final QueryQueryDefinition actual = m.queryDefinition(equityIdentifiers, mock(UnaryOperator.class));
        actual.define(qq);

        //VERIFY
        verify(qq).getStocksByTickersAndExchangeIds(eq(equityIdentifiers), any());
    }

    @Test
    void requestMapper_verify() {
        //SETUP
        final BusinessCountryEndpoint m = mock(BusinessCountryEndpoint.class);

        final StockQuery etfQuery = mock(StockQuery.class);
        when(etfQuery.businessCountry(any())).thenReturn(etfQuery);
        when(etfQuery.externalIdentifiers(any())).thenReturn(etfQuery);

        doCallRealMethod().when(m).requestMapper(any());
        //ACT
        final StockQuery actual = m.requestMapper(etfQuery);

        //VERIFY
        verify(actual).businessCountry(STRING_WITH_DATA_PROVIDER_DEFINITION);
        verify(actual).externalIdentifiers(any());
    }

    @Test
    void responseMapper_checkResult() {
        //SETUP
        final BusinessCountryEndpoint m = mock(BusinessCountryEndpoint.class);

        final Stock stock = mock(Stock.class);

        final StockHolding holding = mock(StockHolding.class);
        final HoldingType cash = HoldingType.CANADA_STOCKS;
        when(holding.getType()).thenReturn(cash);

        doCallRealMethod().when(m).responseMapper(any(), any());
        //ACT
        m.responseMapper(stock, holding);

        //VERIFY
        verify(m).responseMapper(stock, holding);
    }

    @Test
    void responseMapper_checkResult2() {
        //SETUP
        final BusinessCountryEndpoint m = mock(BusinessCountryEndpoint.class);

        final Stock stock = mock(Stock.class);
        final StringDatapoint businessCountry = mock(StringDatapoint.class);
        when(stock.getBusinessCountry()).thenReturn(businessCountry);

        final StockHolding holding = mock(StockHolding.class);
        final HoldingType cash = HoldingType.CANADA_STOCKS;
        when(holding.getType()).thenReturn(cash);

        doCallRealMethod().when(m).responseMapper(any(), any());
        //ACT
        assertThrows(SystemException.class, () -> m.responseMapper(stock, holding));

        //VERIFY
    }

    @Test
    void responseMapper_checkResult3() {
        //SETUP
        final BusinessCountryEndpoint m = mock(BusinessCountryEndpoint.class);

        final Stock stock = mock(Stock.class);
        final StringDatapoint businessCountry = mock(StringDatapoint.class);
        String country = Country.CAN.name();
        RBusinessCountry expected = new RBusinessCountry(country);
        expected.setProvider(com.fintex.ce.config.enumeration.DataProvider.EAGLE.name());
        final StockHolding holding = mock(StockHolding.class);
        final HoldingType cash = HoldingType.CANADA_STOCKS;

        when(businessCountry.getDataProvider()).thenReturn(DataProvider.EAGLE);
        when(stock.getBusinessCountry()).thenReturn(businessCountry);
        when(businessCountry.getValue()).thenReturn(country);
        when(holding.getType()).thenReturn(cash);

        doCallRealMethod().when(m).responseMapper(any(), any());
        //ACT
        RBusinessCountry actual = m.responseMapper(stock, holding);

        //VERIFY
        assertEquals(expected, actual);
    }

}