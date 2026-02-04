package api.excel.writer.tab;

import api.dto.RequestParamsSupplier;
import api.excel.writer.SMDataWriter;
import api.excel.writer.WritableSpreadsheet;
import api.util.CommonTools;
import com.fintex.ce.adapter.graphqlclient.repository.CreditQualitySMRepository;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.enumeration.calculation.CreditQualityRating;
import com.fintex.ce.domain.model.CreditQuality;
import com.fintex.ce.domain.model.holding.GicHolding;
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
import static java.util.stream.Collectors.toMap;

public class CreditQualityFDSTab extends SMDataWriter<Holding, String, BigDecimal> implements WritableSpreadsheet {
  private static final CreditQualitySMRepository CREDIT_QUALITY_FDS = initCreditQualityFDS();
  private static final String TAB_NAME = "CreditQuality_FDS";

  private static CreditQualitySMRepository initCreditQualityFDS() {
    return new CreditQualitySMRepository(CommonTools.GRAPHQL_TRANSPORT_COMPONENT);
  }

  @Override
  public void write(List<Holding> holdings, RequestParamsSupplier params, Workbook workbook) {
    final Sheet sheet = Objects.requireNonNull(Objects.requireNonNull(workbook.getSheet(TAB_NAME)));

    final Map<Holding, CreditQuality> creditQuality = callFds(holdings, params.getDataProviders());
    creditQuality.putAll(addGic(holdings));

    final Map<Holding, Pair<DataProvider, Map<String, BigDecimal>>> rawData = creditQuality.entrySet().stream().collect(
        toMap(
            Map.Entry::getKey,
            e -> Pair.of(DataProvider.of(e.getValue().getProvider()), e.getValue().getRatings())));
    fillSpreadSheet(rawData, params.getDataProviders(), sheet);
  }

  private Map<Holding, CreditQuality> callFds(final List<Holding> holdings, final List<DataProvider> dataProviders) {
    final Map<Holding, CreditQuality> map = new LinkedHashMap<>();
    if (!filterHoldings(holdings, US_ETF_PREDICATE).isEmpty()) {
      map.putAll(CREDIT_QUALITY_FDS.queryBenchOfOfEtfUs(filterHoldings(holdings, US_ETF_PREDICATE), dataProviders));
    }
    if (!filterHoldings(holdings, CANADA_ETF_PREDICATE).isEmpty()) {
      map.putAll(CREDIT_QUALITY_FDS.queryBenchOfEtfCanada(filterHoldings(holdings, CANADA_ETF_PREDICATE),
          dataProviders));
    }
    if (!filterHoldings(holdings, CANADA_MUTUAL_PREDICATE).isEmpty()) {
      map.putAll(CREDIT_QUALITY_FDS.queryBenchOfFundCanada(filterHoldings(holdings, CANADA_MUTUAL_PREDICATE),
          dataProviders));
    }
    if (!filterHoldings(holdings, CANADA_POOLED_FUND_PREDICATE).isEmpty()) {
      map.putAll(CREDIT_QUALITY_FDS.queryCanadaPooledFunds(filterHoldings(holdings, CANADA_POOLED_FUND_PREDICATE),
          dataProviders));
    }
    if (!filterHoldings(holdings, CANADA_HEDGE_FUND_PREDICATE).isEmpty()) {
      map.putAll(CREDIT_QUALITY_FDS.queryCanadaHedgeFunds(filterHoldings(holdings, CANADA_HEDGE_FUND_PREDICATE),
          dataProviders));
    }
    if (!filterHoldings(holdings, US_MUTUAL_FUND_PREDICATE).isEmpty()) {
      map.putAll(CREDIT_QUALITY_FDS.queryUsMutualFunds(filterHoldings(holdings, US_MUTUAL_FUND_PREDICATE),
          dataProviders));
    }
    return map;
  }

  private Map<Holding, CreditQuality> addGic(final List<Holding> holdings) {
    final List<Holding> filteredHoldings = holdings.stream().filter(holding -> holding.getType() == HoldingType.GIC)
        .collect(Collectors.toList());
    final Map<Holding, CreditQuality> result = new LinkedHashMap<>();
    for (final Holding holding : filteredHoldings) {
      final GicHolding gic = (GicHolding) holding;
      if (!gic.isLessThanOneYearOld()) {
        final CreditQuality rCreditQuality = new CreditQuality(HoldingType.GIC, Map.of(CreditQualityRating.AAA.name(),
            BigDecimal.ONE));
        result.put(holding, rCreditQuality);
      }
    }
    return result;
  }
}
