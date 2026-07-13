package com.springfield.plant.service;

import com.springfield.plant.model.SafetyIncident;
import com.springfield.plant.repository.IncidentRepository;
import org.apache.commons.text.WordUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * Incident business logic.
 * ☢️ LEGACY ALERT: Hashtable (hello 1996), commons-text 1.8 (CVE bait),
 * severity magic numbers.
 */
@Service
public class IncidentService {

    private final IncidentRepository incidentRepository;

    public IncidentService(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    public List<SafetyIncident> findAll() {
        return incidentRepository.findAll();
    }

    public SafetyIncident report(SafetyIncident incident) {
        // Make descriptions look professional for the auditors.
        incident.setDescription(WordUtils.capitalizeFully(incident.getDescription()));
        return incidentRepository.save(incident);
    }

    /** Incidents that would definitely alarm an auditor (severity >= 4). */
    public List<SafetyIncident> auditAlarming() {
        return incidentRepository.findBySeverityGreaterThanEqual(4);
    }

    /** Incident counts per reporter, using the world's most legacy Map. */
    public Map<String, Integer> incidentsPerReporter() {
        Hashtable<String, Integer> counts = new Hashtable<String, Integer>(); // ☢️
        List<SafetyIncident> all = incidentRepository.findAll();
        for (int i = 0; i < all.size(); i++) {
            SafetyIncident inc = all.get(i);
            String who = inc.getReportedBy();
            if (counts.containsKey(who)) {
                counts.put(who, new Integer(counts.get(who).intValue() + 1)); // ☢️
            } else {
                counts.put(who, new Integer(1)); // ☢️
            }
        }
        return counts;
    }

    /** Total donuts consumed during incidents. A critical plant KPI. */
    public int totalDonuts() {
        int total = 0;
        List<SafetyIncident> all = incidentRepository.findAll();
        for (SafetyIncident inc : all) {
            total += inc.getDonutsConsumedDuringIncident();
        }
        return total;
    }

    public List<String> severityLabels() {
        // ☢️ Magic numbers with meanings that live only in Carl's head.
        List<String> labels = new ArrayList<String>();
        labels.add("1 - Meh");
        labels.add("2 - D'oh");
        labels.add("3 - Ay caramba");
        labels.add("4 - Release the hounds");
        labels.add("5 - EVERYBODY OUT");
        return labels;
    }
}
