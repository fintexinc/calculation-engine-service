package ca.tangerine.pce.application.config;

import ca.tangerine.wm.commons.domain.currency.Currency;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * FX-related configuration. {@code defaultTargetCurrency} is the reporting currency that money-value calculations
 * (fees, MER, management fee) convert into before weighting / summing — every weighted-average across multi-currency
 * holdings is computed against this currency so the weights are comparable. Defaults to {@link Currency#CAD}.
 */
@Getter
@Setter
@NoArgsConstructor
@Component
@ConfigurationProperties(prefix = "calculation.fx")
public class FxProperties {

  private Currency defaultTargetCurrency = Currency.CAD;
}
