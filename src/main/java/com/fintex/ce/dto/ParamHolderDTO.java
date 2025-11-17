package com.fintex.ce.dto;

import com.fintex.ce.config.enumeration.Currency;
import com.fintex.ce.dto.holding.Holding;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Used to transfer objects between services
 */
@Data
public class ParamHolderDTO {

    // not mandatory
    private Currency currency;

    private Map<Holding, BigDecimal> allocations;

    public ParamHolderDTO() {
    }

    public ParamHolderDTO(final Currency currency) {
        this.currency = currency;
    }

    public ParamHolderDTO(final Map<Holding, BigDecimal> allocations) {
        this.allocations = allocations;
    }
}
