package com.fintex.ce.adapter.rest.dto.returns;

import com.fintex.ce.adapter.rest.dto.WarningDTO;
import com.fintex.ce.model.domain.calculation.returns.ReturnsAndDistributionReceived;
import com.fintex.ce.model.domain.enumeration.DailyResultType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class DailyPerformanceResDTO extends WarningDTO {

  public static final String AGGREGATED_RESULT = "AGGREGATED_RESULT";

  private Map<DailyResultType, Map<String, ReturnsAndDistributionReceived>> dailyPerformance = new HashMap<>();
  private LocalDate performanceStartDate;
  private LocalDate performanceEndDate;

  public DailyPerformanceResDTO add(DailyResultType type, Map<String, ReturnsAndDistributionReceived> result) {
    this.dailyPerformance.put(type, result);
    return this;
  }

  public void aggregate() {
    for (DailyResultType type : DailyResultType.values()) {
      addAggregatedReturnsFor(type);
    }
  }

  private void addAggregatedReturnsFor(DailyResultType reinvestWithPacAndWithdrawal) {
    var dailyPerformanceAndDistributionReceived = this.dailyPerformance.get(reinvestWithPacAndWithdrawal);

    TreeMap<LocalDate, BigDecimal> aggregatedReturns = aggregateReturns(dailyPerformanceAndDistributionReceived);
    ReturnsAndDistributionReceived aggregatedRes = sumAllAdditionalFields(dailyPerformanceAndDistributionReceived);
    aggregatedRes.setReturns(aggregatedReturns);

    dailyPerformanceAndDistributionReceived.put(AGGREGATED_RESULT, aggregatedRes);
  }

  private TreeMap<LocalDate, BigDecimal> aggregateReturns(
      Map<String, ReturnsAndDistributionReceived> dailyPerformanceAndDistributionReceived) {
    var returns = dailyPerformanceAndDistributionReceived
        .entrySet()
        .stream()
        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getReturns()));

    var aggregatedReturns = new TreeMap<LocalDate, BigDecimal>();
    returns.forEach((key, value) -> {
      value.forEach((k, v) -> aggregatedReturns.merge(k, v, BigDecimal::add));
    });
    return aggregatedReturns;
  }

  private ReturnsAndDistributionReceived sumAllAdditionalFields(
      Map<String, ReturnsAndDistributionReceived> dailyPerformanceAndDistributionReceived) {
    BigDecimal distributionReceived = BigDecimal.ZERO;
    BigDecimal totalContribution = BigDecimal.ZERO;
    BigDecimal totalWithdrawal = BigDecimal.ZERO;
    BigDecimal subsequentContribution = BigDecimal.ZERO;

    for (Map.Entry<String, ReturnsAndDistributionReceived> entry : dailyPerformanceAndDistributionReceived.entrySet()) {
      ReturnsAndDistributionReceived res = entry.getValue();

      distributionReceived = distributionReceived.add(res.getDistributionReceived());
      totalContribution = totalContribution.add(res.getTotalContribution());
      totalWithdrawal = totalWithdrawal.add(res.getTotalWithdrawal());
      subsequentContribution = subsequentContribution.add(res.getSubsequentContribution());
    }

    var aggregatedRes = new ReturnsAndDistributionReceived();
    aggregatedRes.setDistributionReceived(distributionReceived);
    aggregatedRes.setTotalContribution(totalContribution);
    aggregatedRes.setTotalWithdrawal(totalWithdrawal);
    aggregatedRes.setSubsequentContribution(subsequentContribution);
    return aggregatedRes;
  }

}
