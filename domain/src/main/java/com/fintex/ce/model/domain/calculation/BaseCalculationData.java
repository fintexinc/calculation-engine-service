package com.fintex.ce.model.domain.calculation;

import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.error.Notification;

import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Base class for all PCE calculation data models that carry provider and validation information. Errors accumulated
 * while assembling the data are kept as {@link Notification}s so the full error context (code, message, severity,
 * holding id) is available at the REST boundary without further lookups.
 *
 * @param <T>
 *          self-type for fluent setter chaining in subclasses
 */
@Getter
@EqualsAndHashCode
@ToString
public abstract class BaseCalculationData<T extends BaseCalculationData<T>> {

  // todo remove and just use SecurityIdentifier. TMI-275
  private String holdingId;
  private List<DataProvider> providers = new ArrayList<>();
  private List<Notification> errors = new ArrayList<>();

  @SuppressWarnings("unchecked")
  public T setHoldingId(String holdingId) {
    this.holdingId = holdingId;
    return (T) this;
  }

  @SuppressWarnings("unchecked")
  public T setProviders(List<DataProvider> providers) {
    this.providers = providers;
    return (T) this;
  }

  @SuppressWarnings("unchecked")
  public T setErrors(List<Notification> errors) {
    this.errors = errors;
    return (T) this;
  }

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
