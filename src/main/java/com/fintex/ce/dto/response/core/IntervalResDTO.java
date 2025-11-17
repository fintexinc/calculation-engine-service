package com.fintex.ce.dto.response.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IntervalResDTO {

    private LocalDate key;
    private BigDecimal value;

}
