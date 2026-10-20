package br.com.passage.api.iam;

import br.com.passage.api.iam.enums.UserRole;

import java.util.UUID;

public record CurrentUser(UUID id, UserRole role, UUID companyUuid) {
}
