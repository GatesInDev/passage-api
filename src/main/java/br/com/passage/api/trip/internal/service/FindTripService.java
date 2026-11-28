package br.com.passage.api.trip.internal.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.passage.api.shared.domain.exceptions.ResourceNotFoundException;
import br.com.passage.api.trip.internal.domain.entities.Trip;
import br.com.passage.api.trip.internal.dto.TripResponse;
import br.com.passage.api.trip.internal.repository.TripRepository;
import lombok.RequiredArgsConstructor;

/**
 * Serviço responsável pela consulta de viagens.
 *
 * <p>Viagens excluídas não são retornadas.</p>
 *
 * <p>O escopo de empresa é definido pelo {@link TripAccessService}: o gestor de frota acessa
 * apenas as viagens da própria empresa e os demais perfis acessam as viagens de todas as empresas.</p>
 */
@Service 
@RequiredArgsConstructor 
public class FindTripService {
    
    private final TripRepository tripRepository;
    private final TripAccessService tripAccessService;

    /**
     * Busca uma viagem visível ao usuário autenticado.
     *
     * @param id identificador da viagem
     * @return viagem encontrada
     * @throws ResourceNotFoundException se a viagem não existir, estiver excluída ou pertencer a outra empresa
     */
    @Transactional(readOnly = true)
    public TripResponse execute(UUID id){
        Trip trip = tripRepository.findVisibleById(id, tripAccessService.visibleCompanyUuid())
                .orElseThrow(() -> new ResourceNotFoundException("Viagem não encontrada com o identificador: " + id));
        return TripResponse.from(trip);
    }

    /**
     * Lista as viagens visíveis ao usuário autenticado.
     *
     * @return viagens encontradas
     */
    @Transactional(readOnly = true)
    public List<TripResponse> executeAll(){
        return tripRepository.findAllVisible(tripAccessService.visibleCompanyUuid()).stream()
                .map(TripResponse::from)
                .toList();
    }
}
