package br.com.passage.api.fleet.internal.web;

import br.com.passage.api.fleet.internal.dto.bus.CreateBusRequest;
import br.com.passage.api.fleet.internal.dto.bus.UpdateBusRequest;
import br.com.passage.api.fleet.internal.dto.bus.BusResponse;
import br.com.passage.api.fleet.internal.service.bus.CreateBusService;
import br.com.passage.api.fleet.internal.service.bus.DeleteBusService;
import br.com.passage.api.fleet.internal.service.bus.FindBusService;
import br.com.passage.api.fleet.internal.service.bus.UpdateBusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/buses")
@RequiredArgsConstructor
@Tag(name = "Buses", description = "Manage company buses.")
public class BusController {

    private final CreateBusService createBusService;
    private final FindBusService findBusService;
    private final UpdateBusService updateBusService;
    private final DeleteBusService deleteBusService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    @Operation(summary = "Create a bus")
    public ResponseEntity<BusResponse> create(@Valid @RequestBody CreateBusRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(createBusService.execute(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    @Operation(summary = "List accessible buses")
    public ResponseEntity<List<BusResponse>> findAll() {
        return ResponseEntity.ok(findBusService.executeAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    @Operation(summary = "Find a bus")
    public ResponseEntity<BusResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(findBusService.execute(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    @Operation(summary = "Update a bus")
    public ResponseEntity<BusResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateBusRequest request) {
        return ResponseEntity.ok(updateBusService.execute(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    @Operation(summary = "Soft delete a bus")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deleteBusService.execute(id);
        return ResponseEntity.noContent().build();
    }
}
