package com.fintex.ce.service.impl.calculation;

import com.fintex.ce.config.enumeration.Currency;
import com.fintex.ce.domain.monthlyreturns.Returns;
import com.fintex.ce.dto.calculation.CommonDatesResDTO;
import com.fintex.ce.dto.exception.ErrorRes2DTO;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.request.MultiplePortfoliosReqDTO;
import com.fintex.ce.dto.response.CommonPerformanceDatesResDTO;
import com.fintex.ce.exception.notification.pattern.Notification;
import com.fintex.ce.model.redis.RMonthlyReturns;
import com.fintex.ce.service.interfaces.calculation.CommonPerformanceDateService;
import com.fintex.ce.util.validation.request.CommonDatesRequestValidator;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Set;

@Service
public class CommonPerformanceDateServiceImpl implements CommonPerformanceDateService {

    private final MonthlyReturnsService monthlyReturnsService;
    private final CommonDatesRequestValidator commonDatesRequestValidator;

    public CommonPerformanceDateServiceImpl(MonthlyReturnsService monthlyReturnsService,
                                            CommonDatesRequestValidator commonDatesRequestValidator) {
        this.monthlyReturnsService = monthlyReturnsService;
        this.commonDatesRequestValidator = commonDatesRequestValidator;
    }

    @Override
    public CommonPerformanceDatesResDTO commonPerformanceDate(MultiplePortfoliosReqDTO mReqDTO) {
        commonDatesRequestValidator.validate(mReqDTO.getBenchmarkHoldings(), mReqDTO.getPortfolios());
        List<Holding> portfolioHoldings = collectAllPortfolioHoldings(mReqDTO.getPortfolios());

        var notification = new Notification();
        Returns<RMonthlyReturns> monthlyReturnsForPortfolios = notification.tryCatch(() -> getPortfolioMonthlyReturns(portfolioHoldings));
        CommonDatesResDTO commonPerformanceDateForPortfolios = notification.tryCatch(() -> commonPerformanceDateFor(monthlyReturnsForPortfolios));
        Returns<RMonthlyReturns> monthlyReturnsForBenchmark = notification.tryCatch(() -> getPortfolioMonthlyReturns(mReqDTO.getBenchmarkHoldings()));
        CommonDatesResDTO commonPerformanceDatesForBenchmarks = notification.tryCatch(() -> commonPerformanceDateFor(monthlyReturnsForBenchmark));
        notification.ifAnyErrorThrowException();

        CommonPerformanceDatesResDTO res = new CommonPerformanceDatesResDTO()
                .setCommonPerformanceStartDatePf(commonPerformanceDateForPortfolios.getStartDate())
                .setCommonPerformanceEndDatePf(commonPerformanceDateForPortfolios.getEndDate())
                .setCommonPerformanceStartDateBm(commonPerformanceDatesForBenchmarks.getStartDate())
                .setCommonPerformanceEndDateBm(commonPerformanceDatesForBenchmarks.getEndDate());

        if (!ObjectUtils.isEmpty(monthlyReturnsForPortfolios)) {
            res.setErrors(notification.tryCatch(monthlyReturnsForPortfolios.getErrors().stream().map(ErrorRes2DTO::new)::toList));
        }

        return res;
    }

    List<Holding> collectAllPortfolioHoldings(Set<MultiplePortfoliosReqDTO.Portfolio> portfolios) {
        if (CollectionUtils.isEmpty(portfolios)) {
            return List.of();
        }
        return portfolios.stream().flatMap(p -> p.getHoldings().stream()).toList();
    }

    CommonDatesResDTO commonPerformanceDateFor(Returns<RMonthlyReturns> monthlyReturns) {
        if (ObjectUtils.isEmpty(monthlyReturns)) {
            return new CommonDatesResDTO();
        }
        return new CommonDatesResDTO()
                .setStartDate(monthlyReturns.getPsd())
                .setEndDate(monthlyReturns.getPed());
    }

    Returns<RMonthlyReturns> getPortfolioMonthlyReturns(List<Holding> holdings) {
        if (CollectionUtils.isEmpty(holdings)) {
            return new Returns<>();
        }
        return monthlyReturnsService.getMonthlyReturnsOnlyWithMonthlyReturnsDataValidation(holdings, Currency.CAD);
    }

}
