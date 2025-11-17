package com.fintex.ce.dto.response;

import com.fintex.ce.config.enumeration.calculation.FixedIncomeSectorType;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.dto.response.core.WarningDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FixedIncomeSectorResDTO extends WarningDTO {

    private Map<FixedIncomeSectorType, BigDecimal> fixedIncomeSector;

    public FixedIncomeSectorResDTO() {

    }

    public FixedIncomeSectorResDTO(final Map<FixedIncomeSectorType, BigDecimal> fixedIncomeSector,
                                   final List<Warning> warnings) {
        super(warnings);
        this.fixedIncomeSector = fixedIncomeSector;
    }

}
