package com.sobczyk.walletMicroservices.dto.responses;

import com.sobczyk.walletMicroservices.dto.InvestorDto;
import com.sobczyk.walletMicroservices.dto.PositionPerformanceDto;
import com.sobczyk.walletMicroservices.position.performance.PositionPerfValue;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PositionPerformanceResponse {

    private List<PositionPerformanceDto> allTimePositions;
    private List<PositionPerformanceDto> currentPositions;
    private List<PositionPerformanceDto> allTimeOverall;
    private PositionPerfValue overall;
    private PositionPerfValue cash;
    private List<TransactionResponse> transactions;
    private List<FinancialNewsResponse> positionsNews;
    private InvestorDto investorDto;

    public PositionPerformanceResponse() {
        this.allTimePositions = new ArrayList<>();
        this.currentPositions = new ArrayList<>();
        this.allTimeOverall = new ArrayList<>();
        this.overall = new PositionPerfValue(BigDecimal.ZERO);
        this.cash = new PositionPerfValue(BigDecimal.ZERO);
        this.transactions = new ArrayList<>();
        this.positionsNews = new ArrayList<>();
    }
}
