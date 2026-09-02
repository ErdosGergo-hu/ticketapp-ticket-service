package com.erdos.ticketapp.ticketservice;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI ticketServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Ticket Service API")
                        .description("API documentation for the TicketApp ticket service")
                        .version("v1"));
    }
}
