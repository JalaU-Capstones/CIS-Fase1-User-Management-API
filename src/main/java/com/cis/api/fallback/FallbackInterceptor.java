package com.cis.api.fallback;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Interceptor that:
 * <ul>
 *     <li>stores the request API version (v1/v2) in {@link RequestVersionContext}</li>
 *     <li>blocks write operations (non-GET) when fallback is active with HTTP 503 and a maintenance message</li>
 * </ul>
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.fallback", name = "enabled", havingValue = "true")
public class FallbackInterceptor implements HandlerInterceptor {

    static final String MAINTENANCE_MESSAGE = "Our system is currently undergoing planned maintenance. Please try again later.\n"
            + "Recommendation: Until further notice, avoid creating, updating, or deleting any resources. Your data is safe, but modifications may not be persisted. If you cannot find recently created items, please wait for the IT department to contact you.";

    private final DatabaseFallbackService databaseFallbackService;

    public FallbackInterceptor(DatabaseFallbackService databaseFallbackService) {
        this.databaseFallbackService = databaseFallbackService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String version = extractVersion(request.getRequestURI());
        RequestVersionContext.setOriginalVersion(version);

        // If both databases are down (and feature enabled), fail fast for all methods.
        databaseFallbackService.assertAtLeastOneDatabaseUp(version);

        if (databaseFallbackService.isFallbackActive() && isWriteMethod(request.getMethod())) {
            log.warn("Write request blocked due to active database fallback (method={}, uri={})",
                    request.getMethod(), request.getRequestURI());
            writeMaintenanceResponse(response);
            return false;
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        RequestVersionContext.clear();
    }

    private boolean isWriteMethod(String method) {
        return method != null && !"GET".equalsIgnoreCase(method);
    }

    private String extractVersion(String uri) {
        if (uri != null && uri.startsWith("/api/v2/")) {
            return "v2";
        }
        return "v1";
    }

    private void writeMaintenanceResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/plain");
        response.getWriter().write(MAINTENANCE_MESSAGE);
    }
}
