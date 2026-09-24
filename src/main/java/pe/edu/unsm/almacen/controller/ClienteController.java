package pe.edu.unsm.almacen.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.unsm.almacen.dto.common.ApiResponse;
import pe.edu.unsm.almacen.dto.common.PageResponse;
import pe.edu.unsm.almacen.dto.request.ClienteRequest;
import pe.edu.unsm.almacen.dto.response.ClienteResponse;
import pe.edu.unsm.almacen.service.IClienteService;

@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
@Tag(name = "Clientes")
public class ClienteController {

    private final IClienteService clienteService;

    @GetMapping
    @PreAuthorize("@moduloAccess.canRead(authentication, 'CLIENTES', 'EGRESOS')")
    @Operation(summary = "Listar clientes paginados")
    public ResponseEntity<ApiResponse<PageResponse<ClienteResponse>>> listar(
            @RequestParam(name = "filtro", required = false) String filtro,
            Pageable pageable) {
        PageResponse<ClienteResponse> response = clienteService.listarPaginado(filtro, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Clientes listados", response));
    }

    @GetMapping("/activos")
    @PreAuthorize("@moduloAccess.canRead(authentication, 'CLIENTES', 'EGRESOS')")
    @Operation(summary = "Listar clientes activos")
    public ResponseEntity<ApiResponse<List<ClienteResponse>>> listarActivos() {
        List<ClienteResponse> response = clienteService.listarActivos();
        return ResponseEntity.ok(new ApiResponse<>(true, "Clientes activos listados", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@moduloAccess.canRead(authentication, 'CLIENTES', 'EGRESOS')")
    @Operation(summary = "Obtener cliente por ID")
    public ResponseEntity<ApiResponse<ClienteResponse>> obtenerPorId(@PathVariable("id") Integer id) {
        ClienteResponse response = clienteService.obtenerPorId(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Cliente obtenido", response));
    }

    @PostMapping
    @PreAuthorize("@moduloAccess.hasAccess(authentication, 'CLIENTES')")
    @Operation(summary = "Crear cliente")
    public ResponseEntity<ApiResponse<ClienteResponse>> crear(@Valid @RequestBody ClienteRequest request) {
        ClienteResponse response = clienteService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Cliente creado", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@moduloAccess.hasAccess(authentication, 'CLIENTES')")
    @Operation(summary = "Actualizar cliente")
    public ResponseEntity<ApiResponse<ClienteResponse>> actualizar(
            @PathVariable("id") Integer id,
            @Valid @RequestBody ClienteRequest request) {
        ClienteResponse response = clienteService.actualizar(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Cliente actualizado", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR') and @moduloAccess.hasAccess(authentication, 'CLIENTES')")
    @Operation(summary = "Desactivar cliente")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable("id") Integer id) {
        clienteService.cambiarEstado(id, "0");
        return ResponseEntity.ok(new ApiResponse<>(true, "Cliente desactivado", null));
    }
}
