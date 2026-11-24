package br.com.passage.api.fleet.internal.web;

import br.com.passage.api.fleet.internal.dto.seat.CreateSeatRequest;
import br.com.passage.api.fleet.internal.dto.seat.SeatResponse;
import br.com.passage.api.fleet.internal.dto.seat.UpdateSeatRequest;
import br.com.passage.api.fleet.internal.service.seat.CreateSeatService;
import br.com.passage.api.fleet.internal.service.seat.DeleteSeatService;
import br.com.passage.api.fleet.internal.service.seat.FindSeatService;
import br.com.passage.api.fleet.internal.service.seat.UpdateSeatService;
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
@RequestMapping("/api/v1/buses/{busId}/seats")
@RequiredArgsConstructor
@Tag(name = "Seats", description = "Manage seats within an accessible bus.")
public class SeatController {

    private final CreateSeatService createSeatService;
    private final FindSeatService findSeatService;
    private final UpdateSeatService updateSeatService;
    private final DeleteSeatService deleteSeatService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    @Operation(summary = "Create a seat")
    public ResponseEntity<SeatResponse> create(@PathVariable UUID busId, @Valid @RequestBody CreateSeatRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(createSeatService.execute(busId, request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    @Operation(summary = "List seats on a bus")
    public ResponseEntity<List<SeatResponse>> findAll(@PathVariable UUID busId) {
        return ResponseEntity.ok(findSeatService.executeAll(busId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    @Operation(summary = "Find a seat on a bus")
    public ResponseEntity<SeatResponse> findById(@PathVariable UUID busId, @PathVariable UUID id) {
        return ResponseEntity.ok(findSeatService.execute(busId, id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    @Operation(summary = "Update a seat on a bus")
    public ResponseEntity<SeatResponse> update(@PathVariable UUID busId, @PathVariable UUID id,
                                               @Valid @RequestBody UpdateSeatRequest request) {
        return ResponseEntity.ok(updateSeatService.execute(busId, id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    @Operation(summary = "Soft delete a seat")
    public ResponseEntity<Void> delete(@PathVariable UUID busId, @PathVariable UUID id) {
        deleteSeatService.execute(busId, id);
        return ResponseEntity.noContent().build();
    }
}
