package com.fintex.ce.dto;

import com.fintex.ce.config.enumeration.calculation.EquityStyleboxType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquityStyleboxExposureDTO {

    private EquityStyleboxType boxType;
    private BigDecimal value;

}
