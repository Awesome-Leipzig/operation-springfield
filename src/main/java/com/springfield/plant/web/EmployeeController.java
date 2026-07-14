package com.springfield.plant.web;

import com.springfield.plant.model.Employee;
import com.springfield.plant.service.EmployeeService;
import org.springframework.http.ResponseEntity;
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

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public List<Employee> all() {
        return employeeService.findAll();
    }

    @GetMapping("/{name}")
    public ResponseEntity<Employee> byName(@PathVariable String name) {
        return employeeService.findByName(name)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
