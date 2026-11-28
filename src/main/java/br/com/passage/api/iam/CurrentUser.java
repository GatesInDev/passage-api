package br.com.passage.api.iam;

import br.com.passage.api.iam.enums.UserRole;

import java.util.UUID;

/**
 * Usuário autenticado na requisição atual, exposto aos demais módulos.
 *
 * @param id          identificador do usuário
 * @param role        perfil de acesso
 * @param companyUuid empresa vinculada; {@code null} para administradores e passageiros
 */
public record CurrentUser(UUID id, UserRole role, UUID companyUuid) {
}
