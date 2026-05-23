package com.media4all.tracking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Classe principal da aplicação Spring Boot.
 *
 * A anotação @SpringBootApplication combina três anotações:
 * - @Configuration: marca a classe como fonte de definições de beans
 * - @EnableAutoConfiguration: ativa a configuração automática do Spring Boot
 * - @ComponentScan: escaneia componentes no pacote atual e subpacotes
 *
 * Isso significa que qualquer @Controller, @Service, @Repository ou @Component
 * dentro de com.media4all.tracking.* será detectado automaticamente.
 */
@SpringBootApplication
public class TrackingApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrackingApplication.class, args);
    }
}
