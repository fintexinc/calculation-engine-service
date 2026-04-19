package com.fintex.ce.adapter.rest.util;

import org.springframework.beans.BeanUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Map;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility for copying properties from a calculation domain result into a REST response DTO. Works around the fact that
 * {@link BeanUtils#copyProperties(Object, Object)} skips {@link Map} properties when generic type parameters differ
 * even when the raw types are compatible.
 */
@Slf4j
@UtilityClass
public final class ResultCopyUtils {

  public static void copyProperties(final Object source, final Object target) {
    BeanUtils.copyProperties(source, target);
    copyMapPropertiesWithIncompatibleGenerics(source, target);
  }

  @SuppressWarnings({"rawtypes"})
  private static void copyMapPropertiesWithIncompatibleGenerics(final Object source, final Object target) {
    PropertyDescriptor[] sourceDescriptors = BeanUtils.getPropertyDescriptors(source.getClass());
    for (PropertyDescriptor sourceDescriptor : sourceDescriptors) {
      String propertyName = sourceDescriptor.getName();
      Method readMethod = sourceDescriptor.getReadMethod();
      if (readMethod == null || !Map.class.isAssignableFrom(readMethod.getReturnType())) {
        continue;
      }

      PropertyDescriptor targetDescriptor = BeanUtils.getPropertyDescriptor(target.getClass(), propertyName);
      if (targetDescriptor == null) {
        continue;
      }

      Method writeMethod = targetDescriptor.getWriteMethod();
      if (writeMethod == null || !Map.class.isAssignableFrom(writeMethod.getParameterTypes()[0])) {
        continue;
      }

      try {
        Map sourceMap = (Map) readMethod.invoke(source);
        if (sourceMap != null) {
          writeMethod.invoke(target, sourceMap);
        }
      } catch (ReflectiveOperationException e) {
        log.warn("Failed to copy map property '{}': {}", propertyName, e.getMessage());
      }
    }
  }
}
