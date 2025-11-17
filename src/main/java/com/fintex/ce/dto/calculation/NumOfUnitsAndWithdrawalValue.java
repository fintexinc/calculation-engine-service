package com.fintex.ce.dto.calculation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NumOfUnitsAndWithdrawalValue {
    private BigDecimal numbUnits;
    private BigDecimal withdrawalValue;
}
