package com.fintex.ce.adapter.rest.service;

import com.fintex.ce.adapter.rest.dto.exception.ErrorRes2DTO;
import com.fintex.ce.adapter.rest.dto.response.core.ErrorDTO;
import com.fintex.ce.domain.exception.DataErrorException;
import com.fintex.ce.domain.exception.FdsDataValidationException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class RestExceptionHandlingServiceImpl implements RestExceptionHandlingService {

  @SuppressWarnings("unchecked")
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
      BeanUtils.copyProperties(result, dto);
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

}
