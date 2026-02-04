package com.fintex.ce.adapter.graphqlclient.endpoint.incomeforecast;

import com.fintex.ce.adapter.graphqlclient.endpoint.incomeforecast.IncomeForecastEtfCanadaEndpoint;
import com.fintex.ce.domain.model.IncomeForecast;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.smclient.graphql.Etf;
import com.fintex.smclient.graphql.EtfQuery;
import com.fintex.smclient.graphql.FloatDatapoint;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.StringsDatapoint;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.EXTERNAL_IDENTIFIERS_QUERY_DEFINITION;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION;
import static com.fintex.ce.adapter.graphqlclient.config.constant.GraphQlResolverConstants.STRING_WITH_DATA_PROVIDER_DEFINITION;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IncomeForecastEtfCanadaEndpointTest {

  @Test
  void getGetCanadaEtfsByTickers_isPresent() {
    // SETUP
    final IncomeForecastEtfCanadaEndpoint sut = new IncomeForecastEtfCanadaEndpoint();

    final Query q = mock(Query.class);
    final ArrayList<Etf> expected = new ArrayList<>();

    when(q.getGetCanadaEtfsByTickers()).thenReturn(expected);

    // ACT
    final Function<Query, List<Etf>> actual = sut.getGetSMEntityFunction();

    // VERIFY
    Assertions.assertSame(actual.apply(q), expected);
  }

  @Test
  void requestMapper_verify() {
    // SETUP
    final IncomeForecastEtfCanadaEndpoint sut = Mockito.mock(IncomeForecastEtfCanadaEndpoint.class);

    final EtfQuery etfQuery = mock(EtfQuery.class);
    when(etfQuery.dividendYield(any())).thenReturn(etfQuery);
    when(etfQuery.distributionDates(any())).thenReturn(etfQuery);
    when(etfQuery.externalIdentifiers(any())).thenReturn(etfQuery);
    when(etfQuery.ticker(any())).thenReturn(etfQuery);

    doCallRealMethod().when(sut).requestMapper(any());

    // ACT
    final EtfQuery actual = sut.requestMapper(etfQuery);

    // VERIFY
    verify(actual).dividendYield(FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION);
    verify(actual).distributionDates(any());
    verify(actual).externalIdentifiers(EXTERNAL_IDENTIFIERS_QUERY_DEFINITION);
    verify(actual).ticker(STRING_WITH_DATA_PROVIDER_DEFINITION);
  }

  @Test
  void responseMapper_verify() {
    // SETUP
    final IncomeForecastEtfCanadaEndpoint sut = Mockito.mock(IncomeForecastEtfCanadaEndpoint.class);

    final EtfHolding holding = mock(EtfHolding.class);
    final FloatDatapoint dividendYield = mock(FloatDatapoint.class);
    final StringsDatapoint distributionDates = mock(StringsDatapoint.class);
    final BigDecimal yieldValue = mock(BigDecimal.class);
    final List<String> schedule = mock(List.class);

    final Etf entity = mock(Etf.class);
    when(entity.getDividendYield()).thenReturn(dividendYield);
    when(dividendYield.getValue()).thenReturn(yieldValue);
    when(entity.getDistributionDates()).thenReturn(distributionDates);
    when(distributionDates.getValues()).thenReturn(schedule);

    doCallRealMethod().when(sut).responseMapper(any(), any());

    // ACT
    final IncomeForecast result = sut.responseMapper(entity, holding);

    // VERIFY
    Assertions.assertNotNull(result);
    Assertions.assertEquals(yieldValue, result.getDividendYield());
    Assertions.assertEquals(schedule, result.getSchedule());
  }

}
