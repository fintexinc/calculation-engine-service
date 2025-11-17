package com.fintex.ce.dto.response;

import com.fintex.ce.dto.response.core.PeriodResDTO;
import com.fintex.ce.dto.response.correlation.CorrelationPeriodDTO;
import com.fintex.ce.dto.response.correlation.HoldingsKeyDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CorrelationResDTO extends PeriodResDTO {

    private List<HoldingsKeyDTO> holdingsKey;
    private List<CorrelationPeriodDTO> correlationPeriods;

}
