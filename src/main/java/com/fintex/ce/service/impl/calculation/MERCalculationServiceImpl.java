package com.fintex.ce.service.impl.calculation;

import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.config.enumeration.HoldingType;
import com.fintex.ce.config.enumeration.ParameterType;
import com.fintex.ce.dto.AverageManagementExpenseCalculationDTO;
import com.fintex.ce.dto.ParamHolderDTO;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.request.AverageMerRequestDTO;
import com.fintex.ce.dto.response.AverageMerResponse;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.exception.notification.pattern.Notification;
import com.fintex.ce.service.impl.cache.AverageMERCacheStorage;
import com.fintex.ce.service.interfaces.calculation.AverageManagementExpenseCalculationService;
import com.fintex.ce.util.validation.request.AverageMerRequestValidator;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.fintex.ce.config.enumeration.ExceptionCode.*;
import static com.fintex.ce.config.enumeration.ParameterType.*;
import static com.fintex.ce.util.FilterUtils.getSpecifiedIfEmpty;

@Service
public class MERCalculationServiceImpl extends AverageManagementExpenseCalculationService<AverageMerResponse> {

    private final AverageMERCacheStorage averageMERCacheStorage;

    public MERCalculationServiceImpl(final AverageMERCacheStorage averageMERCacheStorage,
                                     final AverageMerRequestValidator requestValidator) {
        super(requestValidator);
        this.averageMERCacheStorage = averageMERCacheStorage;
    }

    @Override
    protected Map<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> loadDataFromCacheStorage(final AverageMerRequestDTO reqDTO) {
        return averageMERCacheStorage.load(reqDTO.getHoldings(), getSpecifiedIfEmpty(reqDTO.getDataProviders(), DataProvider.DEFAULT_PROVIDERS),
                List.of(), new ParamHolderDTO());
    }

    @Override
    protected List<Warning> setInitialFeeAndModifiedFeeValues(final Map<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> groupOfMers) {
        List<Warning> warnings = new ArrayList<>();
        var notification = new Notification();
        groupOfMers.forEach((holdingType, mer) -> {
            if (HoldingType.CANADA_ETF.equals(holdingType) || HoldingType.CANADA_MUTUAL_FUNDS.equals(holdingType) || HoldingType.SEGREGATED_FUND_CANADA.equals(holdingType)
                    || HoldingType.CANADA_HEDGE_FUNDS.equals(holdingType)) {
                mer.forEach((key, value)
                        -> handleFeesForCanadaMutualHedgeFundsAndEtf(value, key, notification).ifPresent(warnings::addAll)
                );
            } else if (HoldingType.US_ETF.equals(holdingType) || HoldingType.US_MUTUAL_FUNDS.equals(holdingType)) {
                mer.forEach((key, value)
                        -> handleFeesForUsEtfAndMutualFund(value, key, notification).ifPresent(warnings::add)
                );
            }
        });

        notification.ifAnyErrorThrowException();
        return warnings;
    }

    Optional<List<Warning>> handleFeesForCanadaMutualHedgeFundsAndEtf(AverageManagementExpenseCalculationDTO averageManagementExpenseCalculationDTO,
                                                                      Holding holding,
                                                                      Notification notification) {
        if (Objects.isNull(averageManagementExpenseCalculationDTO.getManagementExpenseRatio()) &&
                Objects.isNull(averageManagementExpenseCalculationDTO.getActualManagementFee())) {
            if (HoldingType.CANADA_HEDGE_FUNDS.equals(holding.getType())) {
                setFeeValues(averageManagementExpenseCalculationDTO, averageManagementExpenseCalculationDTO.getManagementExpenseRatio());
                return Optional.of(List.of(WRN_MER_MER_001.warning(holding), WRN_MER_AMF_001.warning(holding)));
            } else {
                notification.addError(ERR_MER_MERMF_001.error(holding));
            }
        } else if (Objects.isNull(averageManagementExpenseCalculationDTO.getManagementExpenseRatio())) {
            setFeeValues(averageManagementExpenseCalculationDTO, averageManagementExpenseCalculationDTO.getActualManagementFee());
            return Optional.of(List.of(WRN_MER_MER_001.warning(holding)));
        } else if (Objects.isNull(averageManagementExpenseCalculationDTO.getActualManagementFee())) {
            setFeeValues(averageManagementExpenseCalculationDTO, averageManagementExpenseCalculationDTO.getManagementExpenseRatio());
            return Optional.of(List.of(WRN_MER_AMF_001.warning(holding)));
        }
        setFeeValues(averageManagementExpenseCalculationDTO, averageManagementExpenseCalculationDTO.getManagementExpenseRatio());
        return Optional.empty();
    }

    Optional<Warning> handleFeesForUsEtfAndMutualFund(AverageManagementExpenseCalculationDTO averageManagementExpenseCalculationDTO,
                                                      Holding holding,
                                                      Notification notification) {
        if (Objects.isNull(averageManagementExpenseCalculationDTO.getNetExpenseRatio()) &&
                Objects.isNull(averageManagementExpenseCalculationDTO.getGrossExpenseRatio())) {
            notification.addError(ERR_MER_NERGER_001.error(holding));
        } else if (Objects.isNull(averageManagementExpenseCalculationDTO.getNetExpenseRatio())) {
            setFeeValues(averageManagementExpenseCalculationDTO, averageManagementExpenseCalculationDTO.getGrossExpenseRatio());
            return Optional.of(WRN_MER_NER_001.warning(holding));
        } else if (Objects.isNull(averageManagementExpenseCalculationDTO.getGrossExpenseRatio())) {
            setFeeValues(averageManagementExpenseCalculationDTO, averageManagementExpenseCalculationDTO.getNetExpenseRatio());
            return Optional.of(WRN_MER_GER_001.warning(holding));
        }
        setFeeValues(averageManagementExpenseCalculationDTO, averageManagementExpenseCalculationDTO.getNetExpenseRatio());
        return Optional.empty();
    }

    @Override
    protected AverageMerResponse calculateAverageValue(final List<ParameterType> parameterTypes, final Map<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> averageMerCalculationDtos) {
        final var resDTO = new AverageMerResponse();
        if (parameterTypes.contains(SCALED)) {
            final BigDecimal scaledAverageMer = getScaledAverageMer(averageMerCalculationDtos);
            resDTO.getManagementExpenseRatio().put(SCALED, scaledAverageMer);
        }
        if (parameterTypes.contains(ABSOLUTE)) {
            final BigDecimal absoluteAverageMer = getAbsoluteAverageMer(averageMerCalculationDtos);
            resDTO.getManagementExpenseRatio().put(ABSOLUTE, absoluteAverageMer);
        }
        if (parameterTypes.contains(FORCE_REPORT_FEE)) {
            final BigDecimal forceReportFeeAverageMer = getForceReportFeeAverageMer(averageMerCalculationDtos);
            resDTO.getManagementExpenseRatio().put(FORCE_REPORT_FEE, forceReportFeeAverageMer);
        }
        return resDTO;
    }

    @Override
    protected void setNullForScaledAndForcedReportFeeIfHoldingContainsNoFunds(final AverageMerResponse response, final AverageMerRequestDTO reqDTO) {
        setNullForScaledAndForcedReportFeeIfHoldingContainsNoFunds(response.getManagementExpenseRatio(), reqDTO);
    }
}
