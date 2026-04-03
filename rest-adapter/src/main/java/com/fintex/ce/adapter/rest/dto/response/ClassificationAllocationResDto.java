package com.fintex.ce.adapter.rest.dto.response;

import com.fintex.ce.adapter.rest.dto.response.core.WarningDTO;
import com.fintex.ce.domain.model.calculation.ClassificationAllocationType;
import com.fintex.ce.domain.model.core.Warning;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Response for classification-allocation metric. Contains classification-based allocation breakdown.")
public class ClassificationAllocationResDto extends WarningDTO {

  @Schema(description = "Allocation percentages by classification type")
  private Map<ClassificationAllocationType, BigDecimal> classificationAllocation;

  public ClassificationAllocationResDto(Map<ClassificationAllocationType, BigDecimal> classificationAllocation,
      List<Warning> warnings) {
    super(warnings);
    this.classificationAllocation = classificationAllocation;
  }
}
