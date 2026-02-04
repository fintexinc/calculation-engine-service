package com.fintex.ce.adapter.graphqlclient.repository;

import com.fintex.smclient.service.GraphqlTransportComponent;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.holding.CanadaHedgeFundHolding;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.holding.UsMutualFundHolding;
import com.fintex.ce.domain.model.ManagementFee;
import com.fintex.ce.adapter.graphqlclient.repository.core.MultipleSMAbstractRepository;
import com.fintex.ce.adapter.graphqlclient.endpoint.managementfee.ManagementFeeCanadaHedgeFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.managementfee.ManagementFeeEtfCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.managementfee.ManagementFeeEtfUsEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.managementfee.ManagementFeeFundCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.managementfee.ManagementFeeUsMutualFundEndpoint;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Log4j2
public class ManagementFeeSMRepository
    extends
      MultipleSMAbstractRepository<ManagementFee, ManagementFee, ManagementFee, ManagementFee> {

  public ManagementFeeSMRepository(GraphqlTransportComponent graphqlTransport) {
    super(graphqlTransport);
  }

  @Override
  public Map<FundSeriesHolding, ManagementFee> queryBenchOfFundCanada(final List<FundSeriesHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new ManagementFeeFundCanadaEndpoint(), providers);
  }

  @Override
  public Map<EtfHolding, ManagementFee> queryBenchOfEtfCanada(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new ManagementFeeEtfCanadaEndpoint(), providers);
  }

  @Override
  public Map<EtfHolding, ManagementFee> queryBenchOfOfEtfUs(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new ManagementFeeEtfUsEndpoint(), providers);
  }

  @Override
  public Map<UsMutualFundHolding, ManagementFee> queryUsMutualFunds(final List<UsMutualFundHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new ManagementFeeUsMutualFundEndpoint(), providers);
  }

  @Override
  public Map<CanadaHedgeFundHolding, ManagementFee> queryCanadaHedgeFunds(final List<CanadaHedgeFundHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new ManagementFeeCanadaHedgeFundEndpoint(), providers);
  }
}
