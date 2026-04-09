package com.fintex.ce.util;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.type.TypeReference;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class JacksonUtilTest {

  static final String jsonValue = "\"test\"";
  static final String stringValue = "test";

  @Test
  void deserialize_fromString_checkResult() {
    // ACT
    final String deserializedValue = JacksonUtil.deserialize(jsonValue, String.class);

    // VERIFY
    assertEquals(stringValue, deserializedValue);
  }

  @Test
  void deserialize_fromString_whenValueIsNull() {
    assertNull(JacksonUtil.deserialize(null, String.class));
  }

  @Test
  void deserialize_fromString_verifyExceptionWhenInvalidJson() {
    assertThrows(IllegalStateException.class, () -> JacksonUtil.deserialize(jsonValue, Integer.class));
  }

  @Test
  void deserialize_fromInputStream_checkResult() {
    // SETUP
    final var byteArrayInputStream = new ByteArrayInputStream(jsonValue.getBytes());

    // ACT
    final String deserializedValue = JacksonUtil.deserialize(byteArrayInputStream, new TypeReference<String>() {});

    // VERIFY
    assertEquals(stringValue, deserializedValue);
  }

  @Test
  void deserialize_fromInputStream_whenValueIsNull() {
    assertNull(JacksonUtil.deserialize((InputStream) null, new TypeReference<String>() {}));
  }

  @Test
  void deserialize_fromInputStream_verifyExceptionWhenInvalidJson() {
    // SETUP
    final var byteArrayInputStream = new ByteArrayInputStream(jsonValue.getBytes());

    // ACT
    TypeReference<Integer> typeReference = new TypeReference<>() {};
    assertThrows(IllegalStateException.class, () -> JacksonUtil.deserialize(byteArrayInputStream, typeReference));
  }

  @Test
  void deserialize_toTypeReference_checkResult() {
    // ACT
    final String deserializedValue = JacksonUtil.deserialize(jsonValue, new TypeReference<String>() {});

    // VERIFY
    assertEquals(stringValue, deserializedValue);
  }

  @Test
  void deserialize_toTypeReference_whenValueIsNull() {
    assertNull(JacksonUtil.deserialize((String) null, new TypeReference<String>() {}));
  }

  @Test
  void deserialize_toTypeReference_verifyExceptionWhenInvalidJson() {
    TypeReference<Integer> typeReference = new TypeReference<>() {};
    assertThrows(IllegalStateException.class, () -> JacksonUtil.deserialize(jsonValue, typeReference));
  }

  @Test
  void serialize_checkResult() {
    // ACT
    final String serialized = JacksonUtil.serialize(stringValue);

    // VERIFY
    assertEquals(jsonValue, serialized);
  }

  @Test
  void serialize_whenValueIsNull() {
    assertNull(JacksonUtil.serialize(null));
  }

}