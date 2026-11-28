package br.com.passage.api.fleet.internal.service.bus;

import br.com.passage.api.fleet.internal.domain.entities.Bus;
import br.com.passage.api.fleet.internal.domain.entities.Company;
import br.com.passage.api.fleet.internal.dto.bus.CreateBusRequest;
import br.com.passage.api.fleet.internal.dto.bus.BusResponse;
import br.com.passage.api.fleet.internal.repository.BusRepository;
import br.com.passage.api.fleet.internal.service.FleetAccessService;
import br.com.passage.api.shared.domain.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço responsável pelo cadastro de ônibus.
 *
 * <p>O ônibus é cadastrado ativo e sem assentos, com capacidade zero. A placa deve ser única
 * no sistema.</p>
 *
 * <p>O escopo de empresa é definido pelo {@link FleetAccessService}: o administrador acessa
 * todas as empresas e o gestor de frota apenas a própria.</p>
 */
@Service
@RequiredArgsConstructor
public class CreateBusService {

    private final BusRepository busRepository;
    private final FleetAccessService fleetAccessService;

    /**
     * Cadastra um ônibus.
     *
     * @param request dados do ônibus
     * @return ônibus cadastrado
     * @throws BusinessException         se a placa já estiver em uso ou a empresa não for informada
     * @throws ResourceNotFoundException se a empresa não existir ou estiver excluída
     */
    @Transactional
    public BusResponse execute(CreateBusRequest request) {
        Company company = fleetAccessService.resolveCompany(request.companyUuid());
        if (busRepository.existsByLicensePlate(request.licensePlate())) {
            throw new BusinessException("Já existe um ônibus cadastrado com a placa " + request.licensePlate());
        }
        Bus bus = Bus.builder()
                .company(company)
                .code(request.code())
                .model(request.model())
                .manufacturer(request.manufacturer())
                .licensePlate(request.licensePlate())
                .isActive(true)
                .build();
        return BusResponse.from(busRepository.save(bus));
    }
}
