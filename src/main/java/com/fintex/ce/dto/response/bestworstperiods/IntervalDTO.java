package com.fintex.ce.dto.response.bestworstperiods;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IntervalDTO {

    private LocalDate startDate;
    private LocalDate endDate;

}
