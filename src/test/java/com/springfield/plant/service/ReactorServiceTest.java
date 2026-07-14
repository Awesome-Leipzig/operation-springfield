package com.springfield.plant.service;

import com.springfield.plant.model.Reactor;
import com.springfield.plant.repository.ReactorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReactorServiceTest {

    private ReactorRepository repository;
    private ReactorService service;

    @BeforeEach
    void setUp() {
        repository = mock(ReactorRepository.class);
        service = new ReactorService(repository);
    }

    @Test
    @DisplayName("shouldSumOnlineOutput_whenOnlineReactorsExist")
    void shouldSumOnlineOutput_whenOnlineReactorsExist() {
        var a = new Reactor("Old Bessie", "7G", "ONLINE", 480, Instant.now());
        var b = new Reactor("Core Blimey", "7G", "ONLINE", 512, Instant.now());
        when(repository.findByStatus("ONLINE")).thenReturn(List.of(a, b));

        assertThat(service.totalOnlineOutputMw()).isEqualTo(992);
    }

    @Test
    @DisplayName("shouldMarkReactorOverdue_whenInspectionIsMissing")
    void shouldMarkReactorOverdue_whenInspectionIsMissing() {
        var never = new Reactor("Backup Bart", "3B", "OFFLINE", 0, null);
        when(repository.findAll()).thenReturn(List.of(never));

        assertThat(service.overdueForInspection(90)).hasSize(1);
    }

    @Test
    @DisplayName("shouldReportFineBanner_whenNoReactorsPresent")
    void shouldReportFineBanner_whenNoReactorsPresent() {
        when(repository.findByStatus(anyString())).thenReturn(List.of());

        assertThat(service.statusBanner()).contains("Everything is fine.");
    }

    @Test
    @DisplayName("shouldUpdateLastInspection_whenReactorExists")
    void shouldUpdateLastInspection_whenReactorExists() {
        var reactor = new Reactor("Old Bessie", "7G", "ONLINE", 480, null);
        reactor.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(reactor));
        when(repository.save(any(Reactor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var inspected = service.inspect(1L);

        assertThat(inspected).isPresent();
        assertThat(inspected.get().getLastInspection()).isNotNull();
    }

    @Test
    @DisplayName("shouldReturnEmpty_whenInspectingUnknownReactor")
    void shouldReturnEmpty_whenInspectingUnknownReactor() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThat(service.inspect(99L)).isEmpty();
    }
}
