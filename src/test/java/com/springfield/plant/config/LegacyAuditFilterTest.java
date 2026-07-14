package com.springfield.plant.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
    @DisplayName("shouldAllowRequest_whenGetOnProtectedPathEvenWithApiKeyConfigured")
    void shouldAllowRequest_whenGetOnProtectedPathEvenWithApiKeyConfigured() throws Exception {
        // Only write methods (POST/PUT/PATCH/DELETE) on protected paths are enforced;
        // reads are always audited-only.
        filter = new LegacyAuditFilter(new PlantSecurityProperties("configured-key"));
        var request = mock(HttpServletRequest.class);
        var response = mock(HttpServletResponse.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/incidents");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verifyNoInteractions(response);
    }

    @Test
    @DisplayName("shouldReject401_whenWritingToProtectedPathWithMissingApiKey")
    void shouldReject401_whenWritingToProtectedPathWithMissingApiKey() throws Exception {
        filter = new LegacyAuditFilter(new PlantSecurityProperties("configured-key"));
        var request = mock(HttpServletRequest.class);
        var response = mock(HttpServletResponse.class);
        var writer = new PrintWriter(new StringWriter());
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/incidents");
        when(request.getHeader("X-Api-Key")).thenReturn(null);
        when(response.getWriter()).thenReturn(writer);

        filter.doFilter(request, response, chain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("shouldAllowRequest_whenWritingToProtectedPathWithCorrectApiKey")
    void shouldAllowRequest_whenWritingToProtectedPathWithCorrectApiKey() throws Exception {
        filter = new LegacyAuditFilter(new PlantSecurityProperties("configured-key"));
        var request = mock(HttpServletRequest.class);
        var response = mock(HttpServletResponse.class);
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/incidents");
        when(request.getHeader("X-Api-Key")).thenReturn("configured-key");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    @DisplayName("shouldAllowWriteToProtectedPath_whenApiKeyNotConfiguredAtAll")
    void shouldAllowWriteToProtectedPath_whenApiKeyNotConfiguredAtAll() throws Exception {
        // Matches the pre-existing "no Spring Security on the classpath" gap
        // (TRIAGE.md S7): enforcement only kicks in once an API key IS configured.
        filter = new LegacyAuditFilter(new PlantSecurityProperties(null));
        var request = mock(HttpServletRequest.class);
        var response = mock(HttpServletResponse.class);
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/incidents");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }
}
