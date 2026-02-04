package com.fintex.ce.dto;

import com.fintex.ce.domain.model.holding.Holding;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class MapOfHoldingCase<E> {

  private List<TPair<Holding, E>> request;
  private List<TPair<Holding, E>> expected;

  public Map<Holding, E> requestToMap() {
    return toMap(request);
  }

  public Map<Holding, E> expectedToMap() {
    return toMap(expected);
  }

  private Map<Holding, E> toMap(List<TPair<Holding, E>> request) {
    return request.stream().collect(Collectors.toMap(TPair::getKey, TPair::getValue));
  }

}
