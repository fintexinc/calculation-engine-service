package ca.tangerine.pce.application.util;

import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

import java.io.InputStream;

@SuppressWarnings("Duplicates")
public class JacksonUtil {

  private static final String CAN_NOT_DESERIALIZE = "Can't deserialize";

  /**
   * A Jackson 3 mapper is immutable, so every feature is set on the builder rather than on the built instance.
   * {@code java.time} support is part of databind now and registers itself, so no module is added for it; the
   * date-shape switch it used to bring moved from {@code SerializationFeature} to {@link DateTimeFeature}.
   */
  public static final ObjectMapper MAPPER = JsonMapper.builder()
      .configure(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS, false)
      .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .build();

  private JacksonUtil() {
  }

  public static <T> T deserialize(String json, Class<T> clazz) {
    if (json == null) {
      return null;
    } else {
      try {
        return MAPPER.readValue(json, clazz);
      } catch (JacksonException exception) {
        throw new IllegalStateException(CAN_NOT_DESERIALIZE, exception);
      }
    }
  }

  public static <T> T deserialize(final InputStream in, final TypeReference<T> type) {
    if (in == null) {
      return null;
    } else {
      try {
        return MAPPER.readValue(in, type);
      } catch (JacksonException exception) {
        throw new IllegalStateException(CAN_NOT_DESERIALIZE, exception);
      }
    }
  }

  public static <T> T deserialize(String json, TypeReference<T> type) {
    if (json == null) {
      return null;
    } else {
      try {
        return MAPPER.readValue(json, type);
      } catch (JacksonException exception) {
        throw new IllegalStateException(CAN_NOT_DESERIALIZE, exception);
      }
    }
  }

  public static String serialize(Object object) {
    if (object == null) {
      return null;
    } else {
      try {
        return MAPPER.writeValueAsString(object);
      } catch (JacksonException exception) {
        throw new IllegalStateException(CAN_NOT_DESERIALIZE, exception);
      }
    }
  }
}
