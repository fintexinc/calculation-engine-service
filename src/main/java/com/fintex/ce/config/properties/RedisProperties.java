package com.fintex.ce.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "redis")
@Data
public class RedisProperties {

    private String host;
    private int port;
    private String password;
    private Integer timeout;
    private boolean legacyVersion;
    private String username;

}
