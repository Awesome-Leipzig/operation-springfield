package com.springfield.plant.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Plant employee. Security clearance is a String. Yes, really.
 * ☢️ LEGACY ALERT: javax.persistence.
 */
@Entity
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String role;
    private String securityClearance;

    public Employee() {
    }

    public Employee(String name, String role, String securityClearance) {
        this.name = name;
        this.role = role;
        this.securityClearance = securityClearance;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getSecurityClearance() { return securityClearance; }
    public void setSecurityClearance(String securityClearance) { this.securityClearance = securityClearance; }
}
