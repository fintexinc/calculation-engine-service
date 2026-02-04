package com.fintex.ce.application.result;

import com.fintex.ce.domain.model.IncomeForecastDto;
import com.fintex.ce.port.input.result.WarningResult;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Accessors(chain = true)
public class IncomeForecastResult extends WarningResult {

  private List<IncomeForecastDto> incomeForecast;
}
