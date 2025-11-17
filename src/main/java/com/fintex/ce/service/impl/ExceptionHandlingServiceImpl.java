package com.fintex.ce.service.impl;

import com.fintex.ce.config.enumeration.HoldingType;
import com.fintex.ce.dto.exception.ErrorRes2DTO;
import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.holding.HoldingsDTO;
import com.fintex.ce.dto.response.core.ErrorDTO;
import com.fintex.ce.exception.DataErrorException;
import com.fintex.ce.exception.FdsDataValidationException;
import com.fintex.ce.model.redis.core.RedisId;
import com.fintex.ce.repository.redis.FxRatesRepository;
import com.fintex.ce.repository.redis.core.CoreRedisCacheRepository;
import com.fintex.ce.service.interfaces.ExceptionHandlingService;
import com.fintex.ce.util.JacksonUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.util.ContentCachingRequestWrapper;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.fintex.ce.config.enumeration.ExceptionCode.FX_RATE_EXCEPTION_CODES;
import static java.util.Objects.nonNull;

@Log4j2
@Service
public class ExceptionHandlingServiceImpl implements ExceptionHandlingService {

    private final List<CoreRedisCacheRepository> coreRedisCacheRepositories;
    private final FxRatesRepository fxRatesRepository;

    @Autowired
    public ExceptionHandlingServiceImpl(final List<CoreRedisCacheRepository> coreRedisCacheRepositories,
                                        final FxRatesRepository fxRatesRepository) {
        this.coreRedisCacheRepositories = coreRedisCacheRepositories;
        this.fxRatesRepository = fxRatesRepository;
    }

    /**
     * removes all records from Redis for Holdings that are in request body, and put information about request statistics in DB
     * requestUri should be as parameter, the method is called in a separate thread,
     * so we can't receive requestUri after HttpServletRequest closed
     *
     * @param request           HttpServletRequest for request information
     * @param reqErrorException an exception that was thrown in this request
     */
    @Override
    public void removeRedisCacheForRequestedHoldings(final HttpServletRequest request, final Exception reqErrorException, final String requestUri) {
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

    @Override
    public void removeFxRatesFromRedisCache() {
        fxRatesRepository.deleteAll();
        log.info("Remove fx rates from redis cache");
    }

    /**
     * removes data from all Redis repositories by holdingId
     *
     * @param holdingId
     */
    @SuppressWarnings("unchecked")
    void removeDataFromRepositoriesByHoldingId(final String holdingId) {
        coreRedisCacheRepositories.forEach(repository -> {
            final List<RedisId> redisIds = repository.findAllByHoldingId(holdingId);
            redisIds.forEach(id -> repository.deleteById(id.getId()));
        });
    }

    /**
     * returns all holding ids from request (holdings and benchmarks)
     *
     * @param holdingsDTO
     * @return list of holding ids
     */
    List<String> getHoldingsIds(final HoldingsDTO holdingsDTO) {
        final List<Holding> allHoldings = mergeHoldings(holdingsDTO);
        final List<HoldingType> holdingTypes = List.of(HoldingType.CANADA_MUTUAL_FUNDS, HoldingType.US_ETF, HoldingType.CANADA_ETF, HoldingType.US_STOCKS, HoldingType.CANADA_STOCKS, HoldingType.SEGREGATED_FUND_CANADA);
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

    /**
     * checks if holdings and benchmarks are not null, and puts all benchmarks in holdings list
     *
     * @param holdingsDTO
     */
    List<Holding> mergeHoldings(final HoldingsDTO holdingsDTO) {
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

    /**
     * returns request body from current request
     *
     * @param request
     * @return
     */
    String getRequestBody(final HttpServletRequest request) {
        final ContentCachingRequestWrapper requestWrapper = (ContentCachingRequestWrapper) request;
        return new String(requestWrapper.getContentAsByteArray());
    }

    @Override
    public void ifFxRatesErrorRemoveFxRatesFromRedisCache(final DataErrorException e) {
        FX_RATE_EXCEPTION_CODES.stream()
                .filter(exceptionCode -> exceptionCode.equals(e.getCode()))
                .forEach(exceptionCode -> CompletableFuture.runAsync(this::removeFxRatesFromRedisCache));
    }

    @Override
    public <T extends ErrorDTO> T returnObjectWithListOfErrors(Supplier<T> methodToPerform, Supplier<T> responseClass, HttpServletRequest request) {
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
}
