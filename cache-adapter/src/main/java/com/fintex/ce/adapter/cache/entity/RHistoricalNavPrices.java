package com.fintex.ce.adapter.cache.entity;

import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.adapter.cache.entity.core.RedisId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.data.redis.core.RedisHash;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@RedisHash("HistoricalNavPrices")
@Accessors(chain = true)
public class RHistoricalNavPrices extends RedisId implements ReturnsI {

  private String currency;
  private HoldingType holdingType;
  private TreeMap<LocalDate, BigDecimal> returns;
  private List<LocalDate> missedMonthData = new ArrayList<>();
  private List<LocalDate> missedDates = new ArrayList<>();
}
