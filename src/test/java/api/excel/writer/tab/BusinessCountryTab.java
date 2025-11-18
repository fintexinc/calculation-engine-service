package api.excel.writer.tab;

import api.dto.RequestParamsSupplier;
import api.excel.writer.WritableSpreadsheet;
import api.util.IdentifierUtils;
import api.util.excel.ExcelUtils;
import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.holding.StockHolding;
import com.fintex.ce.model.redis.RBusinessCountry;
import com.fintex.ce.repository.graphql.query.BusinessCountrySMRepository;
import com.fintex.ce.util.FilterUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static api.util.CommonTools.GRAPHQL_TRANSPORT_COMPONENT;
import static com.fintex.ce.util.FilterUtils.STOCK_PREDICATE;

@Log4j2
public class BusinessCountryTab implements WritableSpreadsheet {
    private static final BusinessCountrySMRepository BUSINESS_COUNTRY_FDS = initAssetAllocationsFDS();
    private static final String TAB_NAME = "BusinessCountry_FDS";

    private static BusinessCountrySMRepository initAssetAllocationsFDS() {
        return new BusinessCountrySMRepository(GRAPHQL_TRANSPORT_COMPONENT);
    }

    @Override
    public void write(final List<Holding> holdings, final RequestParamsSupplier params, final Workbook workbook) {
        if (CollectionUtils.isEmpty(holdings)) {
            log.info("Don't have any holdings to perform business country insertion");
            return;
        }

        final Sheet sheet = Objects.requireNonNull(workbook.getSheet(TAB_NAME));
        final Map<StockHolding, RBusinessCountry> businessCountries = callFds(holdings, params.getDataProviders());

        fillSpreadSheet(businessCountries, sheet);
        ExcelUtils.reevaluateSheet(CellRangeAddress.valueOf("D4:Q4"), sheet);
    }

    private Map<StockHolding, RBusinessCountry> callFds(List<Holding> holdings, List<DataProvider> providers) {
        return BUSINESS_COUNTRY_FDS.queryBenchOfStock(FilterUtils.filterHoldings(holdings, STOCK_PREDICATE), providers);
    }

    private void fillSpreadSheet(final Map<StockHolding, RBusinessCountry> businessCountries, final Sheet sheet) {
        ExcelUtils.clearSheet(sheet, CellRangeAddress.valueOf("B1:Q3"));

        int holdingCol = 3;
        for (Map.Entry<StockHolding, RBusinessCountry> entry : businessCountries.entrySet()) {

            Row holdingNameRow = sheet.getRow(0);
            final String holdingName = IdentifierUtils.cutUserIdentifier(entry.getKey());
            holdingNameRow.getCell(holdingCol).setCellValue(holdingName);

            Row row2 = sheet.getRow(1);
            row2.getCell(holdingCol).setCellValue(entry.getValue().getProvider());

            Row row3 = sheet.getRow(3);
            row3.getCell(holdingCol).setCellValue(entry.getValue().getValue());
            holdingCol++;
        }
    }

}
