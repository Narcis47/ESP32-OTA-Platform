package com.narcis.esp32ota;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class Esp32otaApplication {

	public static void main(String[] args) {
		SpringApplication.run(Esp32otaApplication.class, args);
	}

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {return new BCryptPasswordEncoder();}

	@Bean
	public RestTemplate restTemplate() {return new RestTemplate();}

	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI()
				.addSecurityItem(new SecurityRequirement().addList("Bearer Auth"))
				.components(new Components()
						.addSecuritySchemes("Bearer Auth", new SecurityScheme()
								.name("Bearer Auth")
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT")));
	}
}
