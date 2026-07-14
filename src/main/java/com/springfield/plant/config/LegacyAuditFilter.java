package com.springfield.plant.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Component
public class LegacyAuditFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(LegacyAuditFilter.class);

    private static final Set<String> WRITE_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");
    private static final Set<String> PROTECTED_PREFIXES = Set.of("/api/incidents", "/api/reactors");

    private final PlantSecurityProperties plantSecurityProperties;

    public LegacyAuditFilter(PlantSecurityProperties plantSecurityProperties) {
        this.plantSecurityProperties = plantSecurityProperties;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest httpRequest)) {
            chain.doFilter(request, response);
            return;
        }

        String method = httpRequest.getMethod();
        String uri = httpRequest.getRequestURI();

        log.info("AUDIT {} {} (api key configured: {})", method, uri,
                plantSecurityProperties.apiKeyConfigured());

        if (plantSecurityProperties.apiKeyConfigured()
                && WRITE_METHODS.contains(method)
                && isProtectedPath(uri)) {

            String providedKey = httpRequest.getHeader("X-Api-Key");
            if (!plantSecurityProperties.apiKey().equals(providedKey)) {
                log.warn("AUDIT DENIED {} {} — missing or invalid API key", method, uri);
                HttpServletResponse httpResponse = (HttpServletResponse) response;
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                httpResponse.setContentType("application/json");
                httpResponse.getWriter().write("{\"error\":\"Unauthorized\"}");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private boolean isProtectedPath(String uri) {
        for (String prefix : PROTECTED_PREFIXES) {
            if (uri.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
