package com.springfield.plant.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class LegacyAuditFilterTest {

    private FilterChain chain;
    private LegacyAuditFilter filter;

    @BeforeEach
    void setUp() {
        chain = mock(FilterChain.class);
    }

    @Test
    @DisplayName("shouldContinueFilterChain_whenApiKeyNotConfigured")
    void shouldContinueFilterChain_whenApiKeyNotConfigured() throws Exception {
        filter = new LegacyAuditFilter(new PlantSecurityProperties(null));
        var request = mock(HttpServletRequest.class);
        var response = mock(HttpServletResponse.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/reactors");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verifyNoInteractions(response);
    }

    @Test
    @DisplayName("shouldContinueFilterChain_whenRequestIsNotHttp")
    void shouldContinueFilterChain_whenRequestIsNotHttp() throws Exception {
        filter = new LegacyAuditFilter(new PlantSecurityProperties("configured-key"));
        var request = mock(jakarta.servlet.ServletRequest.class);
        var response = mock(jakarta.servlet.ServletResponse.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    @DisplayName("shouldNotBlockRequest_regardlessOfApiKeyConfiguration")
    void shouldNotBlockRequest_regardlessOfApiKeyConfiguration() throws Exception {
        // ☢️ There is no auth check here on purpose (TRIAGE.md S7: no Spring Security
        // on the classpath). This filter only audits; it never rejects a request.
        filter = new LegacyAuditFilter(new PlantSecurityProperties("configured-key"));
        var request = mock(HttpServletRequest.class);
        var response = mock(HttpServletResponse.class);
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/incidents");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(any(), any());
    }
}
