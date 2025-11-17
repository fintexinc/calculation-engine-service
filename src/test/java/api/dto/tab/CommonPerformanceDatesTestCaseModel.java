package api.dto.tab;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class CommonPerformanceDatesTestCaseModel extends CoreTestCaseModel {

    private List<Portfolio> portfolios;
    private Map<String, BigDecimal> benchmarkHoldings;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Portfolio {

        private List<CoreTestCaseModel> holdings;

    }
}

