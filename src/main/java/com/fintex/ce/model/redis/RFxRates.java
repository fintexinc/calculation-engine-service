package com.fintex.ce.model.redis;

import com.fintex.ce.model.redis.core.RedisId;
import com.fintex.smclient.dto.FxRatesDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.redis.core.RedisHash;

import java.time.LocalDate;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@RedisHash("RFxRates")
public class RFxRates extends RedisId {

    private Map<LocalDate, FxRatesDTO> fxRates;

    public RFxRates(final Map<LocalDate, FxRatesDTO> fxRates) {
        this.fxRates = fxRates;
    }

    public RFxRates() {
    }
}
