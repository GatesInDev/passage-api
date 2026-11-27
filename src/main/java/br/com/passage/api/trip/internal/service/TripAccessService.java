package br.com.passage.api.trip.internal.service;

import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import br.com.passage.api.iam.CurrentUser;
import br.com.passage.api.iam.IamApi;
import br.com.passage.api.iam.enums.UserRole;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TripAccessService {

    private final IamApi iamApi;

    public UUID visibleCompanyUuid() {
        CurrentUser user = iamApi.currentUser();

        if (user.role() != UserRole.FLEET_MANAGER) {
            return null;                      // ADMIN e os demais perfis veem tudo
        }

        if (user.companyUuid() == null) {
            throw new AccessDeniedException("Gestor sem empresa vinculada não pode acessar viagens.");
        }

        return user.companyUuid();            // gestor vê só a própria empresa
    }
}