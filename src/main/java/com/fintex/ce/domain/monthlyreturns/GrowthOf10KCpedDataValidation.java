package com.fintex.ce.domain.monthlyreturns;

import com.fintex.ce.exception.notification.pattern.Notification;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@EqualsAndHashCode
public class GrowthOf10KCpedDataValidation extends PortfolioCpedDataValidation {

    @Override
    public void validate(LocalDate cped,LocalDate psd, LocalDate ped, Notification notification) {
        validateCpedIsBeforePsd(cped, psd, notification);
    }
}
