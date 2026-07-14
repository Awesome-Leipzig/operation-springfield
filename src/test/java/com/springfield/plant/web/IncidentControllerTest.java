package com.springfield.plant.web;

import com.springfield.plant.config.PlantSecurityProperties;
import com.springfield.plant.model.Reactor;
import com.springfield.plant.model.SafetyIncident;
import com.springfield.plant.service.IncidentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IncidentController.class)
@EnableConfigurationProperties(PlantSecurityProperties.class)
class IncidentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IncidentService incidentService;

    @Test
    @DisplayName("shouldReturnAllIncidents_whenIncidentsExist")
    void shouldReturnAllIncidents_whenIncidentsExist() throws Exception {
        var reactor = new Reactor("Old Bessie", "7G", "ONLINE", 480, Instant.now());
        var incident = new SafetyIncident(reactor, "control rod used as back scratcher", 2,
                "Homer Simpson", Instant.now(), 6);
        when(incidentService.findAll()).thenReturn(List.of(incident));

        mockMvc.perform(get("/api/incidents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reportedBy").value("Homer Simpson"));
    }

    @Test
    @DisplayName("shouldReportIncident_whenPosted")
    void shouldReportIncident_whenPosted() throws Exception {
        var reactor = new Reactor("Old Bessie", "7G", "ONLINE", 480, Instant.now());
        when(incidentService.report(any(SafetyIncident.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var requestBody = """
                {"description":"glowing rat spotted","severity":4,"reportedBy":"Waylon Smithers","donutsConsumedDuringIncident":1}""";

        mockMvc.perform(post("/api/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportedBy").value("Waylon Smithers"));
    }

    @Test
    @DisplayName("shouldReturnLeaderboard_whenRequested")
    void shouldReturnLeaderboard_whenRequested() throws Exception {
        when(incidentService.incidentsPerReporter()).thenReturn(Map.of("Homer Simpson", 2));

        mockMvc.perform(get("/api/incidents/leaderboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.['Homer Simpson']").value(2));
    }

    @Test
    @DisplayName("shouldReturnDonutTotal_whenRequested")
    void shouldReturnDonutTotal_whenRequested() throws Exception {
        when(incidentService.totalDonuts()).thenReturn(21);

        mockMvc.perform(get("/api/incidents/donuts"))
                .andExpect(status().isOk())
                .andExpect(content().string("Donuts consumed during incidents to date: 21 \uD83C\uDF69"));
    }
}
