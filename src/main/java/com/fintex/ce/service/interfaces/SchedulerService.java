package com.fintex.ce.service.interfaces;

import org.springframework.scheduling.annotation.Scheduled;

public interface SchedulerService {
    @Scheduled(cron = "0 0 0 1/1 * ? *")
    void preCacheExistingFDSCalls();
}
