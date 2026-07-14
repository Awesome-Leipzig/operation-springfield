package com.springfield.plant.web;

import com.springfield.plant.config.PlantSecurityProperties;
import com.springfield.plant.model.Employee;
import com.springfield.plant.service.EmployeeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
@EnableConfigurationProperties(PlantSecurityProperties.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeService employeeService;

    @Test
    @DisplayName("shouldReturnAllEmployees_whenEmployeesExist")
    void shouldReturnAllEmployees_whenEmployeesExist() throws Exception {
        var homer = new Employee("Homer Simpson", "Safety Inspector", "TOP SECRET (misfiled)");
        when(employeeService.findAll()).thenReturn(List.of(homer));

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Homer Simpson"));
    }

    @Test
    @DisplayName("shouldReturn404_whenEmployeeNameNotFound")
    void shouldReturn404_whenEmployeeNameNotFound() throws Exception {
        when(employeeService.findByName("Nobody")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/employees/Nobody"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("shouldReturnEmployee_whenNameFound")
    void shouldReturnEmployee_whenNameFound() throws Exception {
        var smithers = new Employee("Waylon Smithers", "Executive Assistant", "ALL OF THEM");
        when(employeeService.findByName("Waylon Smithers")).thenReturn(Optional.of(smithers));

        mockMvc.perform(get("/api/employees/Waylon Smithers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("Executive Assistant"));
    }
}
