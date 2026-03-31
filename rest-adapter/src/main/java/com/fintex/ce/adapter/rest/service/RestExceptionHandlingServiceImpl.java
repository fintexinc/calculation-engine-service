package com.fintex.ce.adapter.rest.service;

import com.fintex.ce.adapter.rest.dto.exception.ErrorRes2DTO;
import com.fintex.ce.adapter.rest.dto.response.core.ErrorDTO;
import com.fintex.ce.domain.exception.DataErrorException;
import com.fintex.ce.domain.exception.FdsDataValidationException;
import jakarta.servlet.http.HttpServletRequest;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class RestExceptionHandlingServiceImpl implements RestExceptionHandlingService {

  @Override
  public <T extends ErrorDTO> T returnObjectWithListOfErrors(Supplier<T> methodToPerform, Supplier<T> responseClass,
      HttpServletRequest request) {
    try {
      return methodToPerform.get();
    } catch (FdsDataValidationException e) {
      List<ErrorRes2DTO> errors = new ArrayList<>();

      for (DataErrorException exception : e.getExceptionList()) {
        log.error(exception.getMessage());
        errors.add(new ErrorRes2DTO(exception.getId(), exception.getCode().toString(), exception.getMessage()));
      }

      final T response = responseClass.get();
      response.setErrors(errors);
      return response;
    }
  }

  public <R, D extends ErrorDTO> D handleWithResultMapping(Supplier<R> resultSupplier, Supplier<D> dtoFactory,
      HttpServletRequest request) {
    try {
      R result = resultSupplier.get();
      D dto = dtoFactory.get();
      copyPropertiesIncludingMaps(result, dto);
      return dto;
    } catch (FdsDataValidationException e) {
      List<ErrorRes2DTO> errors = new ArrayList<>();

      for (DataErrorException exception : e.getExceptionList()) {
        log.error(exception.getMessage());
        errors.add(new ErrorRes2DTO(exception.getId(), exception.getCode().toString(), exception.getMessage()));
      }

      final D response = dtoFactory.get();
      response.setErrors(errors);
      return response;
    }
  }

  /**
   * Copies properties from source to target, including Map properties with different generic types.
   * BeanUtils.copyProperties skips Map properties when generic type parameters differ, even if the
   * raw types are compatible. This method handles that case by checking raw type compatibility.
   */
  @SuppressWarnings({"rawtypes"})
  private void copyPropertiesIncludingMaps(Object source, Object target) {
    BeanUtils.copyProperties(source, target);

    // Additionally copy Map properties that BeanUtils may have skipped due to generic type mismatch
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
      } catch (Exception e) {
        log.warn("Failed to copy map property '{}': {}", propertyName, e.getMessage());
      }
    }
  }

}
