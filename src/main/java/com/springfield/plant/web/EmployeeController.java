package com.springfield.plant.web;

import com.springfield.plant.model.Employee;
import com.springfield.plant.repository.EmployeeRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Employee REST API.
 * ☢️ LEGACY ALERT: controller talks straight to the repository. No service, no DTOs,
 * entities serialized directly to JSON.
 */
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeRepository employeeRepository;

    public EmployeeController(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @GetMapping
    public List<Employee> all() {
        return employeeRepository.findAll();
    }

    @GetMapping("/{name}")
    public Employee byName(@PathVariable String name) {
        return employeeRepository.findByName(name); // ☢️ returns null → 200 with empty body
    }
}
