package com.sobczyk.walletMicroservices.dto.requests;

import com.sobczyk.walletMicroservices.dto.InvestorDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PositionPerformanceRequest {
    private Long investorId;
    private InvestorDto investorDto;
    private Integer timeSeries;

    public PositionPerformanceRequest(Long investorId, Integer timeSeries) {
        this.investorId = investorId;
        this.timeSeries = timeSeries;
    }
}
