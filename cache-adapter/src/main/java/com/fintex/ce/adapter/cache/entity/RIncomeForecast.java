package com.fintex.ce.adapter.cache.entity;

import com.fintex.smclient.graphql.PaymentFrequencyType;
import com.fintex.ce.adapter.cache.entity.core.RedisId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.redis.core.RedisHash;

import java.math.BigDecimal;
import java.util.List;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@RedisHash("IncomeForecast")
public class RIncomeForecast extends RedisId {

  private BigDecimal dividendYield;
  private PaymentFrequencyType paymentFrequencyType;
  private List<String> schedule;
  private String maturityDate;
  private String issueDate;

}
