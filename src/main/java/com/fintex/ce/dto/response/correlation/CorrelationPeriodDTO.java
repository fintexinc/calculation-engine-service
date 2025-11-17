package com.fintex.ce.dto.response.correlation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CorrelationPeriodDTO {

    private String period;
    private String key;
    private List<CorrelationKeyValueDTO> correlations;

}
