package api.testcases.v1x5;

import api.config.constant.CEVersion;
import api.dto.tab.LeadingTestCaseModel;
import api.model.HoldingDataDTO;
import api.testcases.core.CoreApiTest;
import api.testcases.core.dto.ExpectedResultWrapper;
import api.testcases.v1x5.core.CoreAdaptor;
import api.testcases.v1x5.dto.request.FactInputDTO;
import api.testcases.v1x5.dto.request.PortfolioRequestDTO;
import api.testcases.v1x5.dto.response.PortfolioResponseDTO;
import api.util.RestUtils;
import com.fintex.ce.dto.response.LeadingTotalReturnsResDTO;
import com.fintex.ce.dto.response.core.TimeIntervalResDTO;
import com.fintex.ce.util.ComparisonUtils;
import com.fintex.ce.util.JacksonUtil;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.fintex.ce.config.enumeration.Period.SINCE_CUSTOM_INTERVAL_PERFORMANCE_START_DATE;
import static com.fintex.ce.config.enumeration.Period.SINCE_PERFORMANCE_START_DATE;
import static com.fintex.ce.config.enumeration.Period.YEAR_TO_DATE;

public class LeadingReturnsV1x5Adapter extends CoreAdaptor<LeadingTestCaseModel, HoldingDataDTO, LeadingTotalReturnsResDTO, Set<TimeIntervalResDTO>> {

    private final String factName;
    private final String URI;

    public LeadingReturnsV1x5Adapter(final CoreApiTest<LeadingTestCaseModel, HoldingDataDTO, LeadingTotalReturnsResDTO,
            Set<TimeIntervalResDTO>> bridge, String factName, final String URI) {
        super(bridge, URI);
        this.factName = factName;
        this.URI = URI;
    }

    //this method is overridden from abstract class because we need to skip a few tests
    @Override
    public void validate(final ExpectedResultWrapper<Set<TimeIntervalResDTO>> expectedResultWrapper,
                         final Map.Entry<Integer, LeadingTestCaseModel> testCaseEntry) {
        // We skip tests 14, 15, 16 because CE1.5 doesn't handle logic for that
        //14 - 15. Should just be skipped. CIPSD in CE1.5 is used as CPSD would be used in CE2.0
        //16 - (CPED) And with #16 we just need to remember to point out that there's a bug in that ce 1.5 has cped
        //functionality even though its not useful for this calculation, but we think its low risk that anyone would use it.
        if (testCaseEntry.getKey() == 14
                || testCaseEntry.getKey() == 15
                || testCaseEntry.getKey() == 16) {
            compare(Set.of(), Set.of());
            return;
        }

        final Object request = buildRequest(testCaseEntry);
        if (expectedResultWrapper.is2xxSuccessful()) {
            final String response = RestUtils.executeAppRequest(URI, request, HttpStatus.OK);
            final Set<TimeIntervalResDTO> actual = getActualResultsFromAppResponse(response);
            final Set<TimeIntervalResDTO> expected = expectedResultWrapper.getValidExpectedResult();
            compare(expected, actual);
        } else {
            // means we had an error on APP (4xx or 5xx status code)
            RestUtils.executeAppRequest(URI, request, expectedResultWrapper.getHttpStatus());
            //  override actual and expected results to avoid errors
            compare(defaultExceptedValues(), defaultExceptedValues());
        }
    }

    @Override
    protected Set<TimeIntervalResDTO> getActualResultsFromAppResponse(final String response) {
        final PortfolioResponseDTO responseDTO = JacksonUtil.deserialize(response.substring(1, response.length() - 1), PortfolioResponseDTO.class);
        final List<PortfolioResponseDTO.ResDTO> res = responseDTO.getResponse();
        final Set<TimeIntervalResDTO> result = res.stream().map(e -> new TimeIntervalResDTO(e.getKey(), e.getValue())).collect(Collectors.toSet());
        return result;
    }

    @Override
    public CEVersion getCEVersion() {
        return CEVersion.CE_1x5;
    }

    @Override
    protected Object buildRequest(final Map.Entry<Integer, LeadingTestCaseModel> testCaseEntry) {
        final PortfolioRequestDTO reqDTO = new PortfolioRequestDTO();
        reqDTO.setPortfolioCurrency(testCaseEntry.getValue().getCurrency().name());
        reqDTO.setPortfolioHoldings(formatHoldingsForTestCase(testCaseEntry.getValue().getHoldings()));
        reqDTO.setFactInput(Set.of(new FactInputDTO(factName, initializeParameters(testCaseEntry.getValue()))));
        return reqDTO;
    }

    private Map<String, Object> initializeParameters(final LeadingTestCaseModel testCase) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("customIntervalPerformanceStartDate", testCase.getCustomPsd());
        parameters.put("customPerformanceEndDate", testCase.getCustomPed());
        parameters.put("timeperiodintervals", mapPeriodIntervalsAsInteger(testCase));
        return parameters;
    }

    private List<Integer> mapPeriodIntervalsAsInteger(final LeadingTestCaseModel testCase) {
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
    protected void compare(final Set<TimeIntervalResDTO> expectedRes,
                           final Set<TimeIntervalResDTO> actualRes) {
        final Map<String, BigDecimal> actual = actualRes.stream().collect(HashMap::new, (m, v) -> m.put(v.getTimeIntervalPeriod(), v.getValue()), HashMap::putAll);
        final Map<String, BigDecimal> expected = expectedRes.stream().collect(HashMap::new, (m, v) -> m.put(v.getTimeIntervalPeriod(), v.getValue()), HashMap::putAll);

        ComparisonUtils.compareMaps(expected, actual);
    }

    @Override
    protected Set<TimeIntervalResDTO> defaultExceptedValues() {
        return Set.of();
    }
}
