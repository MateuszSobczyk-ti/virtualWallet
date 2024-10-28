package com.sobczyk.walletMicroservices.repository;

import com.sobczyk.walletMicroservices.entity.Investor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InvestorRepository extends JpaRepository<Investor, Long> {

    Optional<Investor> findByEmail(String email);

    Optional<Investor> findById(Long id);
}