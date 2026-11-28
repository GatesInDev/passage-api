package br.com.passage.api.fleet;

import java.util.UUID;

/**
 * Dados de um motorista expostos aos demais módulos.
 *
 * @param companyUuid empresa à qual o motorista está vinculado
 * @param active      se o motorista está ativo
 */
public record DriverInfo(UUID id, UUID companyUuid, boolean active) {}
