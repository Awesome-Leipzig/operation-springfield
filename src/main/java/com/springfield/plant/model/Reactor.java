package com.springfield.plant.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

/**
 * A reactor core in the plant.
 * ☢️ LEGACY ALERT: javax.persistence (Boot 3 needs jakarta.*), java.util.Date,
 * status as a free-text String instead of an enum.
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
    private Date lastInspection;

    public Reactor() {
    }

    public Reactor(String name, String sector, String status, Integer thermalOutputMw, Date lastInspection) {
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
    public Date getLastInspection() { return lastInspection; }
    public void setLastInspection(Date lastInspection) { this.lastInspection = lastInspection; }
}
