package com.fintex.ce.repository.graphql.query;

import com.fintex.smclient.service.GraphqlTransportComponent;
import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.holding.CanadaHedgeFundHolding;
import com.fintex.ce.dto.holding.EtfHolding;
import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.dto.holding.UsMutualFundHolding;
import com.fintex.ce.model.redis.core.RedisId;
import com.fintex.ce.model.redis.managementfee.RManagementFee;
import com.fintex.ce.repository.graphql.query.core.MultipleSMAbstractRepository;
import com.fintex.ce.repository.graphql.query.endpoint.managementfee.ManagementFeeCanadaHedgeFundEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.managementfee.ManagementFeeEtfCanadaEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.managementfee.ManagementFeeEtfUsEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.managementfee.ManagementFeeFundCanadaEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.managementfee.ManagementFeeUsMutualFundEndpoint;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Log4j2
public class ManagementFeeSMRepository extends MultipleSMAbstractRepository<RManagementFee, RManagementFee, RManagementFee, RedisId> {

    public ManagementFeeSMRepository(GraphqlTransportComponent graphqlTransport) {
        super(graphqlTransport);
    }

    @Override
    public Map<FundSeriesHolding, RManagementFee> queryBenchOfFundCanada(final List<FundSeriesHolding> holdings,
                                                                         final List<DataProvider> providers) {
        return doQuery(holdings, new ManagementFeeFundCanadaEndpoint(), providers);
    }

    @Override
    public Map<EtfHolding, RManagementFee> queryBenchOfEtfCanada(final List<EtfHolding> holdings,
                                                                 final List<DataProvider> providers) {
        return doQuery(holdings, new ManagementFeeEtfCanadaEndpoint(), providers);
    }

    @Override
    public Map<EtfHolding, RManagementFee> queryBenchOfOfEtfUs(final List<EtfHolding> holdings,
                                                                final List<DataProvider> providers) {
        return doQuery(holdings, new ManagementFeeEtfUsEndpoint(), providers);
    }

    @Override
    public Map<UsMutualFundHolding, RManagementFee> queryUsMutualFunds(final List<UsMutualFundHolding> holdings,
                                                                       final List<DataProvider> providers) {
        return doQuery(holdings, new ManagementFeeUsMutualFundEndpoint(), providers);
    }

    @Override
    public Map<CanadaHedgeFundHolding, RManagementFee> queryCanadaHedgeFunds(final List<CanadaHedgeFundHolding> holdings,
                                                                             final List<DataProvider> providers) {
        return doQuery(holdings, new ManagementFeeCanadaHedgeFundEndpoint(), providers);
    }
}
