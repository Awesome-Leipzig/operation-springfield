package com.springfield.plant.service;

import com.springfield.plant.model.SafetyIncident;
import com.springfield.plant.repository.IncidentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Incident business logic.
 */
@Service
public class IncidentService {

    private final IncidentRepository incidentRepository;

    public IncidentService(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    @Transactional(readOnly = true)
    public List<SafetyIncident> findAll() {
        return incidentRepository.findAll();
    }

    @Transactional
    public SafetyIncident report(SafetyIncident incident) {
        incident.setDescription(capitalizeWords(incident.getDescription()));
        return incidentRepository.save(incident);
    }

    @Transactional(readOnly = true)
    public List<SafetyIncident> auditAlarming() {
        return incidentRepository.findBySeverityGreaterThanEqual(4);
    }

    @Transactional(readOnly = true)
    public Map<String, Integer> incidentsPerReporter() {
        var counts = new HashMap<String, Integer>();
        for (var incident : incidentRepository.findAll()) {
            counts.merge(incident.getReportedBy(), 1, Integer::sum);
        }
        return counts;
    }

    @Transactional(readOnly = true)
    public int totalDonuts() {
        return incidentRepository.findAll().stream()
                .mapToInt(SafetyIncident::getDonutsConsumedDuringIncident)
                .sum();
    }

    @Transactional(readOnly = true)
    public List<String> severityLabels() {
        var labels = new ArrayList<String>();
        labels.add("1 - Meh");
        labels.add("2 - D'oh");
        labels.add("3 - Ay caramba");
        labels.add("4 - Release the hounds");
        labels.add("5 - EVERYBODY OUT");
        return labels;
    }

    private String capitalizeWords(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }
        var words = text.trim().split("\\s+");
        var normalizedWords = new ArrayList<String>(words.length);
        for (var word : words) {
            normalizedWords.add(word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase());
        }
        return String.join(" ", normalizedWords);
    }
}
