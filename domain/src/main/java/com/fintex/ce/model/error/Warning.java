package com.fintex.ce.model.error;

import java.io.Serializable;
import lombok.Data;

@Data
public class Warning implements Serializable {

  private String id;
  private String message;
  private String code;

  public Warning() {
  }

  public Warning(final String id, final String message) {
    this.id = id;
    this.message = message;
  }

  public Warning(final String id, final String message, final String code) {
    this.id = id;
    this.message = message;
    this.code = code;
  }
}
