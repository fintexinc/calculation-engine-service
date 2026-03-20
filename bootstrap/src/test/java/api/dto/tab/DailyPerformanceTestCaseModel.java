package api.dto.tab;

import com.fintex.ce.domain.model.enumeration.Currency;
import com.fintex.ce.domain.model.holding.Holding;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class DailyPerformanceTestCaseModel extends CoreTestCaseModel {
  private String description;
  private LocalDate startDate;
  private LocalDate endDate;
  private Currency currency;
  private String holding1;
  private String holding2;
  private String holding3;
  private Map<String, DailyPerformanceCoreTestCaseModel> holdingDetails;
  private Holding holding;
}