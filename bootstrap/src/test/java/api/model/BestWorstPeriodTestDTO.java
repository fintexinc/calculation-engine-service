package api.model;

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
public class BestWorstPeriodTestDTO {

  // field used in reflection for API test case
  private Long timeIntervalPeriod;
  // field used in reflection for API test case
  private BigDecimal bestPeriodPct;
  // field used in reflection for API test case
  private LocalDate bestPeriodStartDate;
  // field used in reflection for API test case
  private LocalDate bestPeriodEndDate;
  // field used in reflection for API test case
  private BigDecimal worstPeriodPct;
  // field used in reflection for API test case
  private LocalDate worstPeriodStartDate;
  // field used in reflection for API test case
  private LocalDate worstPeriodEndDate;
  // field used in reflection for API test case
  private BigDecimal average;
  // field used in reflection for API test case
  private BigDecimal numberOFPeriods;
  // field used in reflection for API test case
  private BigDecimal pctPositive;

}
