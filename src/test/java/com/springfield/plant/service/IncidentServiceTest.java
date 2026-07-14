package com.springfield.plant.service;

import com.springfield.plant.model.Reactor;
import com.springfield.plant.model.SafetyIncident;
import com.springfield.plant.repository.IncidentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IncidentServiceTest {

    private IncidentRepository repository;
    private IncidentService service;

    @BeforeEach
    void setUp() {
        repository = mock(IncidentRepository.class);
        service = new IncidentService(repository);
    }

    @Test
    @DisplayName("shouldCapitalizeDescription_whenIncidentIsReported")
    void shouldCapitalizeDescription_whenIncidentIsReported() {
        var reactor = new Reactor("Old Bessie", "7G", "ONLINE", 480, Instant.now());
        var incident = new SafetyIncident(reactor, "control rod used as back scratcher", 2,
                "Homer Simpson", Instant.now(), 6);
        when(repository.save(any(SafetyIncident.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var saved = service.report(incident);

        assertThat(saved.getDescription()).isEqualTo("Control Rod Used As Back Scratcher");
        verify(repository).save(incident);
    }

    @Test
    @DisplayName("shouldFindAlarmingIncidents_whenSeverityIsHigh")
    void shouldFindAlarmingIncidents_whenSeverityIsHigh() {
        var reactor = new Reactor("The Inconvenience", "6F", "MELTDOWN-ISH", 730, Instant.now());
        var alarming = new SafetyIncident(reactor, "core temperature exceeded 'the red bit'", 5,
                "Carl Carlson", Instant.now(), 0);
        when(repository.findBySeverityGreaterThanEqual(4)).thenReturn(List.of(alarming));

        assertThat(service.auditAlarming()).containsExactly(alarming);
    }

    @Test
    @DisplayName("shouldCountIncidentsPerReporter_whenIncidentsExist")
    void shouldCountIncidentsPerReporter_whenIncidentsExist() {
        var reactor = new Reactor("Old Bessie", "7G", "ONLINE", 480, Instant.now());
        var first = new SafetyIncident(reactor, "issue one", 2, "Homer Simpson", Instant.now(), 1);
        var second = new SafetyIncident(reactor, "issue two", 3, "Homer Simpson", Instant.now(), 2);
        when(repository.findAll()).thenReturn(List.of(first, second));

        assertThat(service.incidentsPerReporter()).containsEntry("Homer Simpson", 2);
    }

    @Test
    @DisplayName("shouldSumDonuts_whenIncidentsExist")
    void shouldSumDonuts_whenIncidentsExist() {
        var reactor = new Reactor("Old Bessie", "7G", "ONLINE", 480, Instant.now());
        var first = new SafetyIncident(reactor, "issue one", 2, "Homer Simpson", Instant.now(), 6);
        var second = new SafetyIncident(reactor, "issue two", 3, "Carl Carlson", Instant.now(), 12);
        when(repository.findAll()).thenReturn(List.of(first, second));

        assertThat(service.totalDonuts()).isEqualTo(18);
    }

    @Test
    @DisplayName("shouldReturnFiveSeverityLabels_always")
    void shouldReturnFiveSeverityLabels_always() {
        assertThat(service.severityLabels()).hasSize(5);
    }
}
