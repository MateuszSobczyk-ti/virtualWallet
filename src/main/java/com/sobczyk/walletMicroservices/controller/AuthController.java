package com.sobczyk.walletMicroservices.controller;

import com.sobczyk.walletMicroservices.dto.requests.AuthenticationRequest;
import com.sobczyk.walletMicroservices.dto.requests.RegisterRequest;
import com.sobczyk.walletMicroservices.dto.responses.AuthenticationResponse;
import com.sobczyk.walletMicroservices.securtiy.InvestorServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final InvestorServiceImpl service;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        return service.registerInvestor(request);
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(service.authenticate(request));
    }

}
