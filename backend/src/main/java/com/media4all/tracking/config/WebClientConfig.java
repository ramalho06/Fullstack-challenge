package com.media4all.tracking.config;

import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

/**
 * Configuração do WebClient para comunicação com a API externa de GPS.
 *
 * Por que WebClient e não RestTemplate?
 * - RestTemplate está em modo de manutenção (deprecated para novos projetos).
 * - WebClient suporta chamadas assíncronas/reativas E síncronas (via .block()).
 * - WebClient permite configuração fluente de timeouts, headers e interceptors.
 * - É o cliente HTTP recomendado oficialmente pelo Spring desde o Spring 5.
 *
 * Decisões de configuração:
 * - Timeout de conexão: 5 segundos (tempo para estabelecer a conexão TCP).
 * - Timeout de resposta: 15 segundos (tempo máximo esperando a resposta completa).
 *   Valor mais alto que o padrão porque a API externa está hospedada no Render
 *   (free tier) e pode ter cold starts lentos.
 * - Header X-API-Key: enviado em todas as requisições automaticamente.
 * - Base URL: configurada uma vez, evitando repetição em cada chamada.
 */
@Configuration
public class WebClientConfig {

    @Value("${external.api.base-url}")
    private String baseUrl;

    @Value("${external.api.key}")
    private String apiKey;

    /**
     * Bean nomeado para o WebClient da API externa.
     *
     * O nome "externalApiWebClient" torna explícito qual WebClient está sendo
     * injetado, caso futuramente haja mais de um WebClient no projeto.
     * Para injetar, use: @Qualifier("externalApiWebClient")
     */
    @Bean(name = "externalApiWebClient")
    public WebClient externalApiWebClient() {

        // Configura o cliente HTTP do Netty (camada de transporte)
        HttpClient httpClient = HttpClient.create()
                // Timeout para estabelecer a conexão TCP (5 segundos)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5_000)
                // Timeout para receber a resposta completa (15 segundos)
                .responseTimeout(Duration.ofSeconds(15));

        return WebClient.builder()
                // URL base: todas as chamadas serão relativas a esta URL
                .baseUrl(baseUrl)
                // Header de autenticação enviado em TODAS as requisições
                .defaultHeader("X-API-Key", apiKey)
                // Conecta o WebClient ao HttpClient do Netty configurado acima
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
