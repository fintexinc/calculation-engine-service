package com.fintex.ce.service.impl.calculation;

import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.config.enumeration.HoldingType;
import com.fintex.ce.config.enumeration.ParameterType;
import com.fintex.ce.dto.AverageManagementExpenseCalculationDTO;
import com.fintex.ce.dto.ParamHolderDTO;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.request.AverageMerRequestDTO;
import com.fintex.ce.dto.response.ManagementFeeResponse;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.exception.notification.pattern.Notification;
import com.fintex.ce.service.impl.cache.ManagementFeeCacheStorage;
import com.fintex.ce.service.interfaces.calculation.AverageManagementExpenseCalculationService;
import com.fintex.ce.util.validation.request.AverageMerRequestValidator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.fintex.ce.config.constant.HoldingTypeGroup.FUNDS;
import static com.fintex.ce.config.enumeration.ExceptionCode.ERR_MF_MF_001;
import static com.fintex.ce.config.enumeration.ParameterType.ABSOLUTE;
import static com.fintex.ce.config.enumeration.ParameterType.SCALED;
import static com.fintex.ce.util.FilterUtils.getSpecifiedIfEmpty;

@Service
public class ManagementFeeCalculationServiceImpl
        extends AverageManagementExpenseCalculationService<ManagementFeeResponse> {

    private final ManagementFeeCacheStorage managementFeeCacheStorage;

    public ManagementFeeCalculationServiceImpl(final ManagementFeeCacheStorage managementFeeCacheStorage,
                                               final AverageMerRequestValidator requestValidator) {
        super(requestValidator);
        this.managementFeeCacheStorage = managementFeeCacheStorage;
    }

    @Override
    protected void setNullForScaledAndForcedReportFeeIfHoldingContainsNoFunds(final ManagementFeeResponse response, final AverageMerRequestDTO reqDTO) {
        setNullForScaledIfHoldingContainsNoFunds(response.getManagementFee(), reqDTO);
    }

    @Override
    protected Map<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> loadDataFromCacheStorage(final AverageMerRequestDTO reqDTO) {
        return managementFeeCacheStorage.load(reqDTO.getHoldings(),
                getSpecifiedIfEmpty(reqDTO.getDataProviders(), DataProvider.DEFAULT_PROVIDERS), List.of(), new ParamHolderDTO());
    }

    @Override
    protected List<Warning> setInitialFeeAndModifiedFeeValues(final Map<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> groupOfMers) {
        List<Warning> warnings = new ArrayList<>();
        var notification = new Notification();
        groupOfMers.forEach((holdingType, managementFee) -> {
            if (FUNDS.contains(holdingType)) {
                managementFee.forEach((key, value) -> validateManagementFee(value, key, notification).ifPresent(warnings::addAll));
            }
        });
        notification.ifAnyErrorThrowException();
        return warnings;
    }

    Optional<List<Warning>> validateManagementFee(AverageManagementExpenseCalculationDTO averageManagementExpenseCalculationDTO,
                                                  Holding holding,
                                                  Notification notification) {
        if (Objects.isNull(averageManagementExpenseCalculationDTO.getActualManagementFee())) {
            notification.addError(ERR_MF_MF_001.error(holding, HttpStatus.BAD_REQUEST));
        }
        setFeeValues(averageManagementExpenseCalculationDTO, averageManagementExpenseCalculationDTO.getActualManagementFee());
        return Optional.empty();
    }

    @Override
    protected ManagementFeeResponse calculateAverageValue(List<ParameterType> parameterTypes, Map<HoldingType, Map<Holding, AverageManagementExpenseCalculationDTO>> averageMerCalculationDtos) {
        final var resDTO = new ManagementFeeResponse();
        if (parameterTypes.contains(SCALED)) {
            final BigDecimal scaledAverageMer = getScaledAverageMer(averageMerCalculationDtos);
            resDTO.getManagementFee().put(SCALED, scaledAverageMer);
        }
        if (parameterTypes.contains(ABSOLUTE)) {
            final BigDecimal absoluteAverageMer = getAbsoluteAverageMer(averageMerCalculationDtos);
            resDTO.getManagementFee().put(ABSOLUTE, absoluteAverageMer);
        }
        return resDTO;
    }
}
