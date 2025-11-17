package com.fintex.ce.dto;

import com.fintex.ce.config.enumeration.calculation.FixedIncomeStyleboxType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FixedIncomeStyleboxExposureDTO {

    private FixedIncomeStyleboxType boxType;
    private BigDecimal value;

}
