package pe.edu.unsm.almacen.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.unsm.almacen.dto.common.ApiResponse;
import pe.edu.unsm.almacen.dto.common.PageResponse;
import pe.edu.unsm.almacen.dto.request.UnidadMedidaRequest;
import pe.edu.unsm.almacen.dto.response.UnidadMedidaResponse;
import pe.edu.unsm.almacen.service.IUnidadMedidaService;

@RestController
@RequestMapping("/unidades-medida")
@RequiredArgsConstructor
@Tag(name = "Unidades de Medida")
public class UnidadMedidaController {

    private final IUnidadMedidaService unidadMedidaService;

    @GetMapping
    @PreAuthorize("@moduloAccess.canRead(authentication, 'UNIDADES_MEDIDA', 'ARTICULOS')")
    @Operation(summary = "Listar unidades de medida paginadas")
    public ResponseEntity<ApiResponse<PageResponse<UnidadMedidaResponse>>> listarPaginado(
            @RequestParam(name = "filtro", required = false) String filtro,
            Pageable pageable) {
        PageResponse<UnidadMedidaResponse> response = unidadMedidaService.listarPaginado(filtro, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Unidades de medida listadas", response));
    }

    @GetMapping("/activas")
    @PreAuthorize("@moduloAccess.canRead(authentication, 'UNIDADES_MEDIDA', 'ARTICULOS')")
    @Operation(summary = "Listar unidades de medida activas")
    public ResponseEntity<ApiResponse<List<UnidadMedidaResponse>>> listarActivas() {
        List<UnidadMedidaResponse> response = unidadMedidaService.listarActivos();
        return ResponseEntity.ok(new ApiResponse<>(true, "Unidades de medida activas listadas", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@moduloAccess.canRead(authentication, 'UNIDADES_MEDIDA', 'ARTICULOS')")
    @Operation(summary = "Obtener unidad de medida por ID")
    public ResponseEntity<ApiResponse<UnidadMedidaResponse>> obtenerPorId(@PathVariable("id") Integer id) {
        UnidadMedidaResponse response = unidadMedidaService.obtenerPorId(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Unidad de medida obtenida", response));
    }

    @PostMapping
    @PreAuthorize("@moduloAccess.hasAccess(authentication, 'UNIDADES_MEDIDA')")
    @Operation(summary = "Crear unidad de medida")
    public ResponseEntity<ApiResponse<UnidadMedidaResponse>> crear(@Valid @RequestBody UnidadMedidaRequest request) {
        UnidadMedidaResponse response = unidadMedidaService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Unidad de medida creada", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@moduloAccess.hasAccess(authentication, 'UNIDADES_MEDIDA')")
    @Operation(summary = "Actualizar unidad de medida")
    public ResponseEntity<ApiResponse<UnidadMedidaResponse>> actualizar(
            @PathVariable("id") Integer id,
            @Valid @RequestBody UnidadMedidaRequest request) {
        UnidadMedidaResponse response = unidadMedidaService.actualizar(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Unidad de medida actualizada", response));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMINISTRADOR') and @moduloAccess.hasAccess(authentication, 'UNIDADES_MEDIDA')")
    @Operation(summary = "Cambiar estado de unidad de medida")
    public ResponseEntity<ApiResponse<Void>> cambiarEstado(
            @PathVariable("id") Integer id,
            @RequestBody Map<String, String> body) {
        String nuevoEstado = body.getOrDefault("estado", "1");
        unidadMedidaService.cambiarEstado(id, nuevoEstado);
        return ResponseEntity.ok(new ApiResponse<>(true, "Estado de unidad cambiado", null));
    }
}
