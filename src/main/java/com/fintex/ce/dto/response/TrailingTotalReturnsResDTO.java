package com.fintex.ce.dto.response;

import com.fintex.ce.dto.response.core.PeriodResDTO;
import com.fintex.ce.dto.response.core.TimeIntervalResDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Set;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
public class TrailingTotalReturnsResDTO extends PeriodResDTO {

    private Set<TimeIntervalResDTO> trailingTotalReturn;

}
