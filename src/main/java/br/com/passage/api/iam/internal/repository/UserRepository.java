package br.com.passage.api.iam.internal.repository;

import br.com.passage.api.iam.internal.domain.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositório de acesso aos usuários.
 */
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Busca o usuário pelo e-mail, utilizado como login.
     *
     * @param email e-mail do usuário
     * @return usuário encontrado, se existir
     */
    Optional<UserDetails> findByEmail(String email);
    /**
     * Busca o usuário pelo identificador público.
     *
     * <p>Considera também os usuários excluídos logicamente.</p>
     *
     * @param id identificador do usuário
     * @return usuário encontrado, se existir
     */
    Optional<User> findById(UUID id);
    /**
     * Verifica se já existe um usuário cadastrado com o e-mail informado.
     *
     * @param email e-mail do usuário
     * @return {@code true} se o e-mail já estiver em uso
     */
    boolean existsByEmail(String email);
}