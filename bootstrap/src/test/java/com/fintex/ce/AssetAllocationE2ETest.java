package com.fintex.ce;

import org.junit.jupiter.api.Tag;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("e2e")
class AssetAllocationE2ETest extends AbstractPortfolioCalculationE2ETest {

  @Override
  protected String metricPath() {
    return "asset-allocations";
  }

  @Override
  protected String requestBodyForSmsUnavailableScenario() {
    return """
        {
          "metric": "asset-allocations",
          "holdings": [
            {
              "holdingType": "ETF_CANADA",
              "value": 50000,
              "securityIdentifier": { "id": "XBAL", "idType": "TICKER" }
            },
            {
              "holdingType": "ETF_CANADA",
              "value": 50000,
              "securityIdentifier": { "id": "VCNS", "idType": "TICKER" }
            }
          ],
          "dataProviders": ["MORNINGSTAR"]
        }
        """;
  }

  @Override
  protected String requestBodyForPositiveSmsScenario() {
    return """
        {
          "metric": "asset-allocations",
          "holdings": [
            {
              "holdingType": "ETF_CANADA",
              "value": 50000,
              "securityIdentifier": { "id": "XBAL", "idType": "TICKER" }
            }
          ],
          "dataProviders": ["MORNINGSTAR"]
        }
        """;
  }

  @Override
  protected String smsPositiveResponseBody() {
    return """
        [
          {
            "identifier": { "id": "XBAL", "idType": "TICKER" },
            "data": {
              "allocation": [
                { "name": "EQUITY", "value": 60.0 },
                { "name": "FIXED_INCOME", "value": 30.0 },
                { "name": "CASH", "value": 10.0 }
              ],
              "dataProvider": "MORNINGSTAR"
            }
          }
        ]
        """;
  }

  @Override
  protected String requestBodyForMismatchedMetricScenario() {
    return """
        {"metric": "sharpe-ratio", "holdings": [], "currency": "CAD"}
        """;
  }

  @Override
  protected void assertPositiveResponseBody(String responseBody) {
    assertThat(responseBody).contains("assetAllocation");
    assertThat(responseBody).contains("\"errors\":null");
  }
}
