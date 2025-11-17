package com.fintex.ce.model.redis.averagemer;

import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.model.redis.core.RedisId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.data.redis.core.RedisHash;

import java.math.BigDecimal;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("RAverageMer")
@Accessors(chain = true)
public class RAverageMer extends RedisId {

    //for now we have one DTO for both funds and etfs to simplify work with different types of holdings we have in the system. So in one time we will have only half fields filled depends on etf or fund holding
    private BigDecimal mer;
    private BigDecimal actualManagementFee;
    private DataProvider merProvider;
    private DataProvider actualManagementFeeProvider;

    private BigDecimal netExpenseRatio;
    private BigDecimal grossExpenseRatio;
    private DataProvider netExpenseRatioProvider;
    private DataProvider grossExpenseRatioProvider;

}
