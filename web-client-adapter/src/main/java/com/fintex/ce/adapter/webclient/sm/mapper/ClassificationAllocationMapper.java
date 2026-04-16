package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.model.domain.calculation.allocation.ClassificationAllocation;
import com.fintex.ce.model.domain.calculation.allocation.ClassificationAllocationType;
import com.fintex.ce.model.domain.holding.Holding;
import com.fintex.wm.commons.domain.allocation.SecurityClassificationAllocation;
import com.fintex.wm.commons.domain.classification.SecurityClassificationTypeValue;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Maps SM SecurityClassificationAllocation to PCE ClassificationAllocation. Combines levelOne and levelTwo into a
 * ClassificationAllocationType enum key.
 */
@Component
public class ClassificationAllocationMapper
    implements
      SecurityMasterResponseMapper<ClassificationAllocation, SecurityClassificationAllocation> {

  @Override
  public ClassificationAllocation map(SecurityClassificationAllocation smsResponse, Holding holding) {
    Map<ClassificationAllocationType, BigDecimal> classificationMap = Optional.ofNullable(smsResponse)
        .map(SecurityClassificationAllocation::getValues)
        .orElse(List.of())
        .stream()
        .filter(entry -> entry.getLevelOne() != null && entry.getLevelTwo() != null && entry.getValue() != null)
        .filter(entry -> toClassificationType(entry) != null)
        .collect(Collectors.toMap(
            this::toClassificationType,
            SecurityClassificationTypeValue::getValue,
            BigDecimal::add,
            () -> new EnumMap<>(ClassificationAllocationType.class)));

    ClassificationAllocation result = new ClassificationAllocation()
        .setSecurityClassificationValues(classificationMap)
        .setHoldingType(holding.getHoldingType())
        .setHoldingId(holding.getSecurityIdentifier().getId());

    Optional.ofNullable(smsResponse)
        .map(SecurityClassificationAllocation::getDataProvider)
        .ifPresent(dp -> result.setProviders(List.of(dp)));

    return result;
  }

  private ClassificationAllocationType toClassificationType(SecurityClassificationTypeValue entry) {
    return ClassificationAllocationType.fromValue(entry.getLevelOne().name() + "__" + entry.getLevelTwo().name());
  }
}