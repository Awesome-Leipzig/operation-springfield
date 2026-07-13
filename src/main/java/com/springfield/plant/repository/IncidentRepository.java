package com.springfield.plant.repository;

import com.springfield.plant.model.SafetyIncident;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidentRepository extends JpaRepository<SafetyIncident, Long> {
    List<SafetyIncident> findBySeverityGreaterThanEqual(int severity);
    List<SafetyIncident> findByReportedBy(String reportedBy);
}
