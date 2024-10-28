package com.sobczyk.walletMicroservices.securtiy;

import com.sobczyk.walletMicroservices.dto.InvestorDto;
import com.sobczyk.walletMicroservices.dto.requests.AuthenticationRequest;
import com.sobczyk.walletMicroservices.dto.requests.RegisterRequest;
import com.sobczyk.walletMicroservices.dto.responses.AuthenticationResponse;
import com.sobczyk.walletMicroservices.entity.Investor;
import com.sobczyk.walletMicroservices.entity.Role;
import com.sobczyk.walletMicroservices.repository.InvestorRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class InvestorServiceImpl implements InvestorService {

    private final InvestorRepository investorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    public ResponseEntity<?> registerInvestor(RegisterRequest request) {
        if (investorRepository.findByEmail(request.email()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
        }
        Investor investor = Investor.builder()
                .firstname(request.firstname())
                .lastname(request.lastname())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .build();
        investorRepository.save(investor);
        var jwt = jwtService.generateToken(investor);
        return ResponseEntity.ok(AuthenticationResponse.builder()
                .token(jwt)
                .build());
    }

    @Override
    public ResponseEntity<?> findInvestorByEmail(String email) {
        Optional<Investor> investor = investorRepository.findByEmail(email);
        if (investor.isPresent()){
            return new ResponseEntity<>(investor, HttpStatus.OK);
        }else{
            return new ResponseEntity<>("Investor with given email not exist", HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public List<InvestorDto> findAllInvestors() {
        return investorRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        Investor investor = investorRepository.findByEmail(request.email()).orElseThrow();
        var jwt = jwtService.generateToken(investor);
        return AuthenticationResponse.builder()
                .token(jwt)
                .build();
    }

    private InvestorDto convertToDto(Investor investor) {
        return new InvestorDto(investor.getFirstname(),investor.getLastname(), investor.getEmail());
    }
}