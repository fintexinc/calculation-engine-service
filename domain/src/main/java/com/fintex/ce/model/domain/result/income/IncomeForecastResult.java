package com.fintex.ce.model.domain.result.income;

import com.fintex.ce.model.domain.result.WarningResult;
import com.fintex.ce.model.dto.IncomeForecastDto;

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
public class IncomeForecastResult extends WarningResult {

  private List<IncomeForecastDto> incomeForecast;
}
