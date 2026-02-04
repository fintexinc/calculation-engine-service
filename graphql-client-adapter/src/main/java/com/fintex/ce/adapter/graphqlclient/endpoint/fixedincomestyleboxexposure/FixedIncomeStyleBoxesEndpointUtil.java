package com.fintex.ce.adapter.graphqlclient.endpoint.fixedincomestyleboxexposure;

import com.fintex.smclient.graphql.FixedIncomeStyleBoxValue;
import com.fintex.smclient.graphql.FixedIncomeStyleBoxes;
import com.fintex.smclient.graphql.FixedIncomeStyleBoxesQueryDefinition;
import com.fintex.smclient.graphql.StyleBoxes;
import com.fintex.ce.domain.model.FixedIncomeStyleboxExposure;
import com.fintex.ce.util.StyleboxUtil;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

@UtilityClass
public class FixedIncomeStyleBoxesEndpointUtil {

  public static FixedIncomeStyleboxExposure getREquityStyleboxExposure(final StyleBoxes styleBoxes,
      final FixedIncomeStyleboxExposure cacheEntity) {
    final Map<String, BigDecimal> boxValues = StyleboxUtil.getBoxValues(styleBoxes);
    cacheEntity.setBoxValues(boxValues);
    return cacheEntity;
  }

  public static FixedIncomeStyleboxExposure getREquityStyleboxExposure(final FixedIncomeStyleBoxes styleBoxes,
      final FixedIncomeStyleboxExposure cacheEntity) {
    Map<String, BigDecimal> boxValuesMap = styleBoxes.getBoxValues().stream()
        .filter(boxValue -> boxValue != null && boxValue.getStyleBoxType() != null)
        .collect(Collectors.toMap(
            boxStyleType -> boxStyleType.getStyleBoxType().toString(),
            FixedIncomeStyleBoxValue::getValue));
    cacheEntity.setBoxValues(boxValuesMap);
    return cacheEntity;
  }

  public static FixedIncomeStyleBoxesQueryDefinition getStyleBoxesQueryDefinition() {

    return qStyleboxes -> {
      qStyleboxes.dataProvider();
      qStyleboxes.asOfDate();
      qStyleboxes.boxValues(
          qBoxValue -> {
            qBoxValue.styleBoxType();
            qBoxValue.value();
          });
    };
  }

}
