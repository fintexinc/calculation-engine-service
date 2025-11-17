package com.fintex.ce.domain.monthlyreturns;

import com.fintex.ce.config.enumeration.ExceptionCode;
import lombok.EqualsAndHashCode;

import static com.fintex.ce.config.enumeration.ExceptionCode.ERR_RRC_BMPED_002;
import static com.fintex.ce.config.enumeration.ExceptionCode.ERR_RRC_BMPED_003;

@EqualsAndHashCode
public class BenchmarkCpedDataValidation extends CpedDataValidation {

    @Override
    protected ExceptionCode getCpedIsAfterPedExceptionCode() {
        return ERR_RRC_BMPED_003;
    }

    @Override
    protected ExceptionCode getCpedIsBeforePsdExceptionCode() {
        return ERR_RRC_BMPED_002;
    }
}
