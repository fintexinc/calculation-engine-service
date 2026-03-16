package com.fintex.ce;

import com.fintex.smclient.config.EnableSmClientLibrary;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

import static com.fintex.ce.util.validation.startup.LogRequestCheckerForPortfolioController.checkPortfolioControllerMethodsHavingHttpServletRequestParameterIfClassIsAnnotatedWithLogRequest;

@EnableScheduling
@EnableSmClientLibrary
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class PortfolioCalculationService {

  public static void main(String[] args) {
    checkPortfolioControllerMethodsHavingHttpServletRequestParameterIfClassIsAnnotatedWithLogRequest();
    SpringApplication.run(PortfolioCalculationService.class, args);
  }

}
