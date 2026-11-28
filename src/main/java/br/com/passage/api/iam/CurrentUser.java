package br.com.passage.api.iam;

import br.com.passage.api.iam.enums.UserRole;

import java.util.UUID;

/**
 * Representa o usuário autenticado na requisição atual.
 *
 * <p>É a forma pela qual os demais módulos conhecem o usuário logado, sem depender da
 * entidade {@code User} nem do Spring Security.</p>
 *
 * @param id          identificador do usuário
 * @param role        perfil de acesso do usuário
 * @param companyUuid empresa vinculada ao usuário; {@code null} para administradores e passageiros
 */
public record CurrentUser(UUID id, UserRole role, UUID companyUuid) {
}
