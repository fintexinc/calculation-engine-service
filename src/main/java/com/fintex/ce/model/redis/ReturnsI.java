package com.fintex.ce.model.redis;

import com.fintex.ce.config.enumeration.HoldingType;
import com.fintex.ce.exception.DataErrorException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.TreeMap;

public interface ReturnsI {

    String getCurrency();
    HoldingType getHoldingType();
    TreeMap<LocalDate, BigDecimal> getReturns();
    void addError(DataErrorException error);
    List<DataErrorException> getErrors();
    boolean hasMonthlyReturnsErrors();
    List<DataErrorException> getOnlyMonthlyReturnsErrors();

}
