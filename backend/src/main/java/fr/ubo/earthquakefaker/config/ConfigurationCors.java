package fr.ubo.earthquakefaker.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Autorise le frontend React (Vite sur 5173, ou 3000) à appeler l'API. La console H2 n'est pas concernée. */
@Configuration
public class ConfigurationCors implements WebMvcConfigurer {

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/api/**")
				.allowedOrigins("http://localhost:5173", "http://localhost:3000")
				.allowedMethods("GET", "POST", "OPTIONS");
	}
}
