package ca.tangerine.pce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class PortfolioCalculationEngineApplication {

  public static void main(String[] args) {
    SpringApplication.run(PortfolioCalculationEngineApplication.class, args);
  }

}
