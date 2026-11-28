package br.com.passage.api.fleet;

import java.util.UUID;

/**
 * Representa os dados de um motorista expostos aos demais módulos.
 *
 * @param id          identificador do motorista
 * @param companyUuid empresa à qual o motorista está vinculado
 * @param active      indica se o motorista está ativo
 */
public record DriverInfo(UUID id, UUID companyUuid, boolean active) {}
