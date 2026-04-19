package com.fintex.ce.adapter.rest.dto.income;

import com.fintex.ce.adapter.rest.dto.WarningDTO;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
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
@Schema(description = "Response for yield metric. Contains portfolio yield value.")
public class YieldResDto extends WarningDTO {

  @Schema(description = "Portfolio yield value")
  private BigDecimal yield;

}
