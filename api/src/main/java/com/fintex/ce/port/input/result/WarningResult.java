package com.fintex.ce.port.input.result;

import com.fintex.ce.domain.model.core.Warning;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Accessors(chain = true)
public abstract class WarningResult extends ErrorResult {

  protected List<Warning> warnings = new ArrayList<>();
}