package com.springfield.plant.service;

import com.springfield.plant.model.Reactor;
import com.springfield.plant.repository.ReactorRepository;
import com.springfield.plant.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Reactor business logic.
 */
@Service
public class ReactorService {

    private static final Logger log = LoggerFactory.getLogger(ReactorService.class);

    private final ReactorRepository reactorRepository;

    public ReactorService(ReactorRepository reactorRepository) {
        this.reactorRepository = reactorRepository;
    }

    @Transactional(readOnly = true)
    public List<Reactor> findAll() {
        return reactorRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Reactor> findById(Long id) {
        return reactorRepository.findById(id);
    }

    @Transactional
    public Reactor save(Reactor reactor) {
        Reactor saved = reactorRepository.save(reactor);
        log.info("AUDIT: reactor persisted id={} name={} sector={} status={} thermalOutputMw={}",
                saved.getId(),
                saved.getName(),
                saved.getSector(),
                saved.getStatus(),
                saved.getThermalOutputMw());
        return saved;
    }

    /**
     * Side Quest: records a reactor inspection right now, clearing it off the
     * overdue list. Returns empty if no reactor exists with that id.
     */
    @Transactional
    public Optional<Reactor> inspect(Long id) {
        return reactorRepository.findById(id).map(reactor -> {
            reactor.setLastInspection(Instant.now());
            Reactor saved = reactorRepository.save(reactor);
            log.info("AUDIT: reactor inspected id={} name={} at={}", saved.getId(), saved.getName(),
                    saved.getLastInspection());
            return saved;
        });
    }

    @Transactional(readOnly = true)
    public long totalOnlineOutputMw() {
        return reactorRepository.findByStatus("ONLINE").stream()
                .map(Reactor::getThermalOutputMw)
                .filter(output -> output != null)
                .mapToLong(Integer::longValue)
                .sum();
    }

    @Transactional(readOnly = true)
    public List<Reactor> overdueForInspection(int maxDays) {
        var cutoff = DateUtils.daysAgo(maxDays);
        return reactorRepository.findAll().stream()
                .filter(reactor -> reactor.getLastInspection() == null || reactor.getLastInspection().isBefore(cutoff))
                .toList();
    }

    @Transactional(readOnly = true)
    public String statusBanner() {
        return "SNPP STATUS :: %d online / %d melting / %d MW total. Everything is fine."
                .formatted(
                        reactorRepository.findByStatus("ONLINE").size(),
                        reactorRepository.findByStatus("MELTDOWN-ISH").size(),
                        totalOnlineOutputMw());
    }
}
