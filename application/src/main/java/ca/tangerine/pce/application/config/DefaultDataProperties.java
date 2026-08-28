package ca.tangerine.pce.application.config;

import ca.tangerine.wm.commons.domain.DataProvider;

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
