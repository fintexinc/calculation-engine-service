package com.fintex.ce.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fintex.ce.dto.holding.Holding;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public class TypeReferences {

    public static final TypeReference<Map<Holding, Map<LocalDate, BigDecimal>>> MONTHLY_RETURNS
            = new TypeReference<>() {
    };

    public static final TypeReference<Map<LocalDate, BigDecimal>> LOCAL_DATE_BIG_DECIMAL
            = new TypeReference<>() {
    };

}
