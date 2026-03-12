package com.fintex.ce.adapter.graphqlclient.repository;

import com.fintex.smclient.service.GraphqlTransportComponent;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.model.holding.CanadaHedgeFundHolding;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.holding.UsMutualFundHolding;
import com.fintex.ce.domain.model.ManagementFee;
import com.fintex.ce.adapter.graphqlclient.endpoint.core.AbstractSMEndpoint;
import com.fintex.ce.adapter.graphqlclient.repository.core.AbstractGraphqlDataFetcher;
import com.fintex.ce.adapter.graphqlclient.endpoint.managementfee.ManagementFeeCanadaHedgeFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.managementfee.ManagementFeeEtfCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.managementfee.ManagementFeeEtfUsEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.managementfee.ManagementFeeFundCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.managementfee.ManagementFeeUsMutualFundEndpoint;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Log4j2
@ConditionalOnProperty(name = "security-master.web-protocol", havingValue = "graphql", matchIfMissing = true)
public class ManagementFeeGraphqlDataFetcher
    extends AbstractGraphqlDataFetcher<ManagementFee> {

  public ManagementFeeGraphqlDataFetcher(GraphqlTransportComponent graphqlTransport) {
    super(graphqlTransport);
  }

  @Override
  protected AbstractSMEndpoint<?, ?, ?, ?, ManagementFee> resolveEndpoint(HoldingType type) {
    return switch (type) {
      case CANADA_HEDGE_FUNDS -> new ManagementFeeCanadaHedgeFundEndpoint();
      case CANADA_ETF -> new ManagementFeeEtfCanadaEndpoint();
      case US_ETF -> new ManagementFeeEtfUsEndpoint();
      case CANADA_MUTUAL_FUNDS, SEGREGATED_FUND_CANADA -> new ManagementFeeFundCanadaEndpoint();
      case US_MUTUAL_FUNDS -> new ManagementFeeUsMutualFundEndpoint();
      default -> null;
    };
  }


    public Map<FundSeriesHolding, ManagementFee> queryBenchOfFundCanada(final List<FundSeriesHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new ManagementFeeFundCanadaEndpoint(), providers);
  }

    public Map<EtfHolding, ManagementFee> queryBenchOfEtfCanada(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new ManagementFeeEtfCanadaEndpoint(), providers);
  }

    public Map<EtfHolding, ManagementFee> queryBenchOfOfEtfUs(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new ManagementFeeEtfUsEndpoint(), providers);
  }

    public Map<UsMutualFundHolding, ManagementFee> queryUsMutualFunds(final List<UsMutualFundHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new ManagementFeeUsMutualFundEndpoint(), providers);
  }

    public Map<CanadaHedgeFundHolding, ManagementFee> queryCanadaHedgeFunds(final List<CanadaHedgeFundHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new ManagementFeeCanadaHedgeFundEndpoint(), providers);
  }
}
