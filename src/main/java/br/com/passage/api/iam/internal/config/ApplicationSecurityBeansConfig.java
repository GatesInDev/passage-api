package br.com.passage.api.iam.internal.config;

import br.com.passage.api.iam.internal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuração dos componentes de autenticação do Spring Security.
 *
 * <p>Disponibiliza a busca de usuários por e-mail, o gerenciador de autenticação e o
 * codificador de senhas BCrypt.</p>
 */
@Configuration
@RequiredArgsConstructor
public class ApplicationSecurityBeansConfig {

    private final UserRepository userRepository;

    /**
     * Carrega o usuário pelo e-mail, utilizado como login.
     *
     * @return serviço de busca de usuários
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));
    }

    /**
     * Gerenciador de autenticação configurado pelo Spring Security.
     *
     * @param config configuração de autenticação da aplicação
     * @return gerenciador de autenticação
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Codificador utilizado para gerar e verificar o hash das senhas.
     *
     * @return codificador BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}