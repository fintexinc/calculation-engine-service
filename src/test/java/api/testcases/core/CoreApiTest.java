package api.testcases.core;

import api.config.constant.CEVersion;
import api.dto.RequestParamsSupplier;
import api.dto.tab.CoreTestCaseModel;
import api.excel.reader.HoldingReaderTab;
import api.excel.reader.TestCaseReaderTab;
import api.excel.writer.WritableSpreadsheet;
import api.excel.writer.tab.MonthlyReturnTab;
import api.model.HoldingDataDTO;
import api.testcases.core.dto.ExpectedResultWrapper;
import api.testcases.core.dto.Results;
import api.testcases.core.dto.TabInfoDTO;
import api.testcases.core.dto.TestCaseExecutor;
import api.testcases.v1x5.core.CoreAdaptor;
import api.util.RestUtils;
import api.util.TestProperties;
import api.util.excel.ExcelUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.io.Closeables;
import com.fintex.ce.config.enumeration.HoldingType;
import com.fintex.ce.dto.holding.GicHolding;
import com.fintex.ce.dto.holding.Holding;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellAddress;
import org.junit.jupiter.params.provider.Arguments;
import org.springframework.http.HttpStatus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static api.util.HoldingUtils.createAppSpecificHoldingBasedOnType;

/**
 * @param <T> stands for: Test Case Type
 * @param <H> stands for: Holding Type
 * @param <R> stands for: Application Response Object
 * @param <E> stands for: Expected & Actual results type
 */

@Log4j2
public abstract class CoreApiTest<T extends CoreTestCaseModel, H extends HoldingDataDTO, R, E> {
    public static final String GIC_CAD_KEYWORD = "GIC.C";
    public static final String GIC_USD_KEYWORD = "GIC.U";
    private final String masterTabName;

    private final String resourceURL;
    protected final String excelRelativePath;
    protected File spreadsheetFileCopy;

    @Getter
    protected final TestCaseReaderTab<T> testCaseTab;
    @Getter
    private final HoldingReaderTab<H> holdingDataTab;
    @Getter
    protected final List<TabInfoDTO> writableTabs;
    private final CellAddress testNumberCell;
    private final TypeReference<R> responseType;

    protected List<Holding> defaultHoldings;

    @Getter
    protected Workbook workbook;
    // initial holdings from tab HOLDINGS_DATA or similar tab
    // KEY - holding code, VALUE - holding body
    @Getter
    private Map<String, H> rawHoldings;

    // initial test cases from tab TEST_CASES  or similar tab
    // KYE - test case number, VALUE - test case body
    @Getter
    protected Map<Integer, T> testCases;

    @Getter
    private final List<CoreAdaptor<T, H, R, E>> adaptors;

    protected CoreApiTest(String resourceURL,
                          TypeReference<R> responseType,
                          String excelRelativePath,
                          String name,
                          CellAddress testNumberCell,
                          TestCaseReaderTab<T> testCaseTab,
                          HoldingReaderTab<H> holdingDataTab,
                          List<TabInfoDTO> writableTabs) {
        this.resourceURL = resourceURL;
        this.responseType = responseType;
        this.excelRelativePath = excelRelativePath;
        this.masterTabName = name;
        this.testNumberCell = testNumberCell;
        this.testCaseTab = testCaseTab;
        this.holdingDataTab = holdingDataTab;
        this.writableTabs = writableTabs;
        this.adaptors = new ArrayList<>();
    }

    protected void prepopulateWorkbook() {
        var originalSpreadsheet = new File(ExcelUtils.class.getResource(this.excelRelativePath).getPath());
        var copySpreadsheet = new File(getRandomFileCopyName());
        copyFile(originalSpreadsheet, copySpreadsheet);

        this.spreadsheetFileCopy = copySpreadsheet;
        this.workbook = ExcelUtils.openWorkbook(this.spreadsheetFileCopy);
        loadBasicInfo();
        populateRequiredTabsFirst();
    }

    protected void copyFile(File sourceFile, File destFile) {
        try {
            FileUtils.copyFile(sourceFile, destFile);
        } catch (IOException e) {
            log.error("Error while creating copy of excel file, exception: ", e);
            throw new UncheckedIOException(e);
        }
    }

    protected String getRandomFileCopyName() {
        return UUID.randomUUID() + ".xlsx";
    }

    protected void closeResources() {
        try {
            if (this.workbook != null) {
                Closeables.close(workbook, false);
                workbook = null;
                FileUtils.forceDelete(spreadsheetFileCopy);
            }
        } catch (IOException e) {
            String message = String.format("You had en error while closing workbook %s, error message %s", this.excelRelativePath, e.getMessage());
            log.error(message);
        }
    }


    /**
     * For debugging purposes only!
     * If doesn't work then try to override existed excel.
     *
     * @param workbookName just the filename without extension, example: test_excel_name
     */
    protected void saveWorkbook(String workbookName) {
        try {
            String dirStr = String.format("%s/%s/", System.getProperty("user.dir"), "tmp");
            File dir = new File(dirStr);
            if (!dir.exists()) {
                dir.mkdir();
            }
            String path = String.format(dirStr + "%s.xlsx", workbookName);
            FileOutputStream fileOut = new FileOutputStream(path);
            workbook.write(fileOut);
            fileOut.close();
        } catch (IOException e) {
            String message = String.format("You had en error while saving workbook %s, error message %s", this.excelRelativePath, e.getMessage());
            log.error(message);
        }
    }

    protected void populateRequiredTabsFirst() {
        // for those tabs the WEIGH doesn't matter
        defaultHoldings = this.rawHoldings.entrySet().stream()
                .map(e -> createAppSpecificHoldingBasedOnType(e.getKey(), BigDecimal.ZERO, e.getValue()))
                .collect(Collectors.toList());
        // populate data for tabs which have to be populated at the beginning
        this.writableTabs.stream().filter(t -> !t.isBeforeEachTest()).map(TabInfoDTO::getTab).filter(t -> t instanceof MonthlyReturnTab).forEach(t -> ((MonthlyReturnTab) t).setDefaultHoldings(defaultHoldings));
        this.writableTabs.stream().filter(t -> !t.isBeforeEachTest()).forEach(t -> populateDataForTabs(t, defaultHoldings));
    }

    /**
     * Populates data for tabs which have to be populated at the beginning
     *
     * @param t           tab into
     * @param appHoldings formatted holdings
     */
    protected void populateDataForTabs(TabInfoDTO t, List<Holding> appHoldings) {
        WritableSpreadsheet tab = t.getTab();
        tab.write(appHoldings, new RequestParamsSupplier(), this.workbook);
    }

    protected void loadBasicInfo() {
        this.testCases = this.testCaseTab.read(this.workbook);
        this.rawHoldings = this.holdingDataTab.read(this.workbook);
    }

    private void populateGicHoldingsWithData(T holdings) {
        if (this.rawHoldings == null || defaultHoldings == null) {
            return;
        }
        this.rawHoldings.forEach((s, h) -> {
            if (s.equalsIgnoreCase(GIC_CAD_KEYWORD) || s.equalsIgnoreCase(GIC_USD_KEYWORD)){
                h.setGicInvestmentDate(holdings.getGicInvestmentDate());
                h.setGicInterestFreq(holdings.getGicInterestFreq());
                h.setGicClientIntRate(holdings.getGicClientIntRate());
                h.setGicTerm(holdings.getGicTerm());
                h.setGicName(holdings.getGicName());
            }
        });
        defaultHoldings.forEach(h -> {
            if (h.getType() == HoldingType.GIC) {
                final GicHolding gicHolding = (GicHolding) h;
                gicHolding.setInvestmentDate(holdings.getGicInvestmentDate());
                gicHolding.setInterestFreq(holdings.getGicInterestFreq());
                gicHolding.setClientIntRate(holdings.getGicClientIntRate());
                gicHolding.setTerm(holdings.getGicTerm());
                gicHolding.setName(holdings.getGicName());
            }
        });
    }

    public Stream<Arguments> loadArgs(String dataPointName) {
        prepopulateWorkbook();
        initializeAdaptors(this.adaptors);
        List<WritableSpreadsheet> tabs = this.writableTabs.stream()
                .filter(TabInfoDTO::isBeforeEachTest).map(TabInfoDTO::getTab).collect(Collectors.toList());
        Sheet master = this.workbook.getSheet(this.masterTabName);
        return this.testCases.entrySet().stream().map(entry -> {
            Supplier<Results<E>> results = buildDelayedTestCaseExecutor(tabs, master, entry);
            return buildArguments(entry, results, dataPointName);
        });
    }

    private Arguments buildArguments(Map.Entry<Integer, T> entry, Supplier<Results<E>> results, String dataPointName) {
        TestCaseExecutor<E> testCaseExecutor = new TestCaseExecutor<>(results, entry.getKey());
        dataPointName = dataPointName.substring(0,1).toUpperCase() + dataPointName.substring(1).toLowerCase();
        String description = dataPointName.replace("-", " ").trim() + " "  + this.testCases.get(entry.getKey()).getDescription().trim();
        return Arguments.of(entry.getKey(), description, testCaseExecutor);
    }

    /**
     * It has to be executed inside the test implementation!
     *
     * @param tabs          tabs which have to be populated before new test case
     * @param master        tab that contains master expected results
     * @param testCaseEntry KEY - test case number, VALUE - test case body
     * @return test case executor
     */
    private Supplier<Results<E>> buildDelayedTestCaseExecutor(List<WritableSpreadsheet> tabs, Sheet master, Map.Entry<Integer, T> testCaseEntry) {
        return () -> {
            // set new test case number to the master tab
            var testCaseNumber = testCaseEntry.getKey();
            master.getRow(this.testNumberCell.getRow()).getCell(this.testNumberCell.getColumn()).setCellValue(testCaseNumber);
            log.info("start test case: {}", testCaseNumber);
            long start1 = System.currentTimeMillis();
            // populate GIC holding with new data foe each test case
            populateGicHoldingsWithData(testCaseEntry.getValue());
            // populate new data based on new DataProviders for the new test case
            populateTabsBeforeEachTestCase(tabs, testCaseNumber, testCaseEntry.getValue());
            // re-calculate everything for the new test case
            long start2 = System.currentTimeMillis();
            reevaluateBeforeEachTestCase(master);
            log.info("reevaluating, testCase: {}, took: {}ms", testCaseNumber, System.currentTimeMillis() - start2);
            // load expected data from master tab

            long start3 = System.currentTimeMillis();
            ExpectedResultWrapper<E> expected = loadExpectedResultsFromExcel(master);
            log.info("loading expected data, testCase: {}, took: {}ms", testCaseNumber, System.currentTimeMillis() - start3);

            // query other CE versions
            validateOtherCEVersions(expected, testCaseEntry);
            // if CE 2.0 tests enabled
            if (TestProperties.getCEVersions().isEnableCE2x0()) {
                long start4 = System.currentTimeMillis();
                Results<E> actualResult = queryApplication(testCaseEntry, expected);
                log.info("getting actual results, testCase: {}, took {}ms", testCaseNumber, System.currentTimeMillis() - start4);
                log.info("In total testCase: {}, took: {} seconds", testCaseNumber, (System.currentTimeMillis() - start1) / 1000);
                return actualResult;
            }

            return new Results<>(defaultExceptedValues(), defaultExceptedValues());
        };
    }

    private void validateOtherCEVersions(ExpectedResultWrapper<E> expected, Map.Entry<Integer, T> testCaseEntry) {
        for (CoreAdaptor<T, H, R, E> adaptor : adaptors) {
            if (CEVersion.CE_1x5.equals(adaptor.getCEVersion())
                    && TestProperties.getCEVersions().isEnableCE1x5()) {
                adaptor.validate(expected, testCaseEntry);
            } else if (CEVersion.CE_1x3.equals(adaptor.getCEVersion())
                    && TestProperties.getCEVersions().isEnableCE1x3()) {
                adaptor.validate(expected, testCaseEntry);
            }
        }
    }

    /**
     * Queries APP to get the actual response
     *
     * @param testCaseEntry         KEY - test case number, VALUE - test case body
     * @param expectedResultWrapper results from EXCEL
     * @return actual and expected results
     */
    private Results<E> queryApplication(Map.Entry<Integer, T> testCaseEntry, ExpectedResultWrapper<E> expectedResultWrapper) {
        Object appRequestBody = buildAppRequest(testCaseEntry.getValue());
        if (expectedResultWrapper.is2xxSuccessful() && Objects.isNull(expectedResultWrapper.getOnFailedResponseFromApp())) {
            R response = RestUtils.executeAppRequest(this.resourceURL, appRequestBody, HttpStatus.OK, this.responseType);
            E actual = getActualResultsFromAppResponse(response);
            E expected = expectedResultWrapper.getValidExpectedResult();
            return new Results<>(expected, actual, true);
        } else {
            // means we had an error on APP (4xx or 5xx status code)
            String appResStr = RestUtils.executeAppRequest(this.resourceURL, appRequestBody, expectedResultWrapper.getHttpStatus());
            // perform user provided assertions on (4xx or 5xx status code)
            expectedResultWrapper.getOnFailedResponseFromApp().accept(appResStr);
            //  override actual and expected results with (@link defaultExceptedValues) to ovoid errors
            return new Results<>(defaultExceptedValues(), defaultExceptedValues(), false);
        }
    }

    protected void populateTabsBeforeEachTestCase(List<WritableSpreadsheet> tabs, Integer testNumber, T testCaseBody) {
        Map<String, BigDecimal> holdings = testCaseBody.getHoldings();
        List<Holding> appHoldings = formatHoldingsForTestCase(holdings);
        RequestParamsSupplier params = new RequestParamsSupplier(testCaseBody.getDataProviders(), testCaseBody.getCurrency());
        addMoreParams(params, testCaseBody);
        tabs.stream().filter(tab -> tab instanceof MonthlyReturnTab).forEach(tab -> ((MonthlyReturnTab) tab).setDefaultHoldings(defaultHoldings));
        tabs.forEach(tab -> tab.write(appHoldings, params, this.workbook));
    }

    public void addMoreParams(RequestParamsSupplier params, T testCaseBody) {
        // override if needed at your test case implementation
    }

    /**
     * @param weights that are part of some test case
     * @return mapped to holding that can be consumed by application
     */
    public List<Holding> formatHoldingsForTestCase(Map<String, BigDecimal> weights) {
        List<Holding> holdings = this.rawHoldings.entrySet().stream()
                .filter(e -> weights.containsKey(e.getKey()))
                .map(e -> createAppSpecificHoldingBasedOnType(e.getKey(), weights.get(e.getKey()), e.getValue()))
                .collect(Collectors.toList());
        return holdings;
    }

    /**
     * Specify places which have to be reevaluated before new test case comes in
     *
     * @param master tab that has expected results
     */
    public abstract void reevaluateBeforeEachTestCase(Sheet master);

    /**
     * Builds the request to the application based on entered test case
     *
     * @param testCase loaded test cases from excel
     * @return application request
     */
    public abstract Object buildAppRequest(T testCase);

    /**
     * Just gets the data from the whole application response which have to be compared with expected data
     *
     * @param appResponse the whole application response
     * @return actual data to compare
     */
    public abstract E getActualResultsFromAppResponse(R appResponse);

    /**
     * Loads expected results and builds the rules to process some exceptional cases such as logical errors
     *
     * @param master tab that contains expected data
     * @return expected data
     */
    public abstract ExpectedResultWrapper<E> loadExpectedResultsFromExcel(Sheet master);

    /**
     * Values which can be used as a dummy data when response status code is not 200 OK
     *
     * @return dummy data
     */
    public abstract E defaultExceptedValues();

    public void initializeAdaptors(List<CoreAdaptor<T, H, R, E>> adaptors) {
        // override if needed
    }

}
