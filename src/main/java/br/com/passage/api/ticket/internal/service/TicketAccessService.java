package br.com.passage.api.ticket.internal.service;

import br.com.passage.api.iam.IamApi;
import br.com.passage.api.iam.enums.UserRole;
import br.com.passage.api.shared.domain.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Resolve a empresa do usuário autenticado para o módulo de passagens.
 *
 * <p>Equivalente ao {@code FleetAccessService} do módulo de frota, com os
 * perfis que operam a bilheteria.</p>
 */
@Service
@RequiredArgsConstructor
public class TicketAccessService {

    private final IamApi iamApi;

    /**
     * Empresa a ser aplicada nas consultas.
     *
     * @return {@code null} para administradores, que enxergam todas as
     *         empresas; caso contrário, a empresa do usuário.
     */
    public UUID currentCompanyId() {
        var user = iamApi.currentUser();

        if (user.role() == UserRole.ADMIN) {
            return null;
        }

        if (user.companyUuid() == null) {
            throw new AccessDeniedException(
                    "Operação de bilheteria exige um usuário vinculado a uma empresa.");
        }

        return user.companyUuid();
    }

    /**
     * Empresa que deve ser gravada em uma passagem nova.
     *
     * <p>O operador vende sempre pela própria empresa, independentemente do
     * que vier no payload. O administrador precisa informar a empresa.</p>
     *
     * <p>TODO: quando a {@code TripApi} expuser a viagem, derivar a empresa
     * dela e remover o campo do payload.</p>
     */
    public UUID resolveCompanyForCreation(UUID requestedCompanyId) {
        UUID currentCompanyId = currentCompanyId();

        if (currentCompanyId != null) {
            return currentCompanyId;
        }

        if (requestedCompanyId == null) {
            throw new BusinessException(
                    "Para usuários ADMIN, o UUID da empresa deve ser informado no payload.");
        }

        return requestedCompanyId;
    }
}
