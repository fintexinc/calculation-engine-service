package com.fintex.ce.dto.response.core;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class WarningDTO extends ErrorDTO {

    protected List<Warning> warnings;

    public WarningDTO(List<Warning> warnings) {
        this.warnings = warnings;
    }

    public WarningDTO() {
        warnings = new ArrayList<>();
    }
}
