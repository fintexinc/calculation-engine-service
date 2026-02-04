package com.fintex.ce.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.io.InputStream;

@SuppressWarnings("Duplicates")
public class JacksonUtil {

  private static final String CAN_NOT_DESERIALIZE = "Can't deserialize";

  public static final ObjectMapper MAPPER = new ObjectMapper();

  static {
    MAPPER.registerModule(new JavaTimeModule());
    MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    MAPPER.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    SimpleModule simpleModule = new SimpleModule();
    MAPPER.registerModule(simpleModule);
  }

  private JacksonUtil() {
  }

  public static <T> T deserialize(String json, Class<T> clazz) {
    if (json == null) {
      return null;
    } else {
      try {
        return MAPPER.readValue(json, clazz);
      } catch (IOException var3) {
        throw new IllegalStateException(CAN_NOT_DESERIALIZE, var3);
      }
    }
  }

  public static <T> T deserialize(final InputStream in, final TypeReference<T> type) {
    if (in == null) {
      return null;
    } else {
      try {
        return MAPPER.readValue(in, type);
      } catch (IOException var3) {
        throw new IllegalStateException(CAN_NOT_DESERIALIZE, var3);
      }
    }
  }

  public static <T> T deserialize(String json, TypeReference<T> type) {
    if (json == null) {
      return null;
    } else {
      try {
        return MAPPER.readValue(json, type);
      } catch (IOException var3) {
        throw new IllegalStateException(CAN_NOT_DESERIALIZE, var3);
      }
    }
  }

  public static String serialize(Object object) {
    if (object == null) {
      return null;
    } else {
      try {
        return MAPPER.writeValueAsString(object);
      } catch (JsonProcessingException var2) {
        throw new IllegalStateException(CAN_NOT_DESERIALIZE, var2);
      }
    }
  }
}
