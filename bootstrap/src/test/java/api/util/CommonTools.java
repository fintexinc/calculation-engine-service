package api.util;

import com.fintex.smclient.config.properties.GraphqlTransportProperties;
import com.fintex.smclient.service.GraphqlQueryExecutor;
import com.fintex.smclient.service.GraphqlTransportComponent;
import com.fintex.smclient.service.impl.CommonEndpointsComponentImpl;
import com.fintex.smclient.service.impl.GraphqlQueryExecutorImpl;
import com.fintex.smclient.service.impl.GraphqlTransportComponentImpl;
import com.fintex.smclient.service.impl.endpoint.FxRatesEndpointComponent;
import com.fintex.smclient.service.impl.endpoint.TreasuryBillEndpointComponent;
import org.springframework.web.client.RestTemplate;

public class CommonTools {

  public static final RestTemplate REST_TEMPLATE = initRestTemplate();
  public static final GraphqlTransportComponent GRAPHQL_TRANSPORT_COMPONENT = initGraphqlTransportComponent();

  public static final CommonEndpointsComponentImpl CURRENCY_TRADING_PROVIDER = initCurrencyTradingProvider();

  private CommonTools() {
  }

  private static GraphqlTransportComponent initGraphqlTransportComponent() {
    final GraphqlTransportProperties properties = new GraphqlTransportProperties();
    properties.setLocation(TestProperties.getFdsUrl());
    final GraphqlQueryExecutor graphqlQueryExecutor = new GraphqlQueryExecutorImpl(REST_TEMPLATE, properties);
    return new GraphqlTransportComponentImpl(graphqlQueryExecutor);
  }

  private static CommonEndpointsComponentImpl initCurrencyTradingProvider() {
    final FxRatesEndpointComponent fxRatesEndpointComponent = new FxRatesEndpointComponent(GRAPHQL_TRANSPORT_COMPONENT);
    final TreasuryBillEndpointComponent treasuryBillEndpointComponent = new TreasuryBillEndpointComponent(
        GRAPHQL_TRANSPORT_COMPONENT);
    return new CommonEndpointsComponentImpl(fxRatesEndpointComponent, treasuryBillEndpointComponent);
  }

  private static RestTemplate initRestTemplate() {
    return new RestTemplate();
  }

}
