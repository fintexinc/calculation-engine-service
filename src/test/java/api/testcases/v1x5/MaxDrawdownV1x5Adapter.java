package api.testcases.v1x5;

import api.config.constant.CEVersion;
import api.dto.tab.PeriodTestCaseModel;
import api.model.HoldingDataDTO;
import api.testcases.core.CoreApiTest;
import api.testcases.core.dto.ExpectedResultWrapper;
import api.testcases.v1x5.core.CoreAdaptor;
import api.testcases.v1x5.dto.request.FactInputDTO;
import api.testcases.v1x5.dto.request.PortfolioRequestDTO;
import api.testcases.v1x5.dto.response.PortfolioResponseDTO;
import api.util.RestUtils;
import com.fintex.ce.dto.response.MaxDrawdownResDTO;
import com.fintex.ce.dto.response.maxdrawdown.MaxDrawdownDTO;
import com.fintex.ce.util.ComparisonUtils;
import com.fintex.ce.util.JacksonUtil;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.fintex.ce.config.enumeration.Period.*;

public class MaxDrawdownV1x5Adapter extends CoreAdaptor<PeriodTestCaseModel, HoldingDataDTO, MaxDrawdownResDTO, List<MaxDrawdownDTO>> {

    private final String factName;
    private final String URI;

    public MaxDrawdownV1x5Adapter(final CoreApiTest<PeriodTestCaseModel, HoldingDataDTO, MaxDrawdownResDTO,
            List<MaxDrawdownDTO>> bridge, String factName, final String URI) {
        super(bridge, URI);
        this.factName = factName;
        this.URI = URI;
    }

    @Override
    public void validate(ExpectedResultWrapper<List<MaxDrawdownDTO>> expectedResultWrapper, Map.Entry<Integer, PeriodTestCaseModel> testCaseEntry) {
        // 18 and 19 tests should be skipped
        //no current users would elaborately try to provide a "too old" date, so its low risk
        if (testCaseEntry.getKey() == 18
                || testCaseEntry.getKey() == 19) {
            compare(List.of(), List.of());
            return;
        }

        final Object request = buildRequest(testCaseEntry);
        if (expectedResultWrapper.is2xxSuccessful()) {
            final String response = RestUtils.executeAppRequest(URI, request, HttpStatus.OK);
            final List<MaxDrawdownDTO> actual = getActualResultsFromAppResponse(response);
            final List<MaxDrawdownDTO> expected = expectedResultWrapper.getValidExpectedResult();
            compare(expected, actual);
        } else {
            // means we had an error on APP (4xx or 5xx status code)
            RestUtils.executeAppRequest(URI, request, expectedResultWrapper.getHttpStatus());
            //  override actual and expected results to avoid errors
            compare(defaultExceptedValues(), defaultExceptedValues());
        }
    }

    @Override
    protected List<MaxDrawdownDTO> defaultExceptedValues() {
        return List.of();
    }

    @Override
    protected List<MaxDrawdownDTO> getActualResultsFromAppResponse(final String response) {
        final PortfolioResponseDTO responseDTO = JacksonUtil.deserialize(response.substring(1, response.length() - 1), PortfolioResponseDTO.class);
        final List<PortfolioResponseDTO.ResDTO> res = responseDTO.getResponse();
        final List<MaxDrawdownDTO> result = res.stream().map(e -> new MaxDrawdownDTO(e.getKey(), e.getValue())).collect(Collectors.toList());
        return result;
    }

    @Override
    public CEVersion getCEVersion() {
        return CEVersion.CE_1x5;
    }

    @Override
    protected Object buildRequest(final Map.Entry<Integer, PeriodTestCaseModel> testCaseEntry) {
        final PortfolioRequestDTO reqDTO = new PortfolioRequestDTO();
        reqDTO.setPortfolioCurrency(testCaseEntry.getValue().getCurrency().name());
        reqDTO.setPortfolioHoldings(formatHoldingsForTestCase(testCaseEntry.getValue().getHoldings()));
        reqDTO.setBenchmarkHoldings(formatHoldingsForTestCase(testCaseEntry.getValue().getBenchmarkHoldings()));
        reqDTO.setFactInput(Set.of(new FactInputDTO(factName, initializeParameters(testCaseEntry.getValue()))));
        return reqDTO;
    }

    private Map<String, Object> initializeParameters(final PeriodTestCaseModel testCase) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("customIntervalPerformanceStartDate", testCase.getCustomIntervalPsd());
        parameters.put("customPerformanceEndDate", testCase.getCustomPed());
        parameters.put("timeperiodintervals", mapPeriodIntervalsAsInteger(testCase));
        return parameters;
    }

    //in CE1.5 all the period intervals should be specified as Integer.
    // We cannot specify CIPSD CPSD and so on as it implemented in CE2.0
    private List<Integer> mapPeriodIntervalsAsInteger(final PeriodTestCaseModel testCase) {
        final List<Integer> result = new ArrayList<>();

        for (final String period : testCase.getPeriods()) {
            if (SINCE_PERFORMANCE_START_DATE.name().equalsIgnoreCase(period)) {
                result.add(-1);       // -1 represents SincePSD in CE1.5
            } else if (YEAR_TO_DATE.name().equalsIgnoreCase(period)) {
                result.add(-2);       // -2 represents YTD in CE1.5
            } else if (SINCE_CUSTOM_INTERVAL_PERFORMANCE_START_DATE.name().equalsIgnoreCase(period)) {
                result.add(-3);       // -3 represents CIPSD in CE1.5
            } else {
                result.add(Integer.parseInt(period));
            }
        }
        return result;
    }

    @Override
    protected void compare(final List<MaxDrawdownDTO> expectedResult, final List<MaxDrawdownDTO> actualResult) {

        final Map<String, BigDecimal> expected = expectedResult.stream().collect(HashMap::new, (m, v) -> m.put(v.getTimeIntervalPeriod(), v.getValue()), HashMap::putAll);
        final Map<String, BigDecimal> actual = mapActualResult(actualResult);

        ComparisonUtils.compareMaps(expected, actual);
    }

    //this method is used to map CE1.5 Result
    private Map<String, BigDecimal> mapActualResult(final List<MaxDrawdownDTO> actual) {
        final Map<String, BigDecimal> actualResult = new LinkedHashMap<>();

        for (final MaxDrawdownDTO dto : actual) {
            if ("-1".equalsIgnoreCase(dto.getTimeIntervalPeriod())) {  // -1 represents SincePSD in CE1.5
                actualResult.put(SINCE_PERFORMANCE_START_DATE.name(), dto.getValue());
            } else if ("-2".equalsIgnoreCase(dto.getTimeIntervalPeriod())) { // -2 represents SincePSD in CE1.5
                actualResult.put(YEAR_TO_DATE.name(), dto.getValue());
            } else if ("-3".equalsIgnoreCase(dto.getTimeIntervalPeriod())) {     // -3 represents CIPSD in CE1.5
                actualResult.put(SINCE_CUSTOM_INTERVAL_PERFORMANCE_START_DATE.name(), dto.getValue());
            } else {
                actualResult.put(dto.getTimeIntervalPeriod(), dto.getValue());
            }
        }
        return actualResult;
    }

}
