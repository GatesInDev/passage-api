package br.com.passage.api.fleet.internal.service.company;

import br.com.passage.api.fleet.internal.domain.entities.Company;
import br.com.passage.api.fleet.internal.dto.company.UpdateCompanyRequest;
import br.com.passage.api.fleet.internal.repository.CompanyRepository;
import br.com.passage.api.iam.CurrentUser;
import br.com.passage.api.iam.IamApi;
import br.com.passage.api.iam.enums.UserRole;
import br.com.passage.api.shared.domain.exceptions.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UpdateCompanyServiceTests {
    private final CompanyRepository repository = mock(CompanyRepository.class);
    private final IamApi iamApi = mock(IamApi.class);
    private final UpdateCompanyService service = new UpdateCompanyService(repository, iamApi);
    private final UUID companyId = UUID.randomUUID();
    private final UpdateCompanyRequest request = new UpdateCompanyRequest(
            "Empresa", "Nome fantasia", "12345678000195", "ISENTO", "ANTT");

    @Test
    void rejectsManagerFromAnotherCompanyBeforeAccessingRepository() {
        login(UserRole.FLEET_MANAGER, UUID.randomUUID());
        assertThrows(AccessDeniedException.class, () -> service.execute(companyId, request));
        verifyNoInteractions(repository);
    }

    @Test
    void rejectsManagerWithoutCompany() {
        login(UserRole.FLEET_MANAGER, null);
        assertThrows(AccessDeniedException.class, () -> service.execute(companyId, request));
        verifyNoInteractions(repository);
    }

    @Test
    void rejectsOtherRolesEvenWithMatchingCompany() {
        login(UserRole.CUSTOMER, companyId);
        assertThrows(AccessDeniedException.class, () -> service.execute(companyId, request));
        verifyNoInteractions(repository);
    }

    @Test
    void allowsManagerToUpdateOwnCompanyWithSameDocument() {
        login(UserRole.FLEET_MANAGER, companyId);
        Company company = existingCompany();
        assertEquals(request.documentId(), service.execute(companyId, request).documentId());
        verify(repository).existsByDocumentIdAndIdNot(request.documentId(), companyId);
        verify(repository).save(company);
    }

    @Test
    void allowsAdminToUpdateAnyCompany() {
        login(UserRole.ADMIN, null);
        Company company = existingCompany();
        service.execute(companyId, request);
        verify(repository).save(company);
    }

    @Test
    void rejectsDuplicateDocumentWithoutChangingCompany() {
        login(UserRole.ADMIN, null);
        Company company = existingCompany();
        when(repository.existsByDocumentIdAndIdNot(request.documentId(), companyId)).thenReturn(true);
        assertThrows(BusinessException.class, () -> service.execute(companyId, request));
        assertEquals("Original", company.getCorporateName());
        verify(repository, never()).save(any());
    }

    private void login(UserRole role, UUID companyUuid) {
        when(iamApi.currentUser()).thenReturn(new CurrentUser(UUID.randomUUID(), role, companyUuid));
    }

    private Company existingCompany() {
        Company company = Company.builder().corporateName("Original").documentId(request.documentId()).build();
        when(repository.findById(companyId)).thenReturn(Optional.of(company));
        return company;
    }
}
