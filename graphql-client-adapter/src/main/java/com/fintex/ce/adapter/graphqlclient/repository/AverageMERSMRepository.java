package com.fintex.ce.adapter.graphqlclient.repository;

import com.fintex.smclient.service.GraphqlTransportComponent;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.holding.CanadaHedgeFundHolding;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.holding.UsMutualFundHolding;
import com.fintex.ce.domain.model.AverageMer;
import com.fintex.ce.adapter.graphqlclient.repository.core.MultipleSMAbstractRepository;
import com.fintex.ce.adapter.graphqlclient.endpoint.averagemer.AverageMERCanadaHedgeFundEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.averagemer.AverageMEREtfCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.averagemer.AverageMEREtfUsEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.averagemer.AverageMERFundCanadaEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.averagemer.AverageMERUsMutualFundEndpoint;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Log4j2
public class AverageMERSMRepository
    extends
      MultipleSMAbstractRepository<AverageMer, AverageMer, AverageMer, AverageMer> {

  public AverageMERSMRepository(GraphqlTransportComponent graphqlTransport) {
    super(graphqlTransport);
  }

  @Override
  public Map<FundSeriesHolding, AverageMer> queryBenchOfFundCanada(final List<FundSeriesHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new AverageMERFundCanadaEndpoint(), providers);
  }

  @Override
  public Map<EtfHolding, AverageMer> queryBenchOfEtfCanada(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new AverageMEREtfCanadaEndpoint(), providers);
  }

  @Override
  public Map<EtfHolding, AverageMer> queryBenchOfOfEtfUs(final List<EtfHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new AverageMEREtfUsEndpoint(), providers);
  }

  @Override
  public Map<CanadaHedgeFundHolding, AverageMer> queryCanadaHedgeFunds(final List<CanadaHedgeFundHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new AverageMERCanadaHedgeFundEndpoint(), providers);
  }

  @Override
  public Map<UsMutualFundHolding, AverageMer> queryUsMutualFunds(final List<UsMutualFundHolding> holdings,
      final List<DataProvider> providers) {
    return doQuery(holdings, new AverageMERUsMutualFundEndpoint(), providers);
  }
}
