package com.fintex.ce.domain.monthlyreturns;

import com.fintex.ce.config.enumeration.ExceptionCode;
import lombok.EqualsAndHashCode;

import static com.fintex.ce.config.enumeration.ExceptionCode.ERR_RRC_BMPSD_002;
import static com.fintex.ce.config.enumeration.ExceptionCode.ERR_RRC_BMPSD_003;

@EqualsAndHashCode
public class BenchmarkCpsdDataValidation extends CpsdDataValidation {

    @Override
    protected ExceptionCode getCpsdIsBeforePsdExceptionCode() {
        return ERR_RRC_BMPSD_002;
    }

    @Override
    protected ExceptionCode getCpsdIsAfterPedExceptionCode() {
        return ERR_RRC_BMPSD_003;
    }
}
