package com.fintex.ce.dto.calculation;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Data
@Accessors(chain = true)
public class CommonDatesResDTO {
    private LocalDate startDate;
    private LocalDate endDate;
}
