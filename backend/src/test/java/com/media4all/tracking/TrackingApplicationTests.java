package com.media4all.tracking;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Teste de integração que verifica se o contexto Spring
 * carrega corretamente (todas as beans são resolvidas).
 *
 * Usa o perfil "test" para carregar o contexto sem depender de
 * variáveis de ambiente locais ou de um MySQL externo.
 */
@ActiveProfiles("test")
@SpringBootTest
class TrackingApplicationTests {

    @Test
    void contextLoads() {
        // Se o contexto Spring carregou sem exceção, o teste passa.
        // Isso valida que todas as configurações e beans estão corretas.
    }
}
