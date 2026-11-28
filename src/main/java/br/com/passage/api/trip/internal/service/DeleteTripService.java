package br.com.passage.api.trip.internal.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.passage.api.shared.domain.exceptions.ResourceNotFoundException;
import br.com.passage.api.trip.internal.domain.entities.Trip;
import br.com.passage.api.trip.internal.repository.TripRepository;
import lombok.RequiredArgsConstructor;

/**
 * Serviço responsável pela exclusão de viagens.
 *
 * <p>A exclusão é lógica: a viagem é marcada como excluída e inativa, permanece no banco e
 * deixa de ocupar a agenda do ônibus e do motorista.</p>
 */
@Service 
@RequiredArgsConstructor 
public class DeleteTripService {
    
    private final TripRepository tripRepository;
    private final TripAccessService tripAccessService;
    
    /**
     * Exclui logicamente uma viagem visível ao usuário autenticado.
     *
     * @param id identificador da viagem
     * @throws ResourceNotFoundException se a viagem não existir, estiver excluída ou pertencer a outra empresa
     */
    @Transactional
    public void execute(UUID id){
        Trip trip = tripRepository.findVisibleById(id, tripAccessService.visibleCompanyUuid())
                .orElseThrow(() -> new ResourceNotFoundException("Viagem não encontrada com o identificador: " + id));

        trip.setDeleted(true);
        trip.setActive(false);
        tripRepository.save(trip);
    }
}
