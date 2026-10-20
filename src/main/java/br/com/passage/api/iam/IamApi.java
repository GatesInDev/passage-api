package br.com.passage.api.iam;

import br.com.passage.api.iam.internal.domain.entities.User;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class IamApi {

    public CurrentUser currentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof User user)) {
            throw new AuthenticationCredentialsNotFoundException("Usuário não autenticado");
        }

        return new CurrentUser(user.getId(), user.getRole(), user.getCompanyUuid());
    }
}
