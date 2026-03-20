package com.fintex.ce.adapter.rest.mapper;

import com.fintex.ce.adapter.rest.dto.request.AverageMerRequestDTO;
import com.fintex.ce.adapter.rest.dto.request.BestWorstPeriodsReqDTO;
import com.fintex.ce.adapter.rest.dto.request.CorrelationReqDTO;
import com.fintex.ce.adapter.rest.dto.request.DistributionOfReturnsReqDTO;
import com.fintex.ce.adapter.rest.dto.request.IncomeForecastReqDTO;
import com.fintex.ce.adapter.rest.dto.request.LeadingTotalReturnPeriodsReqDTO;
import com.fintex.ce.adapter.rest.dto.request.MultiplePortfoliosReqDTO;
import com.fintex.ce.adapter.rest.dto.request.PeriodsReqDTO;
import com.fintex.ce.adapter.rest.dto.request.PortfolioHoldingsReqDTO;
import com.fintex.ce.adapter.rest.dto.request.ReturnReqDTO;
import com.fintex.ce.adapter.rest.dto.request.RollingCalculationReqDTO;
import com.fintex.ce.adapter.rest.dto.request.RollingCorrelationCalculationReqDTO;
import com.fintex.ce.adapter.rest.dto.request.TopCommonHoldingsReqDTO;
import com.fintex.ce.adapter.rest.dto.request.YieldReqDTO;
import com.fintex.ce.domain.dto.command.AverageMerCommand;
import com.fintex.ce.domain.dto.command.BestWorstPeriodsCommand;
import com.fintex.ce.domain.dto.command.CorrelationCommand;
import com.fintex.ce.domain.dto.command.DistributionOfReturnsCommand;
import com.fintex.ce.domain.dto.command.IncomeForecastCommand;
import com.fintex.ce.domain.dto.command.LeadingTotalReturnCommand;
import com.fintex.ce.domain.dto.command.ReturnCommand;
import com.fintex.ce.domain.dto.command.RollingCalculationCommand;
import com.fintex.ce.domain.dto.command.RollingCorrelationCommand;
import com.fintex.ce.domain.dto.command.TopCommonHoldingsCommand;
import com.fintex.ce.domain.dto.command.YieldCommand;
import com.fintex.ce.domain.dto.command.MultiplePortfoliosCommand;
import com.fintex.ce.domain.dto.command.PeriodCommand;
import com.fintex.ce.domain.dto.command.PortfolioHoldingsCommand;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class RestCommandMapper {

    public PeriodCommand toPeriodCommand(PeriodsReqDTO dto) {
        PeriodCommand cmd = new PeriodCommand();
        BeanUtils.copyProperties(dto, cmd);
        return cmd;
    }

    public PortfolioHoldingsCommand toPortfolioHoldingsCommand(PortfolioHoldingsReqDTO dto) {
        PortfolioHoldingsCommand cmd = new PortfolioHoldingsCommand();
        BeanUtils.copyProperties(dto, cmd);
        return cmd;
    }

    public ReturnCommand toReturnCommand(ReturnReqDTO dto) {
        ReturnCommand cmd = new ReturnCommand();
        BeanUtils.copyProperties(dto, cmd);
        return cmd;
    }

    public RollingCalculationCommand toRollingCalculationCommand(RollingCalculationReqDTO dto) {
        RollingCalculationCommand cmd = new RollingCalculationCommand();
        BeanUtils.copyProperties(dto, cmd);
        return cmd;
    }

    public AverageMerCommand toAverageMerCommand(AverageMerRequestDTO dto) {
        AverageMerCommand cmd = new AverageMerCommand();
        BeanUtils.copyProperties(dto, cmd);
        return cmd;
    }

    public MultiplePortfoliosCommand toMultiplePortfoliosCommand(MultiplePortfoliosReqDTO dto) {
        MultiplePortfoliosCommand cmd = new MultiplePortfoliosCommand();
        BeanUtils.copyProperties(dto, cmd);
        return cmd;
    }

    public CorrelationCommand toCorrelationCommand(CorrelationReqDTO dto) {
        CorrelationCommand cmd = new CorrelationCommand();
        BeanUtils.copyProperties(dto, cmd);
        return cmd;
    }

    public BestWorstPeriodsCommand toBestWorstPeriodsCommand(BestWorstPeriodsReqDTO dto) {
        BestWorstPeriodsCommand cmd = new BestWorstPeriodsCommand();
        BeanUtils.copyProperties(dto, cmd);
        return cmd;
    }

    public TopCommonHoldingsCommand toTopCommonHoldingsCommand(TopCommonHoldingsReqDTO dto) {
        TopCommonHoldingsCommand cmd = new TopCommonHoldingsCommand();
        BeanUtils.copyProperties(dto, cmd);
        return cmd;
    }

    public DistributionOfReturnsCommand toDistributionOfReturnsCommand(
            DistributionOfReturnsReqDTO dto) {
        DistributionOfReturnsCommand cmd = new DistributionOfReturnsCommand();
        BeanUtils.copyProperties(dto, cmd);
        return cmd;
    }

    public LeadingTotalReturnCommand toLeadingTotalReturnCommand(
            LeadingTotalReturnPeriodsReqDTO dto) {
        LeadingTotalReturnCommand cmd = new LeadingTotalReturnCommand();
        BeanUtils.copyProperties(dto, cmd);
        return cmd;
    }

    public IncomeForecastCommand toIncomeForecastCommand(IncomeForecastReqDTO dto) {
        IncomeForecastCommand cmd = new IncomeForecastCommand();
        BeanUtils.copyProperties(dto, cmd);
        return cmd;
    }

    public YieldCommand toYieldCommand(YieldReqDTO dto) {
        YieldCommand cmd = new YieldCommand();
        BeanUtils.copyProperties(dto, cmd);
        return cmd;
    }

    public RollingCorrelationCommand toRollingCorrelationCommand(
            RollingCorrelationCalculationReqDTO dto) {
        RollingCorrelationCommand cmd = new RollingCorrelationCommand();
        BeanUtils.copyProperties(dto, cmd);
        return cmd;
    }
}