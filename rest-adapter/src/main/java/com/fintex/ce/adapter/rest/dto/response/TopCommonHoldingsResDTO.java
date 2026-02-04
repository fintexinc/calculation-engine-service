package com.fintex.ce.adapter.rest.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fintex.ce.adapter.rest.dto.response.commonholdings.TopCommonHoldingsDTO;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.adapter.rest.dto.response.core.WarningDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TopCommonHoldingsResDTO extends WarningDTO {

  private List<TopCommonHoldingsDTO> commonHoldings;

  public TopCommonHoldingsResDTO(final List<TopCommonHoldingsDTO> commonHoldings, final List<Warning> warnings) {
    super(warnings);
    this.commonHoldings = commonHoldings;
  }
}
