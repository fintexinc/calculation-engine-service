package com.fintex.ce.adapter.graphqlclient.endpoint.equitystyleboxexposure;

import com.fintex.smclient.graphql.StyleBoxes;
import com.fintex.smclient.graphql.StyleBoxesQueryDefinition;
import com.fintex.ce.domain.model.EquityStyleboxExposure;
import com.fintex.ce.util.StyleboxUtil;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.util.Map;

@UtilityClass
public class EquityStyleboxExposureEndpointUtil {

  public static EquityStyleboxExposure getEquityStyleboxExposure(final StyleBoxes styleBoxes,
      final EquityStyleboxExposure cacheEntity) {
    final Map<String, BigDecimal> boxValues = StyleboxUtil.getBoxValues(styleBoxes);
    cacheEntity.setBoxValues(boxValues);
    return cacheEntity;
  }

  public static StyleBoxesQueryDefinition getStyleBoxesQueryDefinition() {

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
