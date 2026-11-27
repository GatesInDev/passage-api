package br.com.passage.api.fleet;

import java.util.UUID;

public record DriverInfo(UUID id, UUID companyUuid, boolean active) {}
