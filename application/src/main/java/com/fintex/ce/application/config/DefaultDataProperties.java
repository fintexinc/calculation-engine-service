package com.fintex.ce.application.config;

import com.fintex.sm.model.DataProvider;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Component
@ConfigurationProperties(prefix = "default")
public class DefaultDataProperties {

  private List<DataProvider> dataProviders;
}
