package com.fintex.ce.model.dto.command;

import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.wm.commons.domain.DataProvider;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "metric", visible = true, defaultImpl = Void.class)
@JsonTypeIdResolver(MetricTypeIdResolver.class)
public abstract class CalculationCommand {

  @Schema(description = "Calculation metric type that determines which calculation to execute")
  private CalculationMetric metric;

  @Schema(description = "Data providers to use for fetching security data; configured defaults apply when absent", example = "[\"MORNINGSTAR\"]")
  private List<DataProvider> dataProviders;
}
