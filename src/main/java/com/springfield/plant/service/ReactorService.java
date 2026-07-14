package com.springfield.plant.service;

import com.springfield.plant.model.Reactor;
import com.springfield.plant.repository.ReactorRepository;
import com.springfield.plant.util.DateUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Reactor business logic.
 */
@Service
public class ReactorService {

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
        return reactorRepository.save(reactor);
    }

    @Transactional(readOnly = true)
    public Integer totalOnlineOutputMw() {
        return reactorRepository.findByStatus("ONLINE").stream()
                .map(Reactor::getThermalOutputMw)
                .filter(output -> output != null)
                .mapToInt(Integer::intValue)
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
