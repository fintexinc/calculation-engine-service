package com.fintex.ce.service.impl.calculation;

import com.fintex.ce.domain.calculation.SalesChargeCalculation;
import com.fintex.ce.dto.ParamHolderDTO;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.request.PortfolioHoldingsReqDTO;
import com.fintex.ce.dto.response.SalesChargeResDtos;
import com.fintex.ce.model.redis.RSalesCharge;
import com.fintex.ce.service.impl.cache.SalesChargeCacheStorage;
import com.fintex.ce.service.interfaces.calculation.SalesChargeService;
import com.fintex.ce.util.validation.request.PortfolioHoldingsReqDtoValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SalesChargeServiceImpl implements SalesChargeService {

    private final SalesChargeCacheStorage salesChargeCacheStorage;
    private final PortfolioHoldingsReqDtoValidator requestValidator;

    @Autowired
    public SalesChargeServiceImpl(SalesChargeCacheStorage salesChargeCacheStorage,
                                  PortfolioHoldingsReqDtoValidator requestValidator) {
        this.salesChargeCacheStorage = salesChargeCacheStorage;
        this.requestValidator = requestValidator;
    }

    @Override
    public SalesChargeResDtos perform(PortfolioHoldingsReqDTO reqDTO) {
        requestValidator.validate(reqDTO);
        Map<Holding, RSalesCharge> salesCharges = salesChargeCacheStorage.load(reqDTO.getHoldings(),
                reqDTO.getDataProviders(), List.of(), new ParamHolderDTO());

        SalesChargeCalculation salesChargeCalculation = getSalesChargeCalculation(salesCharges);
        return salesChargeCalculation.calculate();
    }

    public SalesChargeCalculation getSalesChargeCalculation(Map<Holding, RSalesCharge> salesCharges) {
        return new SalesChargeCalculation(salesCharges);
    }


}
