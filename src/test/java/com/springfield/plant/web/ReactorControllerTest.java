package com.springfield.plant.web;

import com.springfield.plant.config.PlantSecurityProperties;
import com.springfield.plant.model.Reactor;
import com.springfield.plant.service.ReactorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReactorController.class)
// LegacyAuditFilter (a @Component Filter) is picked up by the @WebMvcTest slice, but its
// PlantSecurityProperties dependency is normally only registered via @ConfigurationPropertiesScan
// on the main application class, which isn't loaded in a web-layer slice.
@EnableConfigurationProperties(PlantSecurityProperties.class)
class ReactorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReactorService reactorService;

    @Test
    @DisplayName("shouldReturnAllReactors_whenReactorsExist")
    void shouldReturnAllReactors_whenReactorsExist() throws Exception {
        var reactor = new Reactor("Old Bessie", "7G", "ONLINE", 480, Instant.now());
        when(reactorService.findAll()).thenReturn(List.of(reactor));

        mockMvc.perform(get("/api/reactors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Old Bessie"));
    }

    @Test
    @DisplayName("shouldReturn404_whenReactorIdNotFound")
    void shouldReturn404_whenReactorIdNotFound() throws Exception {
        when(reactorService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/reactors/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("shouldReturnReactor_whenReactorIdFound")
    void shouldReturnReactor_whenReactorIdFound() throws Exception {
        var reactor = new Reactor("Core Blimey", "7G", "ONLINE", 512, Instant.now());
        reactor.setId(1L);
        when(reactorService.findById(1L)).thenReturn(Optional.of(reactor));

        mockMvc.perform(get("/api/reactors/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Core Blimey"));
    }

    @Test
    @DisplayName("shouldReturnTotalOutput_whenRequested")
    void shouldReturnTotalOutput_whenRequested() throws Exception {
        when(reactorService.totalOnlineOutputMw()).thenReturn(992);

        mockMvc.perform(get("/api/reactors/output"))
                .andExpect(status().isOk())
                .andExpect(content().string("Total online output: 992 MW"));
    }
}
