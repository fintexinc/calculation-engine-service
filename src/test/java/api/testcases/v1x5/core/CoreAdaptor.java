package api.testcases.v1x5.core;

import api.config.constant.CEVersion;
import api.dto.tab.CoreTestCaseModel;
import api.model.HoldingDataDTO;
import api.testcases.core.CoreApiTest;
import api.testcases.core.dto.ExpectedResultWrapper;
import api.testcases.v1x5.dto.request.PortfolioHoldingDTO;
import api.util.HoldingUtils;
import api.util.RestUtils;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class CoreAdaptor<T extends CoreTestCaseModel, H extends HoldingDataDTO, Res, E> {

    protected final CoreApiTest<T, H, Res, E> bridge;
    private final String URI;

    protected CoreAdaptor(final CoreApiTest<T, H, Res, E> bridge, final String URI) {
        this.bridge = bridge;
        this.URI = URI;
    }

    public void validate(final ExpectedResultWrapper<E> expectedResultWrapper, final Map.Entry<Integer, T> testCaseEntry) {
        final Object request = buildRequest(testCaseEntry);
        if (expectedResultWrapper.is2xxSuccessful()) {
            final String response = RestUtils.executeAppRequest(URI, request, HttpStatus.OK);
            final E actual = getActualResultsFromAppResponse(response);
            final E expected = expectedResultWrapper.getValidExpectedResult();
            compare(expected, actual);
        } else {
            // means we had an error on APP (4xx or 5xx status code)
            RestUtils.executeAppRequest(URI, request, expectedResultWrapper.getHttpStatus());
            //  override actual and expected results to avoid errors
            compare(defaultExceptedValues(), defaultExceptedValues());
        }
    }

    protected abstract E defaultExceptedValues();

    protected abstract E getActualResultsFromAppResponse(final String response);

    public abstract CEVersion getCEVersion();

    protected abstract Object buildRequest(final Map.Entry<Integer, T> testCaseEntry);

    protected abstract void compare(final E expected, final E actual);

    public List<PortfolioHoldingDTO> formatHoldingsForTestCase(final Map<String, BigDecimal> weights) {
        return bridge.getRawHoldings().entrySet()
                .stream()
                .filter(e -> weights.containsKey(e.getKey()))
                .map(e -> HoldingUtils.createAppSpecificHoldingV1x5(e.getKey(), weights.get(e.getKey()), e.getValue()))
                .collect(Collectors.toList());
    }
}
