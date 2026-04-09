package com.fintex.ce.domain.model.result;

import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.result.core.KeyValueResult;

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
public class AnnualReturnResult<T> extends DatesResult {

  private List<KeyValueResult<T>> annualReturns;
  private List<Warning> warnings;
}
