package com.sobczyk.walletMicroservices.service;

import com.sobczyk.walletMicroservices.dto.requests.PositionPerformanceRequest;
import com.sobczyk.walletMicroservices.dto.responses.PositionPerformanceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PerformanceCoreListener {

    private final PositionPerfService positionPerfService;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = {"q.performance-core"})
    public void generatePerfReport(PositionPerformanceRequest request) {
        PositionPerformanceResponse response = positionPerfService.getPositionPerformance(request);
        rabbitTemplate.convertAndSend("x.performance-report", "performance-report", response);
    }
}
