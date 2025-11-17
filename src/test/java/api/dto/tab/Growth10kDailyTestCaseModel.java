package api.dto.tab;

import com.fintex.ce.dto.holding.Holding;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Growth10kDailyTestCaseModel extends CoreTestCaseModel {
    private LocalDate customPerformanceStartDate;
    private LocalDate customPerformanceEndDate;
    private BigDecimal purchaseAmount;
    private String holding1;
    private String holding2;
    private String holding3;
    private Holding holding;
}
