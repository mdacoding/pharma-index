package de.pharmaindex.security;

import de.pharmaindex.b2b.domain.Partner;
import de.pharmaindex.b2b.repo.PartnerRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    public static final String PARTNER_ATTRIBUTE = "pharmaIndex.partner";
    public static final String API_KEY_HEADER = "X-API-Key";

    private final PartnerRepository partnerRepository;

    public ApiKeyAuthFilter(PartnerRepository partnerRepository) {
        this.partnerRepository = partnerRepository;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/")
                || path.equals("/index.html")
                || path.equals("/favicon.ico")
                || path.startsWith("/actuator/health")
                || path.startsWith("/actuator/info")
                || path.startsWith("/actuator/prometheus")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/h2-console")
                || "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String apiKey = request.getHeader(API_KEY_HEADER);
        if (apiKey == null || apiKey.isBlank()) {
            unauthorized(response, "Header X-API-Key fehlt");
            return;
        }
        Partner partner = partnerRepository.findByApiKeyAndActiveTrue(apiKey).orElse(null);
        if (partner == null) {
            unauthorized(response, "Ungültiger API-Key");
            return;
        }
        request.setAttribute(PARTNER_ATTRIBUTE, partner);
        filterChain.doFilter(request, response);
    }

    private static void unauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("""
                {"type":"about:blank","title":"Unauthorized","status":401,"detail":"%s"}
                """.formatted(message));
    }
}
