package com.cis.api.interceptor;

import com.cis.api.config.SystemStateConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class V1SunsetInterceptor implements HandlerInterceptor {

    static final String WARNING_HEADER_NAME  = "Warning";
    static final String WARNING_HEADER_VALUE = "299 - \"API v1 is deprecated. Please migrate to /api/v2/\"";

    private static final Map<String, Object> MAINTENANCE_BODY = Map.of(
            "error", "Service is under maintenance. Write operations are temporarily disabled. Please try again shortly."
    );

    private static final Map<String, Object> SUNSET_BODY = Map.of(
            "status", 410,
            "error", "Gone",
            "message", "API v1 has been sunset. Please migrate to /api/v2/"
    );

    private final SystemStateConfig systemStateConfig;
    private final ObjectMapper      objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest  request,
                             HttpServletResponse response,
                             Object              handler) throws Exception {

        boolean migrationRunning = systemStateConfig.isMigrationRunning();
        boolean sunset           = systemStateConfig.isV1Sunset();
        String  method           = request.getMethod();
        String  path             = request.getRequestURI();
        boolean isV1             = path.startsWith("/api/v1/") || path.equals("/api/v1");

        if (migrationRunning) {
            if (!HttpMethod.GET.matches(method)) {
                log.warn("Migration running — blocking {} {} with 503 Service Unavailable",
                        method, path);
                response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                objectMapper.writeValue(response.getWriter(), MAINTENANCE_BODY);
                return false;
            }
            return true;
        }

        if (sunset && isV1) {
            if (HttpMethod.GET.matches(method)) {
                log.debug("V1 sunset active — appending deprecation Warning header for GET {}", path);
                response.addHeader(WARNING_HEADER_NAME, WARNING_HEADER_VALUE);
                return true;
            }

            log.warn("V1 sunset active — blocking {} {} with 410 Gone", method, path);
            response.setStatus(HttpStatus.GONE.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getWriter(), SUNSET_BODY);
            return false;
        }

        return true;
    }
}