package com.fintex.ce.dto.response;

import com.fintex.ce.dto.response.core.ErrorDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class CommonPerformanceDatesResDTO extends ErrorDTO {

    private LocalDate commonPerformanceStartDatePf;
    private LocalDate commonPerformanceEndDatePf;
    private LocalDate commonPerformanceStartDateBm;
    private LocalDate commonPerformanceEndDateBm;


}
