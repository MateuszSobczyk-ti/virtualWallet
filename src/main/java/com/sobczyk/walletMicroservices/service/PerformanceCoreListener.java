package com.sobczyk.walletMicroservices.service;

import com.sobczyk.walletMicroservices.dto.InvestorDto;
import com.sobczyk.walletMicroservices.dto.requests.PositionPerformanceRequest;
import com.sobczyk.walletMicroservices.dto.responses.PositionPerformanceResponse;
import com.sobczyk.walletMicroservices.entity.Investor;
import com.sobczyk.walletMicroservices.repository.InvestorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PerformanceCoreListener {

    private final PositionPerfService positionPerfService;
    private final RabbitTemplate rabbitTemplate;
    private final InvestorRepository investorRepository;

    @RabbitListener(queues = {"q.performance-core"})
    public void generatePerfReport(PositionPerformanceRequest request) {
        Optional<Investor> investor = investorRepository.findById(request.getInvestorId());
        if (investor.isPresent()) {
            request.setInvestorDto(new InvestorDto(investor.get().getFirstname(), investor.get().getLastname(), investor.get().getEmail()));
            PositionPerformanceResponse response = positionPerfService.getPositionPerformance(request);
            rabbitTemplate.convertAndSend("x.performance-report", "performance-report", response);
        } else {
            log.warn("Investor with given id: " + request.getInvestorId() + " does not exists");
        }
    }
}
