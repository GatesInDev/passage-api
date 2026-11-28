package br.com.passage.api.fleet;

import java.util.UUID;

/**
 * Dados de um ônibus expostos aos demais módulos.
 *
 * @param companyUuid empresa dona do ônibus
 * @param active      se o ônibus está ativo
 * @param capacity    quantidade de assentos ativos e não excluídos
 */
public record BusInfo(UUID id, UUID companyUuid, boolean active, int capacity) {}
