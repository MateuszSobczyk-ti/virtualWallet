package com.sobczyk.walletMicroservices.controller;

import com.sobczyk.walletMicroservices.dto.requests.PositionPerformanceRequest;
import com.sobczyk.walletMicroservices.dto.requests.TransactionRequest;
import com.sobczyk.walletMicroservices.dto.responses.PositionPerformanceResponse;
import com.sobczyk.walletMicroservices.entity.Investor;
import com.sobczyk.walletMicroservices.service.PositionPerfService;
import com.sobczyk.walletMicroservices.service.TransactionService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/position")
@Validated
public class PositionController {

    private final TransactionService transactionService;
    private final PositionPerfService positionPerfService;

    @PostMapping("/transaction")
    public ResponseEntity<?> saveTransaction(@AuthenticationPrincipal Investor investor,
                                             @Valid @RequestBody TransactionRequest request) {
        return transactionService.saveTransaction(investor, request);
    }

    @GetMapping("/performance")
    public ResponseEntity<PositionPerformanceResponse> getPositionPerformance(@AuthenticationPrincipal Investor investor,
                                                                                    @RequestParam Integer timeSeries) {
        return ResponseEntity.ok().body(
                positionPerfService.getPositionPerformance(new PositionPerformanceRequest(investor.getId(), timeSeries)));
    }
}
