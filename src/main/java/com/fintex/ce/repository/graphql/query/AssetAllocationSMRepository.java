package com.fintex.ce.repository.graphql.query;

import com.fintex.smclient.service.GraphqlTransportComponent;
import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.holding.BenchmarkIndexHolding;
import com.fintex.ce.dto.holding.CanadaHedgeFundHolding;
import com.fintex.ce.dto.holding.CanadaPooledFundHolding;
import com.fintex.ce.dto.holding.EtfHolding;
import com.fintex.ce.dto.holding.FixedIncomeHolding;
import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.dto.holding.SmaHolding;
import com.fintex.ce.dto.holding.UsMutualFundHolding;
import com.fintex.ce.model.redis.RAssetAllocation;
import com.fintex.ce.repository.graphql.query.core.MultipleSMAbstractRepository;
import com.fintex.ce.repository.graphql.query.endpoint.assetallocation.AssetAllocationBenchmarkEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.assetallocation.AssetAllocationCanadaHedgeFundEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.assetallocation.AssetAllocationCanadaPooledFundEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.assetallocation.AssetAllocationEtfCanadaEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.assetallocation.AssetAllocationEtfUsEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.assetallocation.AssetAllocationFixedIncomeEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.assetallocation.AssetAllocationFundCanadaEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.assetallocation.AssetAllocationSeparatelyManagedAccountEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.assetallocation.AssetAllocationUsFundEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AssetAllocationSMRepository
        extends MultipleSMAbstractRepository<RAssetAllocation, RAssetAllocation, RAssetAllocation, RAssetAllocation> {

    @Autowired
    public AssetAllocationSMRepository(GraphqlTransportComponent graphqlTransport) {
        super(graphqlTransport);
    }

    @Override
    public Map<FundSeriesHolding, RAssetAllocation> queryBenchOfFundCanada(final List<FundSeriesHolding> holdings,
                                                                           final List<DataProvider> providers) {
        return doQuery(holdings, new AssetAllocationFundCanadaEndpoint(), providers);
    }

    @Override
    public Map<EtfHolding, RAssetAllocation> queryBenchOfOfEtfUs(final List<EtfHolding> holdings,
                                                                 final List<DataProvider> providers) {
        return doQuery(holdings, new AssetAllocationEtfUsEndpoint(), providers);
    }

    @Override
    public Map<EtfHolding, RAssetAllocation> queryBenchOfEtfCanada(final List<EtfHolding> holdings,
                                                                   final List<DataProvider> providers) {
        return doQuery(holdings, new AssetAllocationEtfCanadaEndpoint(), providers);
    }

    @Override
    public Map<BenchmarkIndexHolding, RAssetAllocation> queryBenchOfBenchmarks(final List<BenchmarkIndexHolding> holdings,
                                                                               final List<DataProvider> providers) {
        return doQuery(holdings, new AssetAllocationBenchmarkEndpoint(), providers);
    }

    @Override
    public Map<CanadaPooledFundHolding, RAssetAllocation> queryCanadaPooledFunds(final List<CanadaPooledFundHolding> holdings,
                                                                                 final List<DataProvider> providers) {
        return doQuery(holdings, new AssetAllocationCanadaPooledFundEndpoint(), providers);
    }

    @Override
    public Map<CanadaHedgeFundHolding, RAssetAllocation> queryCanadaHedgeFunds(final List<CanadaHedgeFundHolding> holdings,
                                                                               final List<DataProvider> providers) {
        return doQuery(holdings, new AssetAllocationCanadaHedgeFundEndpoint(), providers);
    }

    @Override
    public Map<UsMutualFundHolding, RAssetAllocation> queryUsMutualFunds(final List<UsMutualFundHolding> holdings,
                                                                         final List<DataProvider> providers) {
        return doQuery(holdings, new AssetAllocationUsFundEndpoint(), providers);
    }

    @Override
    public Map<FixedIncomeHolding, RAssetAllocation> queryBenchOfFixedIncomes(final List<FixedIncomeHolding> holdings,
                                                                              final List<DataProvider> providers) {
        return doQuery(holdings, new AssetAllocationFixedIncomeEndpoint(), providers);
    }

    @Override
    public Map<SmaHolding, RAssetAllocation> queryBenchOfSeparatelyManagedAccounts(List<SmaHolding> holdings, List<DataProvider> providers) {
        return doQuery(holdings, new AssetAllocationSeparatelyManagedAccountEndpoint(), providers);
    }
}
