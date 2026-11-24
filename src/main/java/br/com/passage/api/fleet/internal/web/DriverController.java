package br.com.passage.api.fleet.internal.web;

import br.com.passage.api.fleet.internal.dto.driver.CreateDriverRequest;
import br.com.passage.api.fleet.internal.dto.driver.UpdateDriverRequest;
import br.com.passage.api.fleet.internal.dto.driver.DriverResponse;
import br.com.passage.api.fleet.internal.service.driver.CreateDriverService;
import br.com.passage.api.fleet.internal.service.driver.DeleteDriverService;
import br.com.passage.api.fleet.internal.service.driver.FindDriverService;
import br.com.passage.api.fleet.internal.service.driver.UpdateDriverService;
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
@RequestMapping("/api/v1/drivers")
@RequiredArgsConstructor
@Tag(name = "Drivers", description = "Manage company drivers.")
public class DriverController {

    private final CreateDriverService createDriverService;
    private final FindDriverService findDriverService;
    private final UpdateDriverService updateDriverService;
    private final DeleteDriverService deleteDriverService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    @Operation(summary = "Create a driver")
    public ResponseEntity<DriverResponse> create(@Valid @RequestBody CreateDriverRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(createDriverService.execute(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    @Operation(summary = "List accessible drivers")
    public ResponseEntity<List<DriverResponse>> findAll() {
        return ResponseEntity.ok(findDriverService.executeAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    @Operation(summary = "Find a driver")
    public ResponseEntity<DriverResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(findDriverService.execute(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    @Operation(summary = "Update a driver")
    public ResponseEntity<DriverResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateDriverRequest request) {
        return ResponseEntity.ok(updateDriverService.execute(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    @Operation(summary = "Soft delete a driver")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deleteDriverService.execute(id);
        return ResponseEntity.noContent().build();
    }
}
