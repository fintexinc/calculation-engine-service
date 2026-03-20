package com.fintex.ce.adapter.rest.dto.response;

import com.fintex.ce.domain.model.enumeration.DailyResultType;
import com.fintex.ce.domain.model.calculation.ReturnsAnsDistributionReceived;
import com.fintex.ce.adapter.rest.dto.response.core.WarningDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class DailyPerformanceResDTO extends WarningDTO {

  public static final String AGGREGATED_RESULT = "AGGREGATED_RESULT";

  private Map<DailyResultType, Map<String, ReturnsAnsDistributionReceived>> dailyPerformance = new HashMap<>();
  private LocalDate performanceStartDate;
  private LocalDate performanceEndDate;

  public DailyPerformanceResDTO add(DailyResultType type, Map<String, ReturnsAnsDistributionReceived> result) {
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
    ReturnsAnsDistributionReceived aggregatedRes = sumAllAdditionalFields(dailyPerformanceAndDistributionReceived);
    aggregatedRes.setReturns(aggregatedReturns);

    dailyPerformanceAndDistributionReceived.put(AGGREGATED_RESULT, aggregatedRes);
  }

  private TreeMap<LocalDate, BigDecimal> aggregateReturns(
      Map<String, ReturnsAnsDistributionReceived> dailyPerformanceAndDistributionReceived) {
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

  private ReturnsAnsDistributionReceived sumAllAdditionalFields(
      Map<String, ReturnsAnsDistributionReceived> dailyPerformanceAndDistributionReceived) {
    BigDecimal distributionReceived = BigDecimal.ZERO;
    BigDecimal totalContribution = BigDecimal.ZERO;
    BigDecimal totalWithdrawal = BigDecimal.ZERO;
    BigDecimal subsequentContribution = BigDecimal.ZERO;

    for (Map.Entry<String, ReturnsAnsDistributionReceived> entry : dailyPerformanceAndDistributionReceived.entrySet()) {
      ReturnsAnsDistributionReceived res = entry.getValue();

      distributionReceived = distributionReceived.add(res.getDistributionReceived());
      totalContribution = totalContribution.add(res.getTotalContribution());
      totalWithdrawal = totalWithdrawal.add(res.getTotalWithdrawal());
      subsequentContribution = subsequentContribution.add(res.getSubsequentContribution());
    }

    var aggregatedRes = new ReturnsAnsDistributionReceived();
    aggregatedRes.setDistributionReceived(distributionReceived);
    aggregatedRes.setTotalContribution(totalContribution);
    aggregatedRes.setTotalWithdrawal(totalWithdrawal);
    aggregatedRes.setSubsequentContribution(subsequentContribution);
    return aggregatedRes;
  }

}
