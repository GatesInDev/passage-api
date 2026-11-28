package br.com.passage.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Classe de inicialização da Passage API.
 *
 * <p>A aplicação é um monólito modular organizado com Spring Modulith. Cada pacote de primeiro
 * nível representa um módulo de negócio:</p>
 *
 * <ul>
 *     <li>{@code iam} – autenticação, usuários e perfis de acesso;</li>
 *     <li>{@code fleet} – empresas, ônibus, assentos e motoristas;</li>
 *     <li>{@code route} – rotas atendidas pelas empresas;</li>
 *     <li>{@code trip} – viagens agendadas das rotas;</li>
 *     <li>{@code ticket} – venda de passagens;</li>
 *     <li>{@code integration} – emissão do documento fiscal (BP-e);</li>
 *     <li>{@code shared} – entidade base e tratamento de erros comuns.</li>
 * </ul>
 *
 * <p>A auditoria do Spring Data JPA é habilitada aqui, preenchendo as datas de criação e
 * alteração de todas as entidades que estendem {@code EntityBase}.</p>
 */
@EnableJpaAuditing
@SpringBootApplication
public class ApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiApplication.class, args);
	}

}
