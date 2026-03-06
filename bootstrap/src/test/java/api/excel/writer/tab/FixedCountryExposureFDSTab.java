package api.excel.writer.tab;

import api.dto.RequestParamsSupplier;
import api.excel.writer.SMDataWriter;
import api.excel.writer.WritableSpreadsheet;
import api.util.CommonTools;
import com.fintex.ce.adapter.graphqlclient.repository.CountryExposureGraphqlDataFetcher;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.CountryExposure;
import com.fintex.ce.domain.model.holding.Holding;
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

public class FixedCountryExposureFDSTab extends SMDataWriter<Holding, String, BigDecimal>
    implements
      WritableSpreadsheet {
  private static final CountryExposureGraphqlDataFetcher Fixed_Income_FDS = initFixedCountryFDS();
  private static final String TAB_NAME = "FICountryExposure_FDS";

  private static CountryExposureGraphqlDataFetcher initFixedCountryFDS() {
    return new CountryExposureGraphqlDataFetcher(CommonTools.GRAPHQL_TRANSPORT_COMPONENT);
  }

  @Override
  public void write(List<Holding> holdings, RequestParamsSupplier params, Workbook workbook) {
    final Sheet sheet = Objects.requireNonNull(Objects.requireNonNull(workbook.getSheet(TAB_NAME)));

    final Map<Holding, CountryExposure> fixedCoutryExposure = callFds(holdings, params.getDataProviders());

    final Map<Holding, Pair<DataProvider, Map<String, BigDecimal>>> rawData = fixedCoutryExposure.entrySet().stream()
        .collect(
            toMap(
                Map.Entry::getKey,
                e -> Pair.of(DataProvider.of(e.getValue().getProvider()), e.getValue().getAllocations())));

    populateTab(rawData, sheet);

  }

  private Map<Holding, CountryExposure> callFds(final List<Holding> holdings, final List<DataProvider> dataProviders) {
    final Map<Holding, CountryExposure> map = new LinkedHashMap<>();
    if (!filterHoldings(holdings, US_ETF_PREDICATE).isEmpty()) {
      map.putAll(Fixed_Income_FDS.queryBenchOfOfEtfUs(filterHoldings(holdings, US_ETF_PREDICATE), dataProviders));
    }
    if (!filterHoldings(holdings, CANADA_ETF_PREDICATE).isEmpty()) {
      map.putAll(Fixed_Income_FDS.queryBenchOfEtfCanada(filterHoldings(holdings, CANADA_ETF_PREDICATE), dataProviders));
    }
    if (!filterHoldings(holdings, CANADA_MUTUAL_PREDICATE).isEmpty()) {
      map.putAll(Fixed_Income_FDS.queryBenchOfFundCanada(filterHoldings(holdings, CANADA_MUTUAL_PREDICATE),
          dataProviders));
    }
    if (!filterHoldings(holdings, CANADA_POOLED_FUND_PREDICATE).isEmpty()) {
      map.putAll(Fixed_Income_FDS.queryCanadaPooledFunds(filterHoldings(holdings, CANADA_POOLED_FUND_PREDICATE),
          dataProviders));
    }
    if (!filterHoldings(holdings, US_MUTUAL_FUND_PREDICATE).isEmpty()) {
      map.putAll(Fixed_Income_FDS.queryUsMutualFunds(filterHoldings(holdings, US_MUTUAL_FUND_PREDICATE),
          dataProviders));
    }
    return map;
  }
}