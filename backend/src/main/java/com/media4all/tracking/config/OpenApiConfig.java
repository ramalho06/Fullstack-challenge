package com.media4all.tracking.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI teamsTrackingOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Teams Tracking System API")
                        .description("""
                                API para rastreamento de equipes externas, sincronização com API GPS externa,
                                histórico de rotas, check-ins, geofences e monitoramento operacional.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Media4All Challenge")
                                .url("https://github.com/ramalho06/Fullstack-challenge")))
                .tags(List.of(
                        new Tag().name("Health").description("Verificação de disponibilidade da aplicação"),
                        new Tag().name("Agents").description("CRUD e consulta de agentes de campo"),
                        new Tag().name("Locations").description("Consulta de localizações atuais dos agentes"),
                        new Tag().name("Check-ins").description("Consulta e registro manual de check-ins"),
                        new Tag().name("Geofences").description("Consulta de cercas geográficas"),
                        new Tag().name("Routes").description("Histórico de rota diária e distância Haversine"),
                        new Tag().name("Sync commands").description("Gatilhos manuais de sincronização externa"),
                        new Tag().name("Sync monitoring").description("Monitoramento operacional das sincronizações")
                ));
    }
}
