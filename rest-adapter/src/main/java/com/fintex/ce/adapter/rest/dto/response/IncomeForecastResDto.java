package com.fintex.ce.adapter.rest.dto.response;

import com.fintex.ce.domain.dto.IncomeForecastDto;
import com.fintex.ce.adapter.rest.dto.response.core.WarningDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@NoArgsConstructor
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class IncomeForecastResDto extends WarningDTO {

  private List<IncomeForecastDto> incomeForecast;

}
