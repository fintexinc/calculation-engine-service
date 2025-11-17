package com.fintex.ce.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fintex.ce.dto.response.bestworstperiods.BestWorstPeriodDTO;
import com.fintex.ce.dto.response.core.ErrorDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

@Data
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BestWorstPeriodsResponseDTO extends ErrorDTO {

    @JsonProperty("performanceEndDate")
    protected LocalDate ped;
    @JsonProperty("performanceStartDate")
    protected LocalDate psd;

    private BestWorstPeriodDTO bestWorstPeriods;

}
