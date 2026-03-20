package com.fintex.ce.domain.model.result;

import com.fintex.ce.domain.dto.IncomeForecastDto;
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
