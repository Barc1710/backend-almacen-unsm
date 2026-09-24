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
@Tag(name = "Proveedores")
public class ProveedorController {

    private final IProveedorService proveedorService;

    @GetMapping
    @PreAuthorize("@moduloAccess.canRead(authentication, 'PROVEEDORES', 'INGRESOS')")
    @Operation(summary = "Listar proveedores paginados")
    public ResponseEntity<ApiResponse<PageResponse<ProveedorResponse>>> listar(
            @RequestParam(name = "filtro", required = false) String filtro,
            Pageable pageable) {
        PageResponse<ProveedorResponse> response = proveedorService.listarPaginado(filtro, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Proveedores listados", response));
    }

    @GetMapping("/activos")
    @PreAuthorize("@moduloAccess.canRead(authentication, 'PROVEEDORES', 'INGRESOS')")
    @Operation(summary = "Listar proveedores activos")
    public ResponseEntity<ApiResponse<List<ProveedorResponse>>> listarActivos() {
        List<ProveedorResponse> response = proveedorService.listarActivos();
        return ResponseEntity.ok(new ApiResponse<>(true, "Proveedores activos listados", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@moduloAccess.canRead(authentication, 'PROVEEDORES', 'INGRESOS')")
    @Operation(summary = "Obtener proveedor por ID")
    public ResponseEntity<ApiResponse<ProveedorResponse>> obtenerPorId(@PathVariable("id") Integer id) {
        ProveedorResponse response = proveedorService.obtenerPorId(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Proveedor obtenido", response));
    }

    @PostMapping
    @PreAuthorize("@moduloAccess.hasAccess(authentication, 'PROVEEDORES')")
    @Operation(summary = "Crear proveedor")
    public ResponseEntity<ApiResponse<ProveedorResponse>> crear(@Valid @RequestBody ProveedorRequest request) {
        ProveedorResponse response = proveedorService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Proveedor creado", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@moduloAccess.hasAccess(authentication, 'PROVEEDORES')")
    @Operation(summary = "Actualizar proveedor")
    public ResponseEntity<ApiResponse<ProveedorResponse>> actualizar(
            @PathVariable("id") Integer id,
            @Valid @RequestBody ProveedorRequest request) {
        ProveedorResponse response = proveedorService.actualizar(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Proveedor actualizado", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR') and @moduloAccess.hasAccess(authentication, 'PROVEEDORES')")
    @Operation(summary = "Desactivar proveedor")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable("id") Integer id) {
        proveedorService.cambiarEstado(id, "0");
        return ResponseEntity.ok(new ApiResponse<>(true, "Proveedor desactivado", null));
    }
}
