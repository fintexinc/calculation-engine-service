package com.fintex.ce.dto.response;

import com.fintex.ce.dto.YieldDTO;
import com.fintex.ce.dto.response.core.WarningDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class YieldResDto extends WarningDTO {

    private BigDecimal yield;

}
