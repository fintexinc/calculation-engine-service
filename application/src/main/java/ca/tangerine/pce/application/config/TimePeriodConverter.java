package ca.tangerine.pce.application.config;

import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import ca.tangerine.wm.commons.domain.enumeration.TimePeriod;

/**
 * Lets {@code application.yml} keep stating a period as its length in months.
 *
 * <p>
 * The {@code @ConfigurationProperties} binder converts through Spring's conversion service rather than Jackson, so it
 * never reaches {@link TimePeriod#fromJson(String)} and would otherwise accept only the constant name — {@code 240} in
 * yml would fail to bind while {@code TWENTY_YR} worked. Both forms bind, which keeps the existing
 * {@code default.periods} values valid exactly as written and leaves in-flight branches that only add a number to them
 * untouched.
 */
@Component
@ConfigurationPropertiesBinding
public class TimePeriodConverter implements Converter<String, TimePeriod> {

  @Override
  public TimePeriod convert(final String source) {
    return TimePeriod.fromJson(source);
  }
}
