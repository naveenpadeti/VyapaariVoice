package com.vaanistock.voice;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoiceCommandRepository extends JpaRepository<VoiceCommand, Long> {
    List<VoiceCommand> findByBusinessIdOrderByCreatedAtDesc(Long businessId);
    Page<VoiceCommand> findByBusinessIdOrderByCreatedAtDesc(Long businessId, Pageable pageable);
}
