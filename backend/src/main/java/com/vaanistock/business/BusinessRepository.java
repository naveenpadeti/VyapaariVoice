package com.vaanistock.business;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusinessRepository extends JpaRepository<Business, Long> {
    List<Business> findByUserId(Long userId);
    Optional<Business> findFirstByUserId(Long userId);
}
