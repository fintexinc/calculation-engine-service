package api.dto.tab;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class PeriodTestCaseModel extends CoreTestCaseModel {

  private Map<String, BigDecimal> benchmarkHoldings;
  private LocalDate customIntervalPsd;
  private LocalDate customPed;
  private Set<String> periods;

}
