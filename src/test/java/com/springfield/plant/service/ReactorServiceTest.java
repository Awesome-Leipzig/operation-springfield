package com.springfield.plant.service;

import com.springfield.plant.model.Reactor;
import com.springfield.plant.repository.ReactorRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * ☢️ LEGACY ALERT: JUnit 4 asserts, no @DisplayName, mock setup circa 2015.
 */
public class ReactorServiceTest {

    private ReactorRepository repository;
    private ReactorService service;

    @Before
    public void setUp() {
        repository = Mockito.mock(ReactorRepository.class);
        service = new ReactorService(repository);
    }

    @Test
    public void totalOnlineOutputAddsUp() {
        Reactor a = new Reactor("Old Bessie", "7G", "ONLINE", 480, new Date());
        Reactor b = new Reactor("Core Blimey", "7G", "ONLINE", 512, new Date());
        Mockito.when(repository.findByStatus("ONLINE")).thenReturn(Arrays.asList(a, b));

        assertEquals(Integer.valueOf(992), service.totalOnlineOutputMw());
    }

    @Test
    public void reactorNeverInspectedIsOverdue() {
        Reactor never = new Reactor("Backup Bart", "3B", "OFFLINE", 0, null);
        Mockito.when(repository.findAll()).thenReturn(Collections.singletonList(never));

        assertEquals(1, service.overdueForInspection(90).size());
    }

    @Test
    public void bannerInsistsEverythingIsFine() {
        Mockito.when(repository.findByStatus(Mockito.anyString())).thenReturn(Collections.<Reactor>emptyList());

        assertTrue(service.statusBanner().contains("Everything is fine."));
    }
}
