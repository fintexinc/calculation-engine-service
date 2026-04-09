package com.fintex.ce.adapter.rest.validation.chainofresponsibility;

import com.fintex.ce.domain.exception.ReqValidationException;

import java.util.LinkedList;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public abstract class ReqValidation {

  protected LinkedList<ReqValidation> validations;

  public ReqValidation linkWith(final ReqValidation next) {
    validations.add(next);
    return this;
  }

  public void validate() {
    final LinkedList<ReqValidationException> exceptions = new LinkedList<>();
    validations.forEach(reqValidation -> {
      try {
        reqValidation.check();
      } catch (ReqValidationException e) {
        exceptions.add(e);
      }
    });
    if (!exceptions.isEmpty()) {
      throw new ReqValidationException(exceptions);
    }
  }

  protected abstract void check();

  public static ReqValidation create() {
    return new StartReqValidation();
  }

  public static class StartReqValidation extends ReqValidation {
    public StartReqValidation() {
      this.validations = new LinkedList<>();
    }

    @Override
    protected void check() {
      // this method is left intentionally empty
    }
  }
}
