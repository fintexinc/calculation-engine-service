package com.fintex.ce.repository.graphql.query.endpoint.yield;

import com.fintex.smclient.graphql.FloatDatapoint;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.SeparatelyManagedAccount;
import com.fintex.smclient.graphql.SeparatelyManagedAccountQuery;
import com.fintex.ce.dto.holding.SmaHolding;
import com.fintex.ce.model.redis.RYield;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.config.constant.graphql.GraphQlResolverConstants.FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class YieldSeparatelyManagedAccountEndpointTest {

	@Test
	public void getGetSeparatelyManagedAccountsBys_isPresent() {
		//SETUP
		final YieldSeparatelyManagedAccountEndpoint sut = new YieldSeparatelyManagedAccountEndpoint();

		final Query q = mock(Query.class);
		final ArrayList<SeparatelyManagedAccount> expected = new ArrayList<>();

		when(q.getGetSeparatelyManagedAccountsBy()).thenReturn(expected);

		//ACT
		final Function<Query, List<SeparatelyManagedAccount>> actual = sut.getGetFDSEntityFunction();

		//VERIFY
		Assertions.assertSame(actual.apply(q), expected);
	}

	@Test
	public void requestMapper_verify() {
		//SETUP
		final YieldSeparatelyManagedAccountEndpoint sut = Mockito.mock(YieldSeparatelyManagedAccountEndpoint.class);

		final SeparatelyManagedAccountQuery smaQuery = mock(SeparatelyManagedAccountQuery.class);
		when(smaQuery.dividendYield(any())).thenReturn(smaQuery);
		when(smaQuery.externalIdentifiers(any())).thenReturn(smaQuery);

		doCallRealMethod().when(sut).requestMapper(any());

		//ACT
		final SeparatelyManagedAccountQuery actual = sut.requestMapper(smaQuery);

		//VERIFY
		verify(actual).dividendYield(FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION);
		verify(actual).externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
	}

	@Test
	public void responseMapper_verify() {
		//SETUP
		final YieldSeparatelyManagedAccountEndpoint sut = Mockito.mock(YieldSeparatelyManagedAccountEndpoint.class);

		final SmaHolding holding = mock(SmaHolding.class);
		final FloatDatapoint dividendYield = mock(FloatDatapoint.class);
		final BigDecimal yieldValue = mock(BigDecimal.class);

		final SeparatelyManagedAccount entity = mock(SeparatelyManagedAccount.class);
		when(entity.getDividendYield()).thenReturn(dividendYield);
		when(dividendYield.getValue()).thenReturn(yieldValue);

		doCallRealMethod().when(sut).responseMapper(any(), any());

		//ACT
		final RYield result = sut.responseMapper(entity, holding);

		//VERIFY
		Assertions.assertNotNull(result);
		Assertions.assertEquals(yieldValue, result.getDividendYield());
	}

}
