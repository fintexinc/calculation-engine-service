package com.fintex.ce.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("job.cache-warm-up")
public class CacheWarmUpProperties {

    private int minNumberOfRecords;
    private int maxNumberOfRecords;
    private int minNumberOfRecordUsages;
    private int percentageFactor;

}
