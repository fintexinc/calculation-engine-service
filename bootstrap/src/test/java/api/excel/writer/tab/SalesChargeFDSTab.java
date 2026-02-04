package api.excel.writer.tab;

import api.dto.RequestParamsSupplier;
import api.excel.writer.SMDataWriter;
import api.excel.writer.WritableSpreadsheet;
import api.util.CommonTools;
import com.fintex.ce.adapter.graphqlclient.repository.SalesChargeSMRepository;
import com.fintex.ce.domain.model.SalesCharge;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.holding.Holding;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.fintex.ce.util.FilterUtils.CANADA_MUTUAL_PREDICATE;
import static com.fintex.ce.util.FilterUtils.filterHoldings;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toMap;

public class SalesChargeFDSTab extends SMDataWriter<Holding, String, BigDecimal> implements WritableSpreadsheet {

  private static final SalesChargeSMRepository EQUITY_SECTOR_FDS = iniEquitySectorFDS();
  private static final String TAB_NAME = "Data";

  private static SalesChargeSMRepository iniEquitySectorFDS() {
    return new SalesChargeSMRepository(CommonTools.GRAPHQL_TRANSPORT_COMPONENT);
  }

  @Override
  public void write(final List<Holding> holdings,
      final RequestParamsSupplier params,
      final Workbook workbook) {
    final Sheet sheet = Objects.requireNonNull(Objects.requireNonNull(workbook.getSheet(TAB_NAME)));
    final Map<FundSeriesHolding, SalesCharge> results = EQUITY_SECTOR_FDS.queryBenchOfFundCanada(
        filterHoldings(holdings, CANADA_MUTUAL_PREDICATE), List.of());
    fillSpreadSheet(results, sheet);
  }

  private void fillSpreadSheet(final Map<FundSeriesHolding, SalesCharge> results, final Sheet sheet) {
    final Map<String, SalesCharge> holdings = results.entrySet().stream()
        .collect(toMap(e -> e.getKey().getFundServCode(), Map.Entry::getValue));
    for (int i = 2; i < 12; i++) {
      Row row = sheet.getRow(i);
      final String holdingCode = row.getCell(0).getStringCellValue();
      final SalesCharge salesCharge = holdings.get(holdingCode);
      assert (nonNull(salesCharge));
      row.getCell(1).setCellValue(salesCharge.getValue());
    }
  }

}
