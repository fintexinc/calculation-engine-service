package com.fintex.ce.model.domain.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ParameterType {

  ABSOLUTE("absolute"),
  SCALED("scaled"),
  FORCE_REPORT_FEE("forceReportFee");

  private final String name;

  ParameterType(final String name) {
    this.name = name;
  }

  /*
   * @JsonCreator was used to deserialize from json to ParameterType
   */
  @JsonCreator
  public static ParameterType fromJson(String name) {
    for (ParameterType t : values()) {
      if (t.name().equalsIgnoreCase(name) || t.getName().equalsIgnoreCase(name)) {
        return t;
      }
    }
    return null;
  }

  /*
   * @JsonValue was used to serialize ParameterType to its value e.g 'FORCE_REPORT_FEE' would be serialized to
   * 'forceReportFee'
   */
  @JsonValue
  public String getName() {
    return name;
  }

}
