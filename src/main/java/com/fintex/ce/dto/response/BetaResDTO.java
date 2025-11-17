package com.fintex.ce.dto.response;

import com.fintex.ce.dto.response.core.PeriodResDTO;
import com.fintex.ce.dto.response.core.TimeIntervalResDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Set;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class BetaResDTO extends PeriodResDTO {

    private Set<TimeIntervalResDTO> beta;

}
