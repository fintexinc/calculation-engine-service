package com.fintex.ce.adapter.webclient.dto;

import com.fintex.sm.model.domain.SecurityIdentifier;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SmAttributeResponse<T> {

  private SecurityIdentifier identifier;
  private T data;
}
