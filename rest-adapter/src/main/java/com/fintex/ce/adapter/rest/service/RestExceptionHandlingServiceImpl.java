package com.fintex.ce.adapter.rest.service;

import com.fintex.ce.adapter.rest.dto.exception.ErrorRes2DTO;
import com.fintex.ce.adapter.rest.dto.holding.HoldingsDTO;
import com.fintex.ce.adapter.rest.dto.response.core.ErrorDTO;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.exception.DataErrorException;
import com.fintex.ce.domain.exception.FdsDataValidationException;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.application.service.ExceptionHandlingServiceImpl;
import com.fintex.ce.adapter.cache.repository.FxRatesRepository;
import com.fintex.ce.adapter.cache.repository.core.CoreRedisCacheRepository;
import com.fintex.ce.util.JacksonUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.util.ContentCachingRequestWrapper;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@Log4j2
@Service
public class RestExceptionHandlingServiceImpl extends ExceptionHandlingServiceImpl
    implements RestExceptionHandlingService {

  public RestExceptionHandlingServiceImpl(final List<CoreRedisCacheRepository> coreRedisCacheRepositories,
      final FxRatesRepository fxRatesRepository) {
    super(coreRedisCacheRepositories, fxRatesRepository);
  }

  @Override
  public void removeRedisCacheForRequestedHoldings(final HttpServletRequest request, final Exception reqErrorException,
      final String requestUri) {
    if (HttpMethod.POST.name().equalsIgnoreCase(request.getMethod())) {
      try {
        final String requestBody = getRequestBody(request);
        final HoldingsDTO holdingsDTO = JacksonUtil.deserialize(requestBody, HoldingsDTO.class);
        final List<String> holdingsIds = getHoldingsIds(holdingsDTO);
        holdingsIds.forEach(this::removeDataFromRepositoriesByHoldingId);
        log.info("Remove from cache these holdings: {}", holdingsDTO);
      } catch (Exception e) {
        log.error(e);
      }
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends ErrorDTO> T returnObjectWithListOfErrors(Supplier<T> methodToPerform, Supplier<T> responseClass,
      HttpServletRequest request) {
    try {
      return methodToPerform.get();
    } catch (FdsDataValidationException e) {
      List<ErrorRes2DTO> errors = new ArrayList<>();

      for (DataErrorException exception : e.getExceptionList()) {
        ifFxRatesErrorRemoveFxRatesFromRedisCache(exception);
        log.error(exception.getMessage());
        errors.add(new ErrorRes2DTO(exception.getId(), exception.getCode().toString(), exception.getMessage()));
      }

      CompletableFuture.runAsync(() -> removeRedisCacheForRequestedHoldings(request, e, request.getRequestURI()));

      final T response = responseClass.get();
      response.setErrors(errors);
      return response;
    }
  }

  /**
   * Handles service calls that return Result objects, mapping them to response DTOs.
   * Catches FdsDataValidationException and maps errors to the response DTO.
   */
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
        ifFxRatesErrorRemoveFxRatesFromRedisCache(exception);
        log.error(exception.getMessage());
        errors.add(new ErrorRes2DTO(exception.getId(), exception.getCode().toString(), exception.getMessage()));
      }

      CompletableFuture.runAsync(() -> removeRedisCacheForRequestedHoldings(request, e, request.getRequestURI()));

      final D response = dtoFactory.get();
      response.setErrors(errors);
      return response;
    }
  }

  public List<String> getHoldingsIds(final HoldingsDTO holdingsDTO) {
    final List<Holding> allHoldings = mergeHoldings(holdingsDTO);
    final List<HoldingType> holdingTypes = List.of(HoldingType.CANADA_MUTUAL_FUNDS, HoldingType.US_ETF,
        HoldingType.CANADA_ETF, HoldingType.US_STOCKS, HoldingType.CANADA_STOCKS, HoldingType.SEGREGATED_FUND_CANADA);
    return allHoldings.stream()
        .filter(h -> holdingTypes.contains(h.getType()))
        .map(h -> {
          if (h.getType() == HoldingType.CANADA_MUTUAL_FUNDS || h.getType() == HoldingType.SEGREGATED_FUND_CANADA) {
            return ((FundSeriesHolding) h).getFundServCode();
          } else {
            return h.generateUserIdentifier();
          }
        }).collect(Collectors.toList());
  }

  public List<Holding> mergeHoldings(final HoldingsDTO holdingsDTO) {
    final var result = new ArrayList<Holding>();
    if (nonNull(holdingsDTO.getHoldings())) {
      result.addAll(holdingsDTO.getHoldings());
    }
    if (nonNull(holdingsDTO.getBenchmarkHoldings())) {
      result.addAll(holdingsDTO.getBenchmarkHoldings());
    }
    if (nonNull(holdingsDTO.getPortfolios())) {
      holdingsDTO.getPortfolios().stream().filter(p -> nonNull(p.getHoldings())).forEach(portfolio -> {
        result.addAll(portfolio.getHoldings());
      });
    }
    if (nonNull(holdingsDTO.getDailyHoldings())) {
      holdingsDTO.getDailyHoldings().stream().filter(p -> nonNull(p.getHolding()))
          .forEach(dailyHolding -> result.add(dailyHolding.getHolding()));
    }
    return result;
  }

  public String getRequestBody(final HttpServletRequest request) {
    final ContentCachingRequestWrapper requestWrapper = (ContentCachingRequestWrapper) request;
    return new String(requestWrapper.getContentAsByteArray());
  }
}