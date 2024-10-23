package com.sobczyk.walletMicroservices.service;

import com.sobczyk.walletMicroservices.dto.requests.PositionPerformanceRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PerformanceCoreListener {

    private final PositionPerfService positionPerfService;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = {"q.performance-core"})
    public void generatePerfReport(List<PositionPerformanceRequest> requestList) {
        log.info("received new message from rabbit queue: " + requestList);
        System.out.println(requestList.size());
        throw new RuntimeException("constraint violation");
//        rabbitTemplate.convertAndSend("x.performance-report", "performance-report", requestList);
//        log.info("New message added on queue");
 //       return ResponseEntity.ok("request added to queue");
//        requestList.stream()
//                .map(r -> positionPerfService.getPositionPerformance(r.investorId(), r.timeSeries()))
//                .forEach(r -> {
//                    System.out.println(r.getTransactions().toString());
//                    System.out.println(r.getCurrentPositions().toString());
//                });
        //throw new RuntimeException("constraint violation");
    }
}
