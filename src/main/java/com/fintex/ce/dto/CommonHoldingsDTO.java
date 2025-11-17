package com.fintex.ce.dto;

import com.google.common.base.Strings;
import com.fintex.ce.dto.calculation.HoldingAggregatorDTO;
import com.fintex.ce.dto.holding.Holding;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static com.fintex.ce.service.impl.calculation.CommonHoldingsServiceImpl.EQUITY_TYPE;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CommonHoldingsDTO {

    private String name;
    private String companyName;
    private String type;
    private BigDecimal value;
    private List<CommonHoldingsDTO> underlyingHoldings;
    private String ticker;
    private String exchangeCode;

    private Holding holding;
    private BigDecimal weight;

    //This field is using only for identifying GIC holdings, as it is possible that all parameters of GIC are equals but them aren't same
    private UUID uuid = null;

    public CommonHoldingsDTO(final String companyName, final String type, final BigDecimal value,
                             final String ticker, final String exchangeCode) {
        this.companyName = companyName;
        this.type = type;
        this.value = value;
        this.ticker = ticker;
        this.exchangeCode = exchangeCode;
    }

    public HoldingAggregatorDTO aggregator() {
        if (EQUITY_TYPE.equalsIgnoreCase(type) && !Strings.isNullOrEmpty(companyName)) {
            return new HoldingAggregatorDTO(null, companyName, uuid);
        }
        return new HoldingAggregatorDTO(name, null, uuid);
    }

}
