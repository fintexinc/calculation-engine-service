package com.fintex.ce.dto;

import com.fintex.ce.config.enumeration.calculation.ClassificationAllocationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassificationAllocationDTO {
    private ClassificationAllocationType boxType;
    private BigDecimal value;

}
