package com.fintex.ce.repository.graphql.query;

import com.fintex.smclient.service.GraphqlTransportComponent;
import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.holding.CanadaHedgeFundHolding;
import com.fintex.ce.dto.holding.CanadaPooledFundHolding;
import com.fintex.ce.dto.holding.EtfHolding;
import com.fintex.ce.dto.holding.FixedIncomeHolding;
import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.dto.holding.SmaHolding;
import com.fintex.ce.dto.holding.StockHolding;
import com.fintex.ce.dto.holding.UsMutualFundHolding;
import com.fintex.ce.model.redis.RYield;
import com.fintex.ce.repository.graphql.query.core.MultipleSMAbstractRepository;
import com.fintex.ce.repository.graphql.query.endpoint.yield.YieldCanadaHedgeFundEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.yield.YieldEtfCanadaEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.yield.YieldEtfUsEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.yield.YieldFixedIncomeEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.yield.YieldFundCanadaEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.yield.YieldPooledFundEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.yield.YieldSeparatelyManagedAccountEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.yield.YieldStockEndpoint;
import com.fintex.ce.repository.graphql.query.endpoint.yield.YieldUsMutualFundEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class YieldSMRepository extends MultipleSMAbstractRepository<RYield, RYield, RYield, RYield> {

	@Autowired
	public YieldSMRepository(GraphqlTransportComponent graphqlTransport) {
		super(graphqlTransport);
	}

	@Override
	public Map<FundSeriesHolding, RYield> queryBenchOfFundCanada(final List<FundSeriesHolding> holdings,
																 final List<DataProvider> providers) {
		return doQuery(holdings, new YieldFundCanadaEndpoint(), providers);
	}

	@Override
	public Map<EtfHolding, RYield> queryBenchOfEtfCanada(final List<EtfHolding> holdings,
														 final List<DataProvider> providers) {
		return doQuery(holdings, new YieldEtfCanadaEndpoint(), providers);
	}

	@Override
	public Map<EtfHolding, RYield> queryBenchOfOfEtfUs(final List<EtfHolding> holdings,
													   final List<DataProvider> providers) {
		return doQuery(holdings, new YieldEtfUsEndpoint(), providers);
	}

	@Override
	public Map<CanadaPooledFundHolding, RYield> queryCanadaPooledFunds(final List<CanadaPooledFundHolding> holdings,
																	   final List<DataProvider> providers) {
		return doQuery(holdings, new YieldPooledFundEndpoint(), providers);
	}

	@Override
	public Map<UsMutualFundHolding, RYield> queryUsMutualFunds(final List<UsMutualFundHolding> holdings,
															   final List<DataProvider> providers) {
		return doQuery(holdings, new YieldUsMutualFundEndpoint(), providers);
	}

	@Override
	public Map<CanadaHedgeFundHolding, RYield> queryCanadaHedgeFunds(final List<CanadaHedgeFundHolding> holdings,
																	 final List<DataProvider> providers) {
		return doQuery(holdings, new YieldCanadaHedgeFundEndpoint(), providers);
	}

	@Override
	public Map<StockHolding, RYield> queryBenchOfStock(final List<StockHolding> holdings,
													   final List<DataProvider> providers) {
		return doQuery(holdings, new YieldStockEndpoint(), providers);
	}

	@Override
	public Map<FixedIncomeHolding, RYield> queryBenchOfFixedIncomes(final List<FixedIncomeHolding> holdings,
																	final List<DataProvider> providers) {
		return doQuery(holdings, new YieldFixedIncomeEndpoint(), providers);
	}

	@Override
	public Map<SmaHolding, RYield> queryBenchOfSeparatelyManagedAccounts(final List<SmaHolding> holdings,
																		 final List<DataProvider> providers) {
		return doQuery(holdings, new YieldSeparatelyManagedAccountEndpoint(), providers);
	}

}
