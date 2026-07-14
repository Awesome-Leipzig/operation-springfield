package com.springfield.plant.service;

import com.springfield.plant.model.Employee;
import com.springfield.plant.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EmployeeServiceTest {

    private EmployeeRepository repository;
    private EmployeeService service;

    @BeforeEach
    void setUp() {
        repository = mock(EmployeeRepository.class);
        service = new EmployeeService(repository);
    }

    @Test
    @DisplayName("shouldReturnAllEmployees_whenEmployeesExist")
    void shouldReturnAllEmployees_whenEmployeesExist() {
        var homer = new Employee("Homer Simpson", "Safety Inspector", "TOP SECRET (misfiled)");
        when(repository.findAll()).thenReturn(List.of(homer));

        assertThat(service.findAll()).containsExactly(homer);
    }

    @Test
    @DisplayName("shouldFindEmployeeByName_whenEmployeeExists")
    void shouldFindEmployeeByName_whenEmployeeExists() {
        var smithers = new Employee("Waylon Smithers", "Executive Assistant", "ALL OF THEM");
        when(repository.findByName("Waylon Smithers")).thenReturn(Optional.of(smithers));

        assertThat(service.findByName("Waylon Smithers")).contains(smithers);
    }

    @Test
    @DisplayName("shouldReturnEmpty_whenEmployeeNameNotFound")
    void shouldReturnEmpty_whenEmployeeNameNotFound() {
        when(repository.findByName("Nobody")).thenReturn(Optional.empty());

        assertThat(service.findByName("Nobody")).isEmpty();
    }
}
