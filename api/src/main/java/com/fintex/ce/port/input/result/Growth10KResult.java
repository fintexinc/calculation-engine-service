package com.fintex.ce.port.input.result;

import com.fintex.ce.port.input.result.core.KeyValueResult;
import com.fintex.ce.port.input.result.WarningResult;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Accessors(chain = true)
public class Growth10KResult extends WarningResult {

  private LocalDate ped;
  private LocalDate psd;
  private List<KeyValueResult> growth10k;
}
