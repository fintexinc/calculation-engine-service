package com.fintex.ce.repository.graphql.query.endpoint.classificationallocation;

import com.fintex.smclient.graphql.SecurityClassification;
import com.fintex.smclient.graphql.SecurityClassificationAllocation;
import com.fintex.smclient.graphql.SecurityClassificationAllocationQueryDefinition;
import com.fintex.smclient.graphql.SecurityClassificationQueryDefinition;
import com.fintex.smclient.graphql.SecurityClassificationTypeValue;
import com.fintex.ce.config.constant.GeneralConstants;
import com.fintex.ce.model.redis.RClassificationAllocation;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@UtilityClass
public class ClassificationAllocationEndpointUtil {

    public static <T> RClassificationAllocation getResponseCacheEntity(final T type,
                                                                       final Supplier<SecurityClassificationAllocation> supplier1,
                                                                       final Supplier<SecurityClassification> supplier2) {
        final var cacheEntity = new RClassificationAllocation();

        final SecurityClassificationAllocation supplier1Value = getOptionalValue(supplier1);
        final SecurityClassification supplier2Value = getOptionalValue(supplier2);

        Optional.ofNullable(type)
                .filter(fs -> ObjectUtils.anyNotNull(supplier1Value, supplier2Value))
                .map(fs -> getClassificationAllocation(
                        supplier1Value,
                        supplier2Value
                ))
                .ifPresent(cacheEntity::setSecurityClassificationValues);

        return cacheEntity;
    }

    private static <T> T getOptionalValue(final Supplier<T> supplier){
       return  Optional.ofNullable(supplier).map(Supplier::get).orElse(null);
    }

    private static Map<String, BigDecimal> getClassificationAllocation(final SecurityClassificationAllocation securityClassificationAllocation,
                                                                       final SecurityClassification securityClassification) {
        return Optional.ofNullable(getClassificationAllocation(securityClassificationAllocation))
                .orElse(getClassificationAllocation(securityClassification));
    }

    private static Map<String, BigDecimal> getClassificationAllocation(final SecurityClassificationAllocation securityClassificationAllocation) {
        return Optional.ofNullable(securityClassificationAllocation)
                .filter(sc -> !CollectionUtils.isEmpty(securityClassificationAllocation.getValues()))
                .map(sc -> getClassificationAllocation(sc.getValues()))
                .orElse(null);
    }

    private static Map<String, BigDecimal> getClassificationAllocation(final List<SecurityClassificationTypeValue> values) {
        return values.stream()
                .filter(v -> ObjectUtils.allNotNull(v.getValue(), v.getLevelOne(), v.getLevelTwo()))
                .filter(v -> ObjectUtils.notEqual(v.getValue().stripTrailingZeros(), BigDecimal.ZERO))
                .collect(Collectors.toMap(
                        sc -> join(sc.getLevelOne().name(), sc.getLevelTwo().name()),
                        SecurityClassificationTypeValue::getValue));
    }

    private static Map<String, BigDecimal> getClassificationAllocation(final SecurityClassification securityClassification) {
        return Optional.ofNullable(securityClassification)
                .filter(sc -> ObjectUtils.allNotNull(sc.getLevelOne(), sc.getLevelTwo()))
                .map(sc -> join(sc.getLevelOne().name(), sc.getLevelTwo().name()))
                .map(classification -> Map.of(classification, BigDecimal.ONE))
                .orElse(null);
    }

    private static String join(final String firstPart, final String secondPart) {
        return StringUtils.joinWith(GeneralConstants.DOUBLE_UNDERSCORE, firstPart, secondPart);
    }

    public static SecurityClassificationQueryDefinition getSecurityClassificationQueryDefinition() {
        return qSecurityClassifications -> {
            qSecurityClassifications.dataProvider();
            qSecurityClassifications.asOfDate();
            qSecurityClassifications.levelOne();
            qSecurityClassifications.levelTwo();
        };
    }

    public static SecurityClassificationAllocationQueryDefinition getSecurityClassificationAllocationQueryDefinition() {
        return qSecurityClassificationAllocation -> {
            qSecurityClassificationAllocation.dataProvider();
            qSecurityClassificationAllocation.asOfDate();
            qSecurityClassificationAllocation.values(value -> value.levelOne().levelTwo().value());
        };
    }

}
