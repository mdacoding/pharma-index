package de.pharmaindex.security;

import de.pharmaindex.config.PharmaIndexProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class RateLimitFilter extends OncePerRequestFilter {

    private final PharmaIndexProperties properties;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public RateLimitFilter(PharmaIndexProperties properties) {
        this.properties = properties;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/")
                || path.equals("/index.html")
                || path.startsWith("/actuator")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String key = request.getHeader(ApiKeyAuthFilter.API_KEY_HEADER);
        if (key == null) {
            key = request.getRemoteAddr();
        }
        Window window = windows.computeIfAbsent(key, ignored -> new Window());
        if (!window.allow(properties.getRatelimit().getRequestsPerMinute())) {
            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write("""
                    {"type":"about:blank","title":"Too Many Requests","status":429,"detail":"Rate-Limit überschritten"}
                    """);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private static final class Window {
        private Instant started = Instant.now();
        private final AtomicInteger count = new AtomicInteger();

        synchronized boolean allow(int limit) {
            Instant now = Instant.now();
            if (now.isAfter(started.plusSeconds(60))) {
                started = now;
                count.set(0);
            }
            return count.incrementAndGet() <= limit;
        }
    }
}
