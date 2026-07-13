package com.springfield.plant.config;

import com.springfield.plant.util.SecretConstants;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * "Security" filter.
 * ☢️ LEGACY ALERT: javax.servlet (Boot 3 needs jakarta.servlet),
 * System.out logging, and a hardcoded backdoor token check that does nothing useful.
 */
@Component
public class LegacyAuditFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest http = (HttpServletRequest) request;

        // ☢️ System.out is our observability strategy.
        System.out.println("[AUDIT] " + http.getMethod() + " " + http.getRequestURI()
                + " (key configured: " + SecretConstants.PLANT_API_KEY.length() + " chars, very secure)");

        // ☢️ The "backdoor": header check that grants... absolutely nothing. Smithers insisted.
        String token = http.getHeader("X-Smithers-Token");
        if (token != null && token.equals(SecretConstants.SMITHERS_BACKDOOR_TOKEN)) {
            System.out.println("[AUDIT] Smithers detected. Releasing the hounds is now enabled.");
        }

        chain.doFilter(request, response);
    }
}
