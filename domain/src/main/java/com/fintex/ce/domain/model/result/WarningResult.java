package com.fintex.ce.domain.model.result;

import com.fintex.ce.domain.model.core.Warning;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Accessors(chain = true)
public abstract class WarningResult extends ErrorResult {

  protected List<Warning> warnings = new ArrayList<>();
}