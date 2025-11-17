package com.fintex.ce;

import com.fintex.smclient.config.EnableFasClientLibrary;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

import static com.fintex.ce.util.validation.startup.LogRequestCheckerForPortfolioController.checkPortfolioControllerMethodsHavingHttpServletRequestParameterIfClassIsAnnotatedWithLogRequest;

@EnableFasClientLibrary
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT30S")
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class PortfolioCalculationService {

    public static void main(String[] args) {
        checkPortfolioControllerMethodsHavingHttpServletRequestParameterIfClassIsAnnotatedWithLogRequest();
        SpringApplication.run(PortfolioCalculationService.class, args);
    }

}
