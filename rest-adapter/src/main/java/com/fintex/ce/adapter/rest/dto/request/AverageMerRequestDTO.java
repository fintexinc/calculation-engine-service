package com.fintex.ce.adapter.rest.dto.request;

import com.fintex.ce.domain.enumeration.ParameterType;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.adapter.rest.dto.request.core.DataProviderReqDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AverageMerRequestDTO extends DataProviderReqDTO {

  private List<ParameterType> parameterTypes;

  private List<Holding> holdings;

}
