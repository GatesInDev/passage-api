package br.com.passage.api.iam;

import br.com.passage.api.iam.enums.UserRole;
import br.com.passage.api.iam.internal.domain.entities.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class IamApiTests {

    private final IamApi iamApi = new IamApi();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void returnsAuthenticatedFleetManager() {
        UUID id = UUID.randomUUID();
        UUID companyUuid = UUID.randomUUID();
        authenticate(id, UserRole.FLEET_MANAGER, companyUuid);

        assertEquals(new CurrentUser(id, UserRole.FLEET_MANAGER, companyUuid), iamApi.currentUser());
    }

    @Test
    void returnsAdminAndCustomerWithoutCompany() {
        for (UserRole role : new UserRole[]{UserRole.ADMIN, UserRole.CUSTOMER}) {
            UUID id = UUID.randomUUID();
            authenticate(id, role, null);

            assertEquals(new CurrentUser(id, role, null), iamApi.currentUser());
        }
    }

    @Test
    void rejectsMissingAuthentication() {
        assertThrows(AuthenticationCredentialsNotFoundException.class, iamApi::currentUser);
    }

    @Test
    void rejectsAnonymousAuthentication() {
        SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken(
                "key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));

        assertThrows(AuthenticationCredentialsNotFoundException.class, iamApi::currentUser);
    }

    @Test
    void rejectsUnauthenticatedUser() {
        User user = new User("User", "user@example.com", "hash", UserRole.ADMIN, null);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, "password"));

        assertThrows(AuthenticationCredentialsNotFoundException.class, iamApi::currentUser);
    }

    @Test
    void rejectsUnexpectedPrincipal() {
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated("user@example.com", null,
                        AuthorityUtils.createAuthorityList("ROLE_ADMIN")));

        assertThrows(AuthenticationCredentialsNotFoundException.class, iamApi::currentUser);
    }

    private void authenticate(UUID id, UserRole role, UUID companyUuid) {
        User user = new User("User", "user@example.com", "hash", role, companyUuid);
        ReflectionTestUtils.setField(user, "id", id);
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(user, null, user.getAuthorities()));
    }
}
