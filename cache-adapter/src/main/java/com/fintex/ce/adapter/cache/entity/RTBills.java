package com.fintex.ce.adapter.cache.entity;

import com.fintex.ce.domain.enumeration.Currency;
import com.fintex.ce.adapter.cache.entity.core.RedisId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString
@RedisHash("RTBills")
@NoArgsConstructor
public class RTBills extends RedisId {

  @Indexed
  private Currency currency;

  private Map<LocalDate, BigDecimal> monthlyReturns;

  public RTBills(final Currency currency, final Map<LocalDate, BigDecimal> monthlyReturns) {
    this.currency = currency;
    this.monthlyReturns = monthlyReturns;
  }
}
