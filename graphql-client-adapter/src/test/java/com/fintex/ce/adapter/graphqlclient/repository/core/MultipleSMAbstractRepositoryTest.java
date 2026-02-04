package com.fintex.ce.adapter.graphqlclient.repository.core;

// import com.fintex.smclient.graphql.QueryQuery;
// import com.fintex.smclient.service.GraphqlTransportComponent;
// import com.fintex.ce.dto.holding.Holding;
// import com.fintex.ce.repository.graphql.query.endpoint.core.AbstractFDSEndpoint;
// import org.junit.jupiter.api.Assertions;
// import org.junit.jupiter.api.Test;
//
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;
// import java.util.function.Function;
//
// import static org.mockito.Mockito.any;
// import static org.mockito.Mockito.doCallRealMethod;
// import static org.mockito.Mockito.eq;
// import static org.mockito.Mockito.mock;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;
// import static org.mockito.Mockito.withSettings;
//
// class MultipleFDSAbstractRepositoryTest {
//
// @Test
// void doQuery_verifyMakeQuery() {
// //SETUP
// final GraphqlTransportComponent g = mock(GraphqlTransportComponent.class);
// final MultipleFDSAbstractRepository m = mock(MultipleFDSAbstractRepository.class, withSettings().useConstructor(g));
//
// final AbstractFDSEndpoint endpoint = mock(AbstractFDSEndpoint.class);
// when(endpoint.setUserEnteredProviders(any())).thenReturn(endpoint);
//
// final List holdings = List.of(mock(Holding.class));
//
// doCallRealMethod().when(m).doQuery(any(), any(), any());
// //ACT
// m.doQuery(holdings, endpoint, List.of());
//
// //VERIFY
// verify(endpoint).makeQuery(holdings);
// verify(endpoint).getGetFDSEntityFunction();
// }
//
// @Test
// void doQuery_verifyQuery() {
// //SETUP
// final GraphqlTransportComponent g = mock(GraphqlTransportComponent.class);
// final MultipleFDSAbstractRepository m = mock(MultipleFDSAbstractRepository.class, withSettings().useConstructor(g));
//
// final AbstractFDSEndpoint endpoint = mock(AbstractFDSEndpoint.class);
//
// final String name = "TEST";
// when(endpoint.getEndpointName()).thenReturn(name);
//
// when(endpoint.setUserEnteredProviders(any())).thenReturn(endpoint);
// final QueryQuery qq = mock(QueryQuery.class);
// when(endpoint.makeQuery(any())).thenReturn(qq);
//
// final List holdings = List.of(mock(Holding.class));
//
//
// final Function func = mock(Function.class);
// when(endpoint.getGetFDSEntityFunction()).thenReturn(func);
//
// doCallRealMethod().when(m).doQuery(any(), any(), any());
// //ACT
// m.doQuery(holdings, endpoint, List.of());
//
// //VERIFY
// verify(g).query(qq, func);
// }
//
// @Test
// void doQuery_verifyCollectResultToMap() {
// //SETUP
// final GraphqlTransportComponent g = mock(GraphqlTransportComponent.class);
// final MultipleFDSAbstractRepository m = mock(MultipleFDSAbstractRepository.class, withSettings().useConstructor(g));
//
// final AbstractFDSEndpoint endpoint = mock(AbstractFDSEndpoint.class);
// when(endpoint.setUserEnteredProviders(any())).thenReturn(endpoint);
//
// final List holdings = List.of(mock(Holding.class));
//
// final List actual = List.of(mock(Object.class));
// when(g.query(any(QueryQuery.class), any())).thenReturn(actual);
//
// when(endpoint.makeQuery(any())).thenReturn(mock(QueryQuery.class));
//
// doCallRealMethod().when(m).doQuery(any(), any(), any());
// //ACT
// m.doQuery(holdings, endpoint, List.of());
//
// //VERIFY
// verify(endpoint).collectResultToMap(holdings, actual);
// }
//
// @Test
// void doQuery_checkResult() {
// //SETUP
// final GraphqlTransportComponent g = mock(GraphqlTransportComponent.class);
// final MultipleFDSAbstractRepository m = mock(MultipleFDSAbstractRepository.class, withSettings().useConstructor(g));
//
// final AbstractFDSEndpoint endpoint = mock(AbstractFDSEndpoint.class);
// when(endpoint.setUserEnteredProviders(any())).thenReturn(endpoint);
//
// final List holdings = List.of(mock(Holding.class));
//
// final HashMap expected = new HashMap();
// when(endpoint.collectResultToMap(any(), any())).thenReturn(expected);
//
// doCallRealMethod().when(m).doQuery(any(), any(), any());
// //ACT
// final Map actual = m.doQuery(holdings, endpoint, List.of());
//
// //VERIFY
// Assertions.assertSame(expected, actual);
// }
//
// }
