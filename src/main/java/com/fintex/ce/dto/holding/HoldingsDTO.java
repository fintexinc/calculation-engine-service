package com.fintex.ce.dto.holding;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fintex.ce.dto.calculation.HoldingForDailyCalculationDTO;
import com.fintex.ce.dto.request.MultiplePortfoliosReqDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class HoldingsDTO {

    private List<Holding> holdings = new ArrayList<>();

    private List<HoldingForDailyCalculationDTO> dailyHoldings = new ArrayList<>();

    private List<Holding> benchmarkHoldings = new ArrayList<>();

    private Set<MultiplePortfoliosReqDTO.Portfolio> portfolios = new HashSet<>();

}
