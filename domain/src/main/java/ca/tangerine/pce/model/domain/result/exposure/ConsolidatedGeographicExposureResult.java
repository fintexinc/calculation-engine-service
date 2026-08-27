package ca.tangerine.pce.model.domain.result.exposure;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Result of the consolidated {@code geographic-exposure} metric.
 *
 * <p>
 * Extends {@link GeographicExposureResult} without adding fields, so the JSON key stays {@code geographicExposure} —
 * the same key the two per-sleeve geographic metrics use. Reusing the key was chosen over minting a distinct one (e.g.
 * {@code consolidatedGeographicExposure}) because all three metrics answer the same shape of question and the metric is
 * already identified by the endpoint path and by the composite response key; a second name for an identical payload
 * would force the client to branch on it for no gain.
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ConsolidatedGeographicExposureResult extends GeographicExposureResult {
}
