package com.springfield.plant.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.Instant;

/**
 * A reactor core in the plant.
 */
@Entity
public class Reactor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String sector;
    /** One of: "ONLINE", "OFFLINE", "MELTDOWN-ISH". Probably. Nobody checks. */
    private String status;
    private Integer thermalOutputMw;
    private Instant lastInspection;

    public Reactor() {
    }

    public Reactor(String name, String sector, String status, Integer thermalOutputMw, Instant lastInspection) {
        this.name = name;
        this.sector = sector;
        this.status = status;
        this.thermalOutputMw = thermalOutputMw;
        this.lastInspection = lastInspection;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getThermalOutputMw() { return thermalOutputMw; }
    public void setThermalOutputMw(Integer thermalOutputMw) { this.thermalOutputMw = thermalOutputMw; }
    public Instant getLastInspection() { return lastInspection; }
    public void setLastInspection(Instant lastInspection) { this.lastInspection = lastInspection; }
}
