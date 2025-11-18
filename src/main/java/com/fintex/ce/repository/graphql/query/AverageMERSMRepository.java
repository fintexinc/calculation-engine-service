package com.fintex.ce.repository.graphql.query;

import com.fintex.smclient.service.GraphqlTransportComponent;
import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.holding.CanadaHedgeFundHolding;
import com.fintex.ce.dto.holding.EtfHolding;
import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.dto.holding.UsMutualFundHolding;
import com.fintex.ce.model.redis.averagemer.RAverageMer;
import com.fintex.ce.model.redis.core.RedisId;
import com.fintex.ce.repository.graphql.query.core.MultipleSMAbstractRepository;
import com.fintex.ce.repository.graphql.query.endpoint.averagemer.AverageMERCanadaHedgeFundEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.averagemer.AverageMEREtfCanadaEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.averagemer.AverageMEREtfUsEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.averagemer.AverageMERFundCanadaEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.averagemer.AverageMERUsMutualFundEndpoint;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Log4j2
public class AverageMERSMRepository extends MultipleSMAbstractRepository<RAverageMer, RAverageMer, RAverageMer, RedisId> {

    public AverageMERSMRepository(GraphqlTransportComponent graphqlTransport) {
        super(graphqlTransport);
    }

    @Override
    public Map<FundSeriesHolding, RAverageMer> queryBenchOfFundCanada(final List<FundSeriesHolding> holdings,
                                                                      final List<DataProvider> providers) {
        return doQuery(holdings, new AverageMERFundCanadaEndpoint(), providers);
    }

    @Override
    public Map<EtfHolding, RAverageMer> queryBenchOfEtfCanada(final List<EtfHolding> holdings,
                                                              final List<DataProvider> providers) {
        return doQuery(holdings, new AverageMEREtfCanadaEndpoint(), providers);
    }

    @Override
    public Map<EtfHolding, RAverageMer> queryBenchOfOfEtfUs(final List<EtfHolding> holdings,
                                                            final List<DataProvider> providers) {
        return doQuery(holdings, new AverageMEREtfUsEndpoint(), providers);
    }

    @Override
    public Map<CanadaHedgeFundHolding, RAverageMer> queryCanadaHedgeFunds(final List<CanadaHedgeFundHolding> holdings,
                                                                          final List<DataProvider> providers) {
        return doQuery(holdings, new AverageMERCanadaHedgeFundEndpoint(), providers);
    }

    @Override
    public Map<UsMutualFundHolding, RAverageMer> queryUsMutualFunds(final List<UsMutualFundHolding> holdings,
                                                                    final List<DataProvider> providers) {
        return doQuery(holdings, new AverageMERUsMutualFundEndpoint(), providers);
    }
}
