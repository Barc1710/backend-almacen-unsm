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
import pe.edu.unsm.almacen.dto.request.UbicacionRequest;
import pe.edu.unsm.almacen.dto.response.UbicacionResponse;
import pe.edu.unsm.almacen.service.IUbicacionService;

@RestController
@RequestMapping("/ubicaciones")
@RequiredArgsConstructor
@Tag(name = "Ubicaciones")
public class UbicacionController {

    private final IUbicacionService ubicacionService;

    @GetMapping
    @PreAuthorize("@moduloAccess.canRead(authentication, 'UBICACIONES', 'ARTICULOS')")
    @Operation(summary = "Listar ubicaciones paginadas")
    public ResponseEntity<ApiResponse<PageResponse<UbicacionResponse>>> listar(
            @RequestParam(name = "filtro", required = false) String filtro,
            Pageable pageable) {
        PageResponse<UbicacionResponse> response = ubicacionService.listarPaginado(filtro, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Ubicaciones listadas", response));
    }

    @GetMapping("/activos")
    @PreAuthorize("@moduloAccess.canRead(authentication, 'UBICACIONES', 'ARTICULOS')")
    @Operation(summary = "Listar ubicaciones activas")
    public ResponseEntity<ApiResponse<List<UbicacionResponse>>> listarActivos() {
        List<UbicacionResponse> response = ubicacionService.listarActivos();
        return ResponseEntity.ok(new ApiResponse<>(true, "Ubicaciones activas listadas", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@moduloAccess.canRead(authentication, 'UBICACIONES', 'ARTICULOS')")
    @Operation(summary = "Obtener ubicación por ID")
    public ResponseEntity<ApiResponse<UbicacionResponse>> obtenerPorId(@PathVariable("id") Integer id) {
        UbicacionResponse response = ubicacionService.obtenerPorId(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Ubicación obtenida", response));
    }

    @PostMapping
    @PreAuthorize("@moduloAccess.hasAccess(authentication, 'UBICACIONES')")
    @Operation(summary = "Crear ubicación")
    public ResponseEntity<ApiResponse<UbicacionResponse>> crear(@Valid @RequestBody UbicacionRequest request) {
        UbicacionResponse response = ubicacionService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Ubicación creada", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@moduloAccess.hasAccess(authentication, 'UBICACIONES')")
    @Operation(summary = "Actualizar ubicación")
    public ResponseEntity<ApiResponse<UbicacionResponse>> actualizar(
            @PathVariable("id") Integer id,
            @Valid @RequestBody UbicacionRequest request) {
        UbicacionResponse response = ubicacionService.actualizar(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Ubicación actualizada", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR') and @moduloAccess.hasAccess(authentication, 'UBICACIONES')")
    @Operation(summary = "Desactivar ubicación")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable("id") Integer id) {
        ubicacionService.cambiarEstado(id, "0");
        return ResponseEntity.ok(new ApiResponse<>(true, "Ubicación desactivada", null));
    }
}
