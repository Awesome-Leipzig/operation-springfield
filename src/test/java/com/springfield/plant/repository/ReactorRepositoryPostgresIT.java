package com.springfield.plant.repository;

import com.springfield.plant.model.Reactor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Side Quest: Testcontainers integration test — proves the JPA repository layer
 * works against a real PostgreSQL instance (matching the Phase 4 production
 * datastore), not just the H2 in-memory database used by default in dev/test.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class ReactorRepositoryPostgresIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private ReactorRepository reactorRepository;

    @Test
    @DisplayName("shouldPersistAndQueryReactor_againstRealPostgresContainer")
    void shouldPersistAndQueryReactor_againstRealPostgresContainer() {
        var saved = reactorRepository.save(new Reactor("Test Core", "7G", "ONLINE", 500, Instant.now()));

        var online = reactorRepository.findByStatus("ONLINE");

        assertThat(online).extracting(Reactor::getId).contains(saved.getId());
        assertThat(reactorRepository.findById(saved.getId())).isPresent();
    }

    @Test
    @DisplayName("shouldFilterBySector_againstRealPostgresContainer")
    void shouldFilterBySector_againstRealPostgresContainer() {
        reactorRepository.save(new Reactor("Old Bessie", "7G", "ONLINE", 480, Instant.now()));
        reactorRepository.save(new Reactor("The Inconvenience", "6F", "MELTDOWN-ISH", 730, Instant.now()));

        var sectorReactors = reactorRepository.findBySector("7G");

        assertThat(sectorReactors).extracting(Reactor::getName).contains("Old Bessie");
    }
}
