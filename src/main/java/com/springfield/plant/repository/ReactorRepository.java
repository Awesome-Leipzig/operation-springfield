package com.springfield.plant.repository;

import com.springfield.plant.model.Reactor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReactorRepository extends JpaRepository<Reactor, Long> {
    List<Reactor> findByStatus(String status);
    List<Reactor> findBySector(String sector);
}
