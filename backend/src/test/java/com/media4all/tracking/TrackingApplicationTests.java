package com.media4all.tracking;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Teste de integração que verifica se o contexto Spring
 * carrega corretamente (todas as beans são resolvidas).
 *
 * Este teste requer que o MySQL esteja acessível.
 * Para rodar sem banco, veja os testes unitários individuais.
 */
@SpringBootTest
class TrackingApplicationTests {

    @Test
    void contextLoads() {
        // Se o contexto Spring carregou sem exceção, o teste passa.
        // Isso valida que todas as configurações e beans estão corretas.
    }
}
