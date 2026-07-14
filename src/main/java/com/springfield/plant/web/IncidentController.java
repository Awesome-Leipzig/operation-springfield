package com.springfield.plant.web;

import com.springfield.plant.model.SafetyIncident;
import com.springfield.plant.service.IncidentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Safety incident REST API.
 */
@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @GetMapping
    public List<SafetyIncident> all() {
        return incidentService.findAll();
    }

    @PostMapping
    public SafetyIncident report(@RequestBody SafetyIncident incident) {
        if (incident.getReportedAt() == null) {
            incident.setReportedAt(Instant.now());
        }
        return incidentService.report(incident);
    }

    @GetMapping("/alarming")
    public List<SafetyIncident> alarming() {
        return incidentService.auditAlarming();
    }

    @GetMapping("/leaderboard")
    public Map<String, Integer> leaderboard() {
        return incidentService.incidentsPerReporter();
    }

    @GetMapping("/donuts")
    public String donuts() {
        return "Donuts consumed during incidents to date: " + incidentService.totalDonuts() + " 🍩";
    }
}
