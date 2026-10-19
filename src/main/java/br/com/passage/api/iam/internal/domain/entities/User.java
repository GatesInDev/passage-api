package br.com.passage.api.iam.internal.domain.entities;

import br.com.passage.api.iam.internal.domain.enums.UserRole;
import br.com.passage.api.shared.domain.entities.EntityBase;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Entidade de usuário do sistema integrada ao ecossistema Spring Security.
 * <p>
 * Implementa {@link UserDetails} para permitir autenticação via e-mail e senha (hash BCrypt),
 * além de herdar os atributos de auditoria e identificação pública da {@link EntityBase}.
 * </p>
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class User extends EntityBase implements UserDetails {

    /**
     * Nome completo do usuário ou operador.
     */
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    /**
     * Endereço de e-mail institucional ou pessoal utilizado como identificador único de login (username).
     */
    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    /**
     * Hash da senha criptografada (gerada via BCryptPasswordEncoder).
     */
    @Column(name = "password", nullable = false)
    private String password;

    /**
     * Papel funcional do usuário no sistema para autorização e controle de permissões.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    private UserRole role;

    /**
     * Identificador público (UUID) da transportadora vinculada ao operador, caso aplicável.
     * Nulo para administradores globais ou passageiros avulsos.
     */
    @Column(name = "company_uuid")
    private UUID companyUuid;

    /**
     * Retorna a coleção de autoridades concedidas ao usuário pelo Spring Security,
     * prefixando o enum de papel com o padrão {@code ROLE_}.
     *
     * @return Lista de autoridades concedidas.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }

    /**
     * Retorna o identificador de login do usuário (neste caso, o e-mail cadastrado).
     *
     * @return E-mail do usuário.
     */
    @Override
    public String getUsername() {
        return this.email;
    }

    /**
     * Indica se a conta do usuário está ativa e apta a se autenticar,
     * validando as flags de ativação e exclusão lógica da {@link EntityBase}.
     *
     * @return {@code true} se o usuário estiver ativo e não excluído logicamente; caso contrário, {@code false}.
     */
    @Override
    public boolean isEnabled() {
        return this.isActive() && !this.isDeleted();
    }
}