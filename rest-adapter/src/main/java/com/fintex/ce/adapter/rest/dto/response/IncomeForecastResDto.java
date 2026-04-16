package com.fintex.ce.adapter.rest.dto.response;

import com.fintex.ce.adapter.rest.dto.response.core.WarningDTO;
import com.fintex.ce.model.dto.IncomeForecastDto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Response for income-forecast metric. Contains income forecast for holdings.")
public class IncomeForecastResDto extends WarningDTO {

  @Schema(description = "Income forecast for holdings")
  private List<IncomeForecastDto> incomeForecast;

}
