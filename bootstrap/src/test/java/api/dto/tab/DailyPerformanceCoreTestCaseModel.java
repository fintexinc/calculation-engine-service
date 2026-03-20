package api.dto.tab;

import com.fintex.ce.domain.model.enumeration.Frequency;
import com.fintex.ce.domain.model.holding.Holding;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class DailyPerformanceCoreTestCaseModel {
  private BigDecimal purchaseAmount;
  private BigDecimal pac;
  private Frequency pacFrequency;
  private BigDecimal withdrawal;
  private Frequency withdrawalFrequency;
  private Holding holding;
}