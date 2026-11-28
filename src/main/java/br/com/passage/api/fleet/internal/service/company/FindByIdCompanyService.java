package br.com.passage.api.fleet.internal.service.company;

import br.com.passage.api.fleet.internal.dto.company.CompanyResponse;
import br.com.passage.api.fleet.internal.service.FleetAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Serviço responsável pela consulta de uma empresa.
 *
 * <p>O gestor de frota pode consultar apenas a própria empresa.</p>
 */
@Service
@RequiredArgsConstructor
public class FindByIdCompanyService {

    private final FleetAccessService fleetAccessService;

    /**
     * Busca uma empresa pelo identificador.
     *
     * @param id identificador da empresa
     * @return empresa encontrada
     * @throws AccessDeniedException     se o gestor tentar acessar outra empresa
     * @throws ResourceNotFoundException se a empresa não existir ou estiver excluída
     */
    @Transactional(readOnly = true)
    public CompanyResponse execute(UUID id) {
        return CompanyResponse.from(fleetAccessService.requireCompany(id));
    }
}
