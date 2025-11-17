package api.testcases.core;

import api.dto.RequestParamsSupplier;
import api.dto.tab.PeriodTestCaseModel;
import api.excel.reader.HoldingReaderTab;
import api.excel.reader.TestCaseReaderTab;
import api.excel.writer.WritableSpreadsheet;
import api.excel.writer.tab.MonthlyReturnTab;
import api.model.HoldingDataDTO;
import api.testcases.core.dto.TabInfoDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fintex.ce.dto.holding.Holding;
import lombok.extern.log4j.Log4j2;
import org.apache.poi.ss.util.CellAddress;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * @param <T> stands for: Test Case Type
 * @param <H> stands for: Holding Type
 * @param <R> stands for: Application Response Object
 * @param <E> stands for: Expected & Actual results type
 */

@Log4j2
public abstract class CoreApiBenchmarkTest<T extends PeriodTestCaseModel, H extends HoldingDataDTO, R, E> extends CoreApiTest<T, H, R, E> {

    protected CoreApiBenchmarkTest(String resourceURL,
                          TypeReference<R> responseType,
                          String excelRelativePath,
                          String name,
                          CellAddress testNumberCell,
                          TestCaseReaderTab<T> testCaseTab,
                          HoldingReaderTab<H> holdingDataTab,
                          List<TabInfoDTO> writableTabs) {
        super(resourceURL, responseType, excelRelativePath, name, testNumberCell, testCaseTab, holdingDataTab, writableTabs);
    }

    @Override
    protected void populateTabsBeforeEachTestCase(List<WritableSpreadsheet> tabs, Integer testNumber, T testCaseBody) {
        final List<Holding> appHoldings = formatHoldingsForTestCase(testCaseBody.getHoldings());
        final List<Holding> appBenchmarkHoldings = formatHoldingsForTestCase(testCaseBody.getBenchmarkHoldings());
        final HashSet<Holding> setOfHoldings = new HashSet<>();
        setOfHoldings.addAll(appHoldings);
        setOfHoldings.addAll(appBenchmarkHoldings);
        final List<Holding> doRequestForTheseHoldings = new ArrayList<>(setOfHoldings);
        RequestParamsSupplier params = new RequestParamsSupplier(testCaseBody.getDataProviders(), testCaseBody.getCurrency());
        addMoreParams(params, testCaseBody);
        tabs.stream().filter(tab -> tab instanceof MonthlyReturnTab).forEach(tab -> ((MonthlyReturnTab) tab).setDefaultHoldings(defaultHoldings));
        tabs.forEach(tab -> tab.write(doRequestForTheseHoldings, params, this.workbook));
    }

}
