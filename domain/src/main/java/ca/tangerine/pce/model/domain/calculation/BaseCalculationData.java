package ca.tangerine.pce.model.domain.calculation;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import ca.tangerine.wm.commons.domain.DataProvider;
import ca.tangerine.wm.commons.error.Notification;

/**
 * Base class for all PCE calculation data models that carry provider and validation information. Errors accumulated
 * while assembling the data are kept as {@link Notification}s so the full error context (code, message, severity) is
 * available at the REST boundary without further lookups.
 */
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public abstract class BaseCalculationData {

  @Builder.Default
  private List<DataProvider> providers = new ArrayList<>();
  @Builder.Default
  private List<Notification> errors = new ArrayList<>();

  public boolean hasErrors() {
    return errors != null && !errors.isEmpty();
  }

  public void addError(Notification error) {
    if (errors == null) {
      errors = new ArrayList<>();
    }
    errors.add(error);
  }

}
