package com.fintex.ce.adapter.rest.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fintex.ce.adapter.rest.dto.response.core.KeyValueDTO;
import com.fintex.ce.adapter.rest.dto.response.core.WarningDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class Growth10KResDTO extends WarningDTO {

  @JsonProperty("performanceEndDate")
  protected LocalDate ped;

  @JsonProperty("performanceStartDate")
  protected LocalDate psd;

  private List<KeyValueDTO> growth10k;

}
