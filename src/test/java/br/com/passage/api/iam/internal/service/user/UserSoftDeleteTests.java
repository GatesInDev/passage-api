package br.com.passage.api.iam.internal.service.user;

import br.com.passage.api.iam.enums.UserRole;
import br.com.passage.api.iam.internal.domain.entities.User;
import br.com.passage.api.iam.internal.repository.UserRepository;
import br.com.passage.api.shared.domain.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserSoftDeleteTests {
    private final UserRepository repository = mock(UserRepository.class);
    private final UUID id = UUID.randomUUID();
    private final User user = new User("User", "user@example.com", "hash", UserRole.CUSTOMER, null);

    @Test
    void hidesDeletedUserFromFindById() {
        user.setDeleted(true);
        when(repository.findById(id)).thenReturn(Optional.of(user));
        assertThrows(ResourceNotFoundException.class, () -> new FindUserService(repository).execute(id));
    }

    @Test
    void returnsExistingUser() {
        when(repository.findById(id)).thenReturn(Optional.of(user));
        assertEquals(user.getEmail(), new FindUserService(repository).execute(id).email());
    }

    @Test
    void secondDeletionFailsAndDoesNotSaveAgain() {
        when(repository.findById(id)).thenReturn(Optional.of(user));
        DeleteUserService service = new DeleteUserService(repository);
        service.execute(id);
        assertTrue(user.isDeleted());
        assertFalse(user.isActive());
        assertThrows(ResourceNotFoundException.class, () -> service.execute(id));
        verify(repository, times(1)).save(user);
    }

    @Test
    void cannotUpdateDeletedUser() {
        user.setDeleted(true);
        when(repository.findById(id)).thenReturn(Optional.of(user));
        assertThrows(ResourceNotFoundException.class, () -> new UpdateUserService(repository).execute(id, null));
        verify(repository, never()).save(any());
    }

    @Test
    void missingUserIsNotFoundForReadAndDelete() {
        when(repository.findById(id)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> new FindUserService(repository).execute(id));
        assertThrows(ResourceNotFoundException.class, () -> new DeleteUserService(repository).execute(id));
    }
}
