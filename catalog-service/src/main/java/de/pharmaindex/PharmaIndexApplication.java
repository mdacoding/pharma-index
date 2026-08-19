package de.pharmaindex;

import de.pharmaindex.config.PharmaIndexProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
@EnableConfigurationProperties(PharmaIndexProperties.class)
public class PharmaIndexApplication {

    public static void main(String[] args) {
        SpringApplication.run(PharmaIndexApplication.class, args);
    }
}
