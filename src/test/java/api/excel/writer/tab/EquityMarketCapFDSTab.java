package api.excel.writer.tab;

import api.dto.RequestParamsSupplier;
import api.excel.writer.SMDataWriter;
import api.excel.writer.WritableSpreadsheet;
import api.util.CommonTools;
import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.model.redis.equitymarketcapitalization.REquityMarketCapitalization;
import com.fintex.ce.repository.graphql.query.EquityMarketCapitalizationFDSRepository;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.fintex.ce.util.FilterUtils.*;
import static java.util.stream.Collectors.toMap;

public class EquityMarketCapFDSTab extends SMDataWriter<Holding, String, BigDecimal> implements WritableSpreadsheet {
    private static final EquityMarketCapitalizationFDSRepository EQUITY_MARKET_CAP_FDS = initEquityMarketCapFDS();
    private static final String TAB_NAME = "EquityMarketCapital_FDS";

    private static EquityMarketCapitalizationFDSRepository initEquityMarketCapFDS() {
        return new EquityMarketCapitalizationFDSRepository(CommonTools.GRAPHQL_TRANSPORT_COMPONENT);
    }

    @Override
    public void write(List<Holding> holdings, RequestParamsSupplier params, Workbook workbook) {
        final Sheet sheet = Objects.requireNonNull(Objects.requireNonNull(workbook.getSheet(TAB_NAME)));

        final Map<Holding, REquityMarketCapitalization> assetAllocations = callFds(holdings, params.getDataProviders());

        final Map<Holding, Pair<DataProvider, Map<String, BigDecimal>>> rawData = assetAllocations.entrySet().stream().collect(
                toMap(
                        Map.Entry::getKey,
                        e -> Pair.of(DataProvider.of(e.getValue().getProvider()), e.getValue().getRatings())
                )
        );
        populateTab(rawData, sheet);
    }

    private Map<Holding, REquityMarketCapitalization> callFds(final List<Holding> holdings, final List<DataProvider> dataProviders) {
        final Map<Holding, REquityMarketCapitalization> map = new LinkedHashMap<>();
        if (!filterHoldings(holdings, US_ETF_PREDICATE).isEmpty()) {
            map.putAll(EQUITY_MARKET_CAP_FDS.queryBenchOfOfEtfUs(filterHoldings(holdings, US_ETF_PREDICATE), dataProviders));
        }
        if (!filterHoldings(holdings, CANADA_ETF_PREDICATE).isEmpty()) {
            map.putAll(EQUITY_MARKET_CAP_FDS.queryBenchOfEtfCanada(filterHoldings(holdings, CANADA_ETF_PREDICATE), dataProviders));
        }
        if (!filterHoldings(holdings, CANADA_MUTUAL_PREDICATE).isEmpty()) {
            map.putAll(EQUITY_MARKET_CAP_FDS.queryBenchOfFundCanada(filterHoldings(holdings, CANADA_MUTUAL_PREDICATE), dataProviders));
        }
        if (!filterHoldings(holdings, CANADA_POOLED_FUND_PREDICATE).isEmpty()) {
            map.putAll(EQUITY_MARKET_CAP_FDS.queryCanadaPooledFunds(filterHoldings(holdings, CANADA_POOLED_FUND_PREDICATE), dataProviders));
        }
        if (!filterHoldings(holdings, CANADA_HEDGE_FUND_PREDICATE).isEmpty()) {
            map.putAll(EQUITY_MARKET_CAP_FDS.queryCanadaHedgeFunds(filterHoldings(holdings, CANADA_HEDGE_FUND_PREDICATE), dataProviders));
        }
        if (!filterHoldings(holdings, US_MUTUAL_FUND_PREDICATE).isEmpty()) {
            map.putAll(EQUITY_MARKET_CAP_FDS.queryUsMutualFunds(filterHoldings(holdings, US_MUTUAL_FUND_PREDICATE), dataProviders));
        }
        return map;
    }
}