package com.fintex.ce.dto.response;

import com.fintex.ce.config.enumeration.calculation.FixedIncomeCreditQuality;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.dto.response.core.WarningDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
public class CreditQualityResDTO extends WarningDTO {

    private Map<FixedIncomeCreditQuality, BigDecimal> creditQuality;

    public CreditQualityResDTO(final Map<FixedIncomeCreditQuality, BigDecimal> creditQuality, final List<Warning> warnings) {
        super(warnings);
        this.creditQuality = creditQuality;
    }
}
