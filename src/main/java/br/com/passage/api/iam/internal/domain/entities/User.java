package br.com.passage.api.iam.internal.domain.entities;

import br.com.passage.api.iam.enums.UserRole;
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
 * Usuário do sistema, autenticado por e-mail e senha.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class User extends EntityBase implements UserDetails {

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    /** Login do usuário, único no sistema. */
    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    /** Hash BCrypt da senha. */
    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    private UserRole role;

    /** Empresa vinculada; {@code null} para administradores e passageiros. */
    @Column(name = "company_uuid")
    private UUID companyUuid;

    /** Perfil como autoridade do Spring Security, com o prefixo {@code ROLE_}. */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    /** Apenas usuários ativos e não excluídos conseguem autenticar. */
    @Override
    public boolean isEnabled() {
        return this.isActive() && !this.isDeleted();
    }
}