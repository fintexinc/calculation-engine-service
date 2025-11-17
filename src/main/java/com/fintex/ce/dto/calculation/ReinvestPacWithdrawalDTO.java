package com.fintex.ce.dto.calculation;

import com.fintex.ce.config.enumeration.Frequency;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

public class ReinvestPacWithdrawalDTO {
    @Getter
    @Setter
    private BigDecimal pac;
    @Getter
    private Frequency pacFreq;
    @Getter
    private BigDecimal pacIndex;
    @Getter
    private BigDecimal withdrawal;
    @Getter
    private Frequency withdrawalFrequency;
    @Getter
    private boolean reinvest;

    public ReinvestPacWithdrawalDTO(BigDecimal pac, Frequency pacFreq, BigDecimal pacIndex, BigDecimal withdrawal, Frequency withdrawalFrequency, boolean reinvest) {
        this.pac = pac;
        this.pacFreq = pacFreq;
        this.pacIndex = pacIndex;
        this.withdrawal = withdrawal;
        this.withdrawalFrequency = withdrawalFrequency;
        this.reinvest = reinvest;
    }
}
