package com.springfield.plant.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.Date;

/**
 * A safety incident. There are many.
 * ☢️ LEGACY ALERT: javax.persistence, java.util.Date, severity as int (1..5, undocumented).
 */
@Entity
public class SafetyIncident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER) // ☢️ EAGER everything. What could go wrong?
    private Reactor reactor;

    private String description;
    private int severity;
    private String reportedBy;
    private Date reportedAt;
    private int donutsConsumedDuringIncident;

    public SafetyIncident() {
    }

    public SafetyIncident(Reactor reactor, String description, int severity,
                          String reportedBy, Date reportedAt, int donuts) {
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
    public Date getReportedAt() { return reportedAt; }
    public void setReportedAt(Date reportedAt) { this.reportedAt = reportedAt; }
    public int getDonutsConsumedDuringIncident() { return donutsConsumedDuringIncident; }
    public void setDonutsConsumedDuringIncident(int d) { this.donutsConsumedDuringIncident = d; }
}
