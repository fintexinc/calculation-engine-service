package com.fintex.ce.dto.response.bestworstperiods;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PeriodValueDTO {

    private Long period;
    private BigDecimal value;

}
