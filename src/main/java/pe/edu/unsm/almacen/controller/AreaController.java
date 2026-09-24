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
import pe.edu.unsm.almacen.dto.request.AreaRequest;
import pe.edu.unsm.almacen.dto.response.AreaResponse;
import pe.edu.unsm.almacen.service.IAreaService;

@RestController
@RequestMapping("/areas")
@RequiredArgsConstructor
@Tag(name = "Áreas")
public class AreaController {

    private final IAreaService areaService;

    @GetMapping
    @PreAuthorize("@moduloAccess.canRead(authentication, 'AREAS', 'EGRESOS')")
    @Operation(summary = "Listar áreas paginadas")
    public ResponseEntity<ApiResponse<PageResponse<AreaResponse>>> listar(
            @RequestParam(name = "filtro", required = false) String filtro,
            Pageable pageable) {
        PageResponse<AreaResponse> response = areaService.listarPaginado(filtro, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Áreas listadas", response));
    }

    @GetMapping("/activos")
    @PreAuthorize("@moduloAccess.canRead(authentication, 'AREAS', 'EGRESOS')")
    @Operation(summary = "Listar áreas activas")
    public ResponseEntity<ApiResponse<List<AreaResponse>>> listarActivos() {
        List<AreaResponse> response = areaService.listarActivos();
        return ResponseEntity.ok(new ApiResponse<>(true, "Áreas activas listadas", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@moduloAccess.canRead(authentication, 'AREAS', 'EGRESOS')")
    @Operation(summary = "Obtener área por ID")
    public ResponseEntity<ApiResponse<AreaResponse>> obtenerPorId(@PathVariable("id") Integer id) {
        AreaResponse response = areaService.obtenerPorId(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Área obtenida", response));
    }

    @PostMapping
    @PreAuthorize("@moduloAccess.hasAccess(authentication, 'AREAS')")
    @Operation(summary = "Crear área")
    public ResponseEntity<ApiResponse<AreaResponse>> crear(@Valid @RequestBody AreaRequest request) {
        AreaResponse response = areaService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Área creada", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@moduloAccess.hasAccess(authentication, 'AREAS')")
    @Operation(summary = "Actualizar área")
    public ResponseEntity<ApiResponse<AreaResponse>> actualizar(
            @PathVariable("id") Integer id,
            @Valid @RequestBody AreaRequest request) {
        AreaResponse response = areaService.actualizar(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Área actualizada", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR') and @moduloAccess.hasAccess(authentication, 'AREAS')")
    @Operation(summary = "Desactivar área")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable("id") Integer id) {
        areaService.cambiarEstado(id, "0");
        return ResponseEntity.ok(new ApiResponse<>(true, "Área desactivada", null));
    }
}
