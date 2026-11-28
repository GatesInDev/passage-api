package br.com.passage.api.ticket.internal.service;

import br.com.passage.api.iam.IamApi;
import br.com.passage.api.iam.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Serviço responsável por definir o escopo de empresa das operações de passagens.
 *
 * <ul>
 *     <li>ADMIN – acessa as passagens de todas as empresas;</li>
 *     <li>demais perfis – acessam apenas as passagens da empresa à qual estão vinculados.</li>
 * </ul>
 *
 * <p>Os perfis autorizados em cada operação são definidos no {@code TicketController}.</p>
 */
@Service
@RequiredArgsConstructor
public class TicketAccessService {

    private final IamApi iamApi;

    /**
     * Recupera a empresa à qual as operações do usuário autenticado são restritas.
     *
     * @return empresa do usuário, ou {@code null} para o administrador
     * @throws AccessDeniedException se o usuário não for administrador nem estiver vinculado a uma empresa
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

}
