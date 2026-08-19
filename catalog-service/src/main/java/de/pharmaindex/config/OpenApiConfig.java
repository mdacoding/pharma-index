package de.pharmaindex.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI pharmaIndexOpenApi() {
        SecurityScheme apiKey = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("X-API-Key")
                .description("B2B-Partnerkey. Demo: demo-partner-key");
        return new OpenAPI()
                .info(new Info()
                        .title("PharmaIndex Catalog API")
                        .version("1.0.0")
                        .description("""
                                Stammdaten- und Qualitätssicherungsservice für Fertigarzneimittel.
                                Portfolio-Projekt (synthetische Demodaten, kein medizinischer Rat).
                                """)
                        .contact(new Contact().name("PharmaIndex Portfolio")))
                .servers(List.of(new Server().url("http://localhost:8080").description("Lokal")))
                .components(new Components().addSecuritySchemes("ApiKey", apiKey))
                .addSecurityItem(new SecurityRequirement().addList("ApiKey"));
    }
}
