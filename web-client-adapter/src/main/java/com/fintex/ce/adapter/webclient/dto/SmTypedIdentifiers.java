package com.fintex.ce.adapter.webclient.dto;

import com.fintex.sm.model.domain.SecurityIdentifier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmTypedIdentifiers {

  private String type;
  private List<SecurityIdentifier> ids;
}
