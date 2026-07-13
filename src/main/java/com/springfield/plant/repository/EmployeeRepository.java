package com.springfield.plant.repository;

import com.springfield.plant.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Employee findByName(String name);
}
