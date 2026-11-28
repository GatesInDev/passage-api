package br.com.passage.api.fleet.internal.service.driver;

import br.com.passage.api.fleet.internal.domain.entities.Driver;
import br.com.passage.api.fleet.internal.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Serviço responsável pela exclusão de motoristas.
 *
 * <p>A exclusão é lógica: o motorista é marcado como excluído e inativo e permanece no banco.</p>
 */
@Service
@RequiredArgsConstructor
public class DeleteDriverService {

    private final DriverRepository driverRepository;
    private final FindDriverService findDriverService;

    /**
     * Exclui logicamente um motorista visível ao usuário autenticado.
     *
     * @param id identificador do motorista
     * @throws ResourceNotFoundException se o motorista não existir, estiver excluído ou pertencer a outra empresa
     */
    @Transactional
    public void execute(UUID id) {
        Driver driver = findDriverService.findEntity(id);
        driver.setDeleted(true);
        driver.setActive(false);
        driverRepository.save(driver);
    }
}
