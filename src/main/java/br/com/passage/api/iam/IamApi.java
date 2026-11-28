package br.com.passage.api.iam;

import br.com.passage.api.iam.internal.domain.entities.User;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * API pública do módulo de identidade e acesso.
 *
 * <p>Ponto de acesso para que outros módulos consultem o usuário autenticado sem depender
 * das classes internas do módulo.</p>
 */
@Component
public class IamApi {

    /**
     * Recupera o usuário autenticado na requisição atual.
     *
     * @return usuário autenticado
     * @throws AuthenticationCredentialsNotFoundException se não houver usuário autenticado
     */
    public CurrentUser currentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof User user)) {
            throw new AuthenticationCredentialsNotFoundException("Usuário não autenticado");
        }

        return new CurrentUser(user.getId(), user.getRole(), user.getCompanyUuid());
    }
}
