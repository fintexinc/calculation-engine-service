package com.fintex.ce.repository.graphql.query.endpoint.assetallocation;

import com.fintex.smclient.graphql.AssetAllocation;
import com.fintex.smclient.graphql.Etf;
import com.fintex.smclient.graphql.EtfQuery;
import com.fintex.smclient.graphql.Query;
import com.fintex.smclient.graphql.QueryQueryDefinition;
import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.holding.EtfHolding;
import com.fintex.ce.model.redis.RAssetAllocation;
import com.fintex.ce.repository.graphql.query.endpoint.core.EtfAbstractEndpoint;
import com.fintex.ce.util.graphql.GraphQlMapperUtils;

import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static com.fintex.ce.config.constant.graphql.GraphQlEndpointConstants.GET_CANADA_ETFS_BY_TICKERS;
import static com.fintex.ce.config.enumeration.cache.CacheCategory.CANADA_ETF;
import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.ASSET_ALLOCATION;
import static com.fintex.ce.util.CacheUtils.buildCacheName;

public class AssetAllocationEtfCanadaEndpoint extends EtfAbstractEndpoint<RAssetAllocation> {

    public AssetAllocationEtfCanadaEndpoint() {
        super(GET_CANADA_ETFS_BY_TICKERS, List.of(), buildCacheName(ASSET_ALLOCATION, CANADA_ETF));
    }

    public AssetAllocationEtfCanadaEndpoint(Function<Query, List<Etf>> getFDSEntityFunction,
                                            List<DataProvider> supportedProviders,
                                            String endpointName) {
        super(getFDSEntityFunction, supportedProviders, endpointName);
    }

    @Override
    public QueryQueryDefinition queryDefinition(final List<String> equityIdentifiers, final UnaryOperator<EtfQuery> preDefinedFDSQuery) {
        return q -> q.getCanadaEtfsByTickers(equityIdentifiers, preDefinedFDSQuery::apply);
    }

    @Override
    public EtfQuery requestMapper(EtfQuery query) {
        return query
                .assetAllocation(al -> al
                        .allocation(p -> p.name().value())
                        .dataProvider()
                )
                .ticker(t -> t.value().dataProvider());
    }

    @Override
    public RAssetAllocation responseMapper(Etf etf, EtfHolding holding) {
        final AssetAllocation assetAllocation = etf.getAssetAllocation();
        return GraphQlMapperUtils.assetAllocation(assetAllocation, holding.getType());
    }
}
