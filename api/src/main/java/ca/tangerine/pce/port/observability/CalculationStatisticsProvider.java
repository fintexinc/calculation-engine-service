package ca.tangerine.pce.port.observability;

/**
 * Reads back what {@link CalculationObservability} has recorded, as a ranked summary that can be served without an
 * external metrics backend. Separate from the recording port because the two have opposite audiences: one is called on
 * every request, this one is called by whoever is asking which calculation metric is in trouble.
 */
public interface CalculationStatisticsProvider {

  CalculationStatisticsReport statistics();
}
