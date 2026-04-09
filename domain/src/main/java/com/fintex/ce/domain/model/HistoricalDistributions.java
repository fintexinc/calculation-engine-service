package com.fintex.ce.domain.model;

import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.TreeMap;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@Accessors(chain = true)
public class HistoricalDistributions extends BaseCalculationData<HistoricalDistributions> {

  private String currency;
  private FinancialInstrumentType holdingType;
  private TreeMap<LocalDate, CapitalGainsDto> capitalGains;
  private TreeMap<LocalDate, DistributionsDto> distributions;

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class DistributionsDto {
    private BigDecimal domesticDividend;
    private BigDecimal foreignDividend;
    private BigDecimal interestIncome;

    public BigDecimal sum() {
      BigDecimal sum = BigDecimal.ZERO;
      if (Objects.nonNull(domesticDividend)) {
        sum = sum.add(domesticDividend);
      }
      if (Objects.nonNull(foreignDividend)) {
        sum = sum.add(foreignDividend);
      }
      if (Objects.nonNull(interestIncome)) {
        sum = sum.add(interestIncome);
      }
      return sum;
    }
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class CapitalGainsDto {
    private BigDecimal capitalGains;
    private BigDecimal returnOfCapital;

    public BigDecimal sum() {
      BigDecimal sum = BigDecimal.ZERO;
      if (Objects.nonNull(capitalGains)) {
        sum = sum.add(capitalGains);
      }
      if (Objects.nonNull(returnOfCapital)) {
        sum = sum.add(returnOfCapital);
      }
      return sum;
    }
  }

}
