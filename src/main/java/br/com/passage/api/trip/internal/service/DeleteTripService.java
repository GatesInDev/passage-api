package br.com.passage.api.trip.internal.service;

import java.util.UUID;

import org.springframework.security.access.AccessDeniedException; 
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.passage.api.iam.CurrentUser;
import br.com.passage.api.iam.IamApi;
import br.com.passage.api.iam.enums.UserRole;
import br.com.passage.api.shared.domain.exceptions.ResourceNotFoundException;
import br.com.passage.api.trip.internal.domain.entities.Trip;
import br.com.passage.api.trip.internal.repository.TripRepository;
import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class DeleteTripService {
    
    private final TripRepository tripRepository;
    private final IamApi iamApi;

    @Transactional
    public void execute(UUID id){
        Trip trip = tripRepository.findById(id)
                .filter(found -> !found.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Viagem não encontrada com o identificador: " + id));

        CurrentUser user = iamApi.currentUser();

        if(user.role() != UserRole.ADMIN && !trip.belongsTo(user.companyUuid())){
            throw new AccessDeniedException("Usuário não tem permissão para deletar esta viagem.");
        }

        trip.setDeleted(true);
        trip.setActive(false);
        tripRepository.save(trip);
    }
}
