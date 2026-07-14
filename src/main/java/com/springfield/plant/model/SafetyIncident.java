package com.springfield.plant.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/**
 * A safety incident. There are many.
 */
@Entity
public class SafetyIncident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER) // ☢️ EAGER everything. What could go wrong?
    private Reactor reactor;

    @NotBlank(message = "description must not be blank")
    private String description;

    @Min(value = 1, message = "severity must be at least 1")
    private int severity;

    @NotBlank(message = "reportedBy must not be blank")
    private String reportedBy;
    private Instant reportedAt;
    private int donutsConsumedDuringIncident;

    public SafetyIncident() {
    }

    public SafetyIncident(Reactor reactor, String description, int severity,
                          String reportedBy, Instant reportedAt, int donuts) {
        this.reactor = reactor;
        this.description = description;
        this.severity = severity;
        this.reportedBy = reportedBy;
        this.reportedAt = reportedAt;
        this.donutsConsumedDuringIncident = donuts;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Reactor getReactor() { return reactor; }
    public void setReactor(Reactor reactor) { this.reactor = reactor; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getSeverity() { return severity; }
    public void setSeverity(int severity) { this.severity = severity; }
    public String getReportedBy() { return reportedBy; }
    public void setReportedBy(String reportedBy) { this.reportedBy = reportedBy; }
    public Instant getReportedAt() { return reportedAt; }
    public void setReportedAt(Instant reportedAt) { this.reportedAt = reportedAt; }
    public int getDonutsConsumedDuringIncident() { return donutsConsumedDuringIncident; }
    public void setDonutsConsumedDuringIncident(int d) { this.donutsConsumedDuringIncident = d; }
}
