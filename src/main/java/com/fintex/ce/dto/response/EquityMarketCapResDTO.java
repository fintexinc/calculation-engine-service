package com.fintex.ce.dto.response;

import com.fintex.ce.config.enumeration.calculation.EquityMarketCapType;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.dto.response.core.WarningDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class EquityMarketCapResDTO extends WarningDTO {

    private Map<EquityMarketCapType, BigDecimal> equityMarketCapitalization;

    public EquityMarketCapResDTO() {
    	
    }
    
    public EquityMarketCapResDTO(Map<EquityMarketCapType, BigDecimal> equityMarketCapitalization, List<Warning> warnings) {
        super(warnings);
        this.equityMarketCapitalization = equityMarketCapitalization;
    }
}
