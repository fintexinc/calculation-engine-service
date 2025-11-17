package api.dto.tab;

import com.fintex.ce.config.enumeration.Currency;
import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.config.enumeration.InterestFreq;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CoreTestCaseModel {

    private String summary;
    private String description;
    private List<DataProvider> dataProviders;
    private Map<String, BigDecimal> holdings;
    private Currency currency;
    private LocalDate gicInvestmentDate;
    private BigDecimal gicClientIntRate;
    private InterestFreq gicInterestFreq;
    private BigDecimal gicTerm;
    private String gicName;

}
