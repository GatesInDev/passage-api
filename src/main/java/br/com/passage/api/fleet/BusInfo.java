package br.com.passage.api.fleet;

import java.util.UUID;

/**
 * Representa os dados de um ônibus expostos aos demais módulos.
 *
 * @param id          identificador do ônibus
 * @param companyUuid empresa proprietária do ônibus
 * @param active      indica se o ônibus está ativo
 * @param capacity    quantidade de assentos ativos e não excluídos
 */
public record BusInfo(UUID id, UUID companyUuid, boolean active, int capacity) {}
