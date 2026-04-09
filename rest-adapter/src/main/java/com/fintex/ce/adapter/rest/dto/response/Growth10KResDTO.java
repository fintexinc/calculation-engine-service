package com.fintex.ce.adapter.rest.dto.response;

import com.fintex.ce.adapter.rest.dto.response.core.KeyValueDTO;
import com.fintex.ce.adapter.rest.dto.response.core.WarningDTO;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Response for growth-of-10k metric. Contains growth of a hypothetical $10,000 investment over time.")
public class Growth10KResDTO extends WarningDTO {

  @Schema(description = "Performance end date")
  @JsonProperty("performanceEndDate")
  protected LocalDate ped;

  @Schema(description = "Performance start date")
  @JsonProperty("performanceStartDate")
  protected LocalDate psd;

  @Schema(description = "Growth of $10K data points over time")
  private List<KeyValueDTO> growth10k;

}
