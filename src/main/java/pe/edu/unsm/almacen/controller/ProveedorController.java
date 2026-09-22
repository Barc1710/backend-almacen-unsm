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
import pe.edu.unsm.almacen.dto.request.ProveedorRequest;
import pe.edu.unsm.almacen.dto.response.ProveedorResponse;
import pe.edu.unsm.almacen.service.IProveedorService;

@RestController
@RequestMapping("/proveedores")
@RequiredArgsConstructor
@Tag(name = "Proveedores", description = "Gestión del catálogo de proveedores comerciales")
public class ProveedorController {

    private final IProveedorService proveedorService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMIN', 'OPERADOR')")
    @Operation(summary = "Listar proveedores paginados", description = "Retorna un listado paginado con filtro de búsqueda opcional por RUC, razón social o contacto")
    public ResponseEntity<ApiResponse<PageResponse<ProveedorResponse>>> listar(
            @RequestParam(name = "filtro", required = false) String filtro,
            Pageable pageable) {
        PageResponse<ProveedorResponse> response = proveedorService.listarPaginado(filtro, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Proveedores recuperados exitosamente", response));
    }

    @GetMapping("/activos")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMIN', 'OPERADOR')")
    @Operation(summary = "Listar proveedores activos", description = "Retorna todos los proveedores activos para combos y selección")
    public ResponseEntity<ApiResponse<List<ProveedorResponse>>> listarActivos() {
        List<ProveedorResponse> response = proveedorService.listarActivos();
        return ResponseEntity.ok(new ApiResponse<>(true, "Proveedores activos recuperados exitosamente", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMIN', 'OPERADOR')")
    @Operation(summary = "Obtener proveedor por ID", description = "Retorna el detalle de un proveedor específico")
    public ResponseEntity<ApiResponse<ProveedorResponse>> obtenerPorId(@PathVariable("id") Integer id) {
        ProveedorResponse response = proveedorService.obtenerPorId(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Proveedor encontrado exitosamente", response));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMIN', 'OPERADOR')")
    @Operation(summary = "Crear nuevo proveedor", description = "Registra un nuevo proveedor con validación estricta de RUC")
    public ResponseEntity<ApiResponse<ProveedorResponse>> crear(@Valid @RequestBody ProveedorRequest request) {
        ProveedorResponse response = proveedorService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Proveedor creado exitosamente", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMIN', 'OPERADOR')")
    @Operation(summary = "Actualizar proveedor", description = "Actualiza los datos de un proveedor existente")
    public ResponseEntity<ApiResponse<ProveedorResponse>> actualizar(
            @PathVariable("id") Integer id,
            @Valid @RequestBody ProveedorRequest request) {
        ProveedorResponse response = proveedorService.actualizar(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Proveedor actualizado exitosamente", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMIN')")
    @Operation(summary = "Borrado lógico de proveedor", description = "Desactiva un proveedor cambiando su estado a '0'")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable("id") Integer id) {
        proveedorService.cambiarEstado(id, "0");
        return ResponseEntity.ok(new ApiResponse<>(true, "Proveedor desactivado exitosamente", null));
    }
}
