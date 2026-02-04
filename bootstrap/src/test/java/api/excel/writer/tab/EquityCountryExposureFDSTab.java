package api.excel.writer.tab;

import api.dto.RequestParamsSupplier;
import api.excel.writer.SMDataWriter;
import api.excel.writer.WritableSpreadsheet;
import api.util.CommonTools;
import com.fintex.ce.adapter.graphqlclient.repository.EquityCountryAllocationSMRepository;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.EquityCountryAllocation;
import com.fintex.ce.domain.model.holding.Holding;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.fintex.ce.util.FilterUtils.*;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toMap;

public class EquityCountryExposureFDSTab extends SMDataWriter<Holding, String, BigDecimal>
    implements
      WritableSpreadsheet {
  private static final EquityCountryAllocationSMRepository EQUITY_COUNTRY_EXPOSURE_FDS = initEquityCountryExposureFDS();
  private static final String TAB_NAME = "EquityCountryExposure_FDS";
  private final boolean needToCheckDataProvidersFromResponse;

  private static EquityCountryAllocationSMRepository initEquityCountryExposureFDS() {
    return new EquityCountryAllocationSMRepository(CommonTools.GRAPHQL_TRANSPORT_COMPONENT);
  }

  public EquityCountryExposureFDSTab(final boolean needToCheckDataProvidersFromResponse) {
    this.needToCheckDataProvidersFromResponse = needToCheckDataProvidersFromResponse;
  }

  @Override
  public void write(List<Holding> holdings, RequestParamsSupplier params, Workbook workbook) {
    final Sheet sheet = Objects.requireNonNull(Objects.requireNonNull(workbook.getSheet(TAB_NAME)));

    final Map<Holding, EquityCountryAllocation> assetAllocations = callFds(holdings, params.getDataProviders());

    final Map<Holding, Pair<DataProvider, Map<String, BigDecimal>>> rawData = assetAllocations.entrySet().stream()
        .collect(
            toMap(
                Map.Entry::getKey,
                e -> Pair.of(DataProvider.of(e.getValue().getProvider()), e.getValue().getAllocations())));
    populateTab(rawData, sheet);
  }

  private Map<Holding, EquityCountryAllocation> callFds(final List<Holding> holdings,
      final List<DataProvider> dataProviders) {
    final Map<Holding, EquityCountryAllocation> map = new LinkedHashMap<>();
    if (!filterHoldings(holdings, US_ETF_PREDICATE).isEmpty()) {
      map.putAll(checkDataProvidersFromResponse(EQUITY_COUNTRY_EXPOSURE_FDS.queryBenchOfOfEtfUs(filterHoldings(holdings,
          US_ETF_PREDICATE), dataProviders), dataProviders));
    }
    if (!filterHoldings(holdings, CANADA_ETF_PREDICATE).isEmpty()) {
      map.putAll(checkDataProvidersFromResponse(EQUITY_COUNTRY_EXPOSURE_FDS.queryBenchOfEtfCanada(filterHoldings(
          holdings, CANADA_ETF_PREDICATE), dataProviders), dataProviders));
    }
    if (!filterHoldings(holdings, CANADA_MUTUAL_PREDICATE).isEmpty()) {
      map.putAll(checkDataProvidersFromResponse(EQUITY_COUNTRY_EXPOSURE_FDS.queryBenchOfFundCanada(filterHoldings(
          holdings, CANADA_MUTUAL_PREDICATE), dataProviders), dataProviders));
    }
    if (!filterHoldings(holdings, CANADA_POOLED_FUND_PREDICATE).isEmpty()) {
      map.putAll(checkDataProvidersFromResponse(EQUITY_COUNTRY_EXPOSURE_FDS.queryCanadaPooledFunds(filterHoldings(
          holdings, CANADA_POOLED_FUND_PREDICATE), dataProviders), dataProviders));
    }
    if (!filterHoldings(holdings, CANADA_HEDGE_FUND_PREDICATE).isEmpty()) {
      map.putAll(checkDataProvidersFromResponse(EQUITY_COUNTRY_EXPOSURE_FDS.queryCanadaHedgeFunds(filterHoldings(
          holdings, CANADA_HEDGE_FUND_PREDICATE), dataProviders), dataProviders));
    }
    return map;
  }

  private Map<? extends Holding, EquityCountryAllocation> checkDataProvidersFromResponse(
      final Map<? extends Holding, EquityCountryAllocation> allocations,
      final List<DataProvider> dataProviders) {
    return needToCheckDataProvidersFromResponse
        ? allocations.entrySet().stream()
            .filter(a -> dataProviders.isEmpty() || (nonNull(a.getValue()) && nonNull(a.getValue().getProvider())
                && !a.getValue().getAllocations().isEmpty() && dataProviders.contains(DataProvider.of(a.getValue()
                    .getProvider()))))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        : allocations;
  }
}