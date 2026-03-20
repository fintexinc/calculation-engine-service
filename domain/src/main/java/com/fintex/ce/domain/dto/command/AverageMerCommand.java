package com.fintex.ce.domain.dto.command;

import com.fintex.ce.domain.model.enumeration.ParameterType;
import com.fintex.ce.domain.model.holding.Holding;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Accessors(chain = true)
public class AverageMerCommand extends DataProviderCommand {
  private List<ParameterType> parameterTypes;
  private List<Holding> holdings;
}
