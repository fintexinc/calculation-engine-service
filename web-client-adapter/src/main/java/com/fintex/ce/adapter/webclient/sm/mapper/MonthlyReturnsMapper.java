package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.domain.model.HoldingMonthlyReturns;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.sm.model.domain.performance.MonthlyReturns;
import com.fintex.sm.model.domain.value.DateBigDecimalValue;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Maps Security Master MonthlyReturns response to PCE HoldingMonthlyReturns domain model.
 */
@Component
public class MonthlyReturnsMapper
    implements
      SecurityMasterResponseMapper<HoldingMonthlyReturns, MonthlyReturns> {

  @Override
  public HoldingMonthlyReturns map(MonthlyReturns smsResponse, Holding holding) {
    TreeMap<LocalDate, BigDecimal> returnsMap = Optional.ofNullable(smsResponse)
        .map(MonthlyReturns::getReturns)
        .orElse(List.of())
        .stream()
        .filter(entry -> entry.getDate() != null && entry.getValue() != null)
        .collect(Collectors.toMap(
            entry -> LocalDate.parse(entry.getDate()),
            DateBigDecimalValue::getValue,
            (existing, replacement) -> existing,
            TreeMap::new));

    HoldingMonthlyReturns result = new HoldingMonthlyReturns()
        .setReturns(returnsMap)
        .setHoldingType(holding.getHoldingType())
        .setHoldingId(holding.getSecurityIdentifier().getId());

    Optional.ofNullable(smsResponse)
        .map(MonthlyReturns::getDataProvider)
        .ifPresent(dp -> result.setProvider(dp.name()));

    return result;
  }
}