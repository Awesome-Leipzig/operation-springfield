package com.springfield.plant.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LegacyAuditFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(LegacyAuditFilter.class);

    private final PlantSecurityProperties plantSecurityProperties;

    public LegacyAuditFilter(PlantSecurityProperties plantSecurityProperties) {
        this.plantSecurityProperties = plantSecurityProperties;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpServletRequest) {
            log.info("AUDIT {} {} (api key configured: {})",
                    httpServletRequest.getMethod(),
                    httpServletRequest.getRequestURI(),
                    plantSecurityProperties.apiKeyConfigured());
        }
        chain.doFilter(request, response);
    }
}
