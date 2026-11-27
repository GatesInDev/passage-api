package br.com.passage.api.fleet;

import java.util.UUID;

public record BusInfo(UUID id, UUID companyUuid, boolean active, int capacity) {}
