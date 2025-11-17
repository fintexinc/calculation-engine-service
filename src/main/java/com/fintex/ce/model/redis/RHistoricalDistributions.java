package com.fintex.ce.model.redis;

import com.fintex.ce.config.enumeration.HoldingType;
import com.fintex.ce.model.redis.core.RedisId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.data.redis.core.RedisHash;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.TreeMap;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@RedisHash("HistoricalDistributions")
@Accessors(chain = true)
public class RHistoricalDistributions extends RedisId {

    private String currency;
    private HoldingType holdingType;

    private TreeMap<LocalDate, CapitalGainsDto> capitalGains;

    private TreeMap<LocalDate, DistributionsDto> distributions;

    @Data
    @AllArgsConstructor
    public static class DistributionsDto {
        BigDecimal domesticDividend;
        BigDecimal foreignDividend;
        BigDecimal interestIncome;

        public BigDecimal sum() {
            BigDecimal sum = BigDecimal.ZERO;
            if (Objects.nonNull(domesticDividend)) {
                sum = sum.add(domesticDividend);
            }
            if (Objects.nonNull(foreignDividend)) {
                sum = sum .add(foreignDividend);
            }
            if (Objects.nonNull(interestIncome)) {
                sum = sum.add(interestIncome);
            }
            return sum;
        }
    }

    @Data
    @AllArgsConstructor
    public static class CapitalGainsDto {
        BigDecimal capitalGains;
        BigDecimal returnOfCapital;

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
