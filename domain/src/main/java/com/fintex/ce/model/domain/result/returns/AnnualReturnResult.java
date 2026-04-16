package com.fintex.ce.model.domain.result.returns;

import com.fintex.ce.model.domain.result.DatesResult;
import com.fintex.ce.model.domain.result.KeyValueResult;
import com.fintex.ce.model.error.Warning;

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
