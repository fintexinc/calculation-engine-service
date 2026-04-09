package com.fintex.ce.adapter.rest.dto.response;

import com.fintex.ce.adapter.rest.dto.response.commonholdings.TopCommonHoldingsDTO;
import com.fintex.ce.adapter.rest.dto.response.core.WarningDTO;
import com.fintex.ce.domain.model.core.Warning;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Response for top-common-holdings metric. Contains top common holdings shared across portfolio funds.")
public class TopCommonHoldingsResDTO extends WarningDTO {

  @Schema(description = "Top common holdings shared across portfolio funds")
  private List<TopCommonHoldingsDTO> commonHoldings;

  public TopCommonHoldingsResDTO(final List<TopCommonHoldingsDTO> commonHoldings, final List<Warning> warnings) {
    super(warnings);
    this.commonHoldings = commonHoldings;
  }
}
