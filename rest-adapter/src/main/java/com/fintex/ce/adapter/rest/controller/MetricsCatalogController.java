package com.fintex.ce.adapter.rest.controller;

import com.fintex.ce.application.calculation.metric.MetricCatalogService;
import com.fintex.ce.model.dto.MetricInfo;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import lombok.RequiredArgsConstructor;

/**
 * REST endpoint for the metrics catalog. Provides discovery of all available metrics that the calculation engine
 * supports.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/metrics")
@Tag(name = "Metrics Catalog", description = "Discovery endpoint for available calculation metrics")
public class MetricsCatalogController {

  private final MetricCatalogService metricCatalogService;

  @Operation(summary = "List all available metrics", description = "Returns a catalog of every metric this engine supports, including metric ID, human-readable name, and category.")
  @ApiResponse(responseCode = "200", description = "Catalog of available metrics", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = MetricInfo.class)))
  @GetMapping("/catalog")
  public List<MetricInfo> getCatalog() {
    return metricCatalogService.getCatalog();
  }
}
