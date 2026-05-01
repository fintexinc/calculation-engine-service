package com.fintex.ce.model.domain.calculation;

import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.error.Notification;

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

/**
 * Base class for all PCE calculation data models that carry provider and validation information. Errors accumulated
 * while assembling the data are kept as {@link Notification}s so the full error context (code, message, severity,
 * holding id) is available at the REST boundary without further lookups.
 *
 * @param <T>
 *          self-type kept for backward compatibility with code that referenced the prior CRTP signature
 */
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public abstract class BaseCalculationData<T extends BaseCalculationData<T>> {

  // todo remove and just use SecurityIdentifier. TMI-275
  private String holdingId;
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
