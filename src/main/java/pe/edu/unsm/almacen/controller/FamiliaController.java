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
import pe.edu.unsm.almacen.dto.request.FamiliaRequest;
import pe.edu.unsm.almacen.dto.response.FamiliaResponse;
import pe.edu.unsm.almacen.service.IFamiliaService;

@RestController
@RequestMapping("/familias")
@RequiredArgsConstructor
@Tag(name = "Familias")
public class FamiliaController {

    private final IFamiliaService familiaService;

    @GetMapping
    @PreAuthorize("@moduloAccess.canRead(authentication, 'FAMILIAS', 'ARTICULOS')")
    @Operation(summary = "Listar familias paginadas")
    public ResponseEntity<ApiResponse<PageResponse<FamiliaResponse>>> listar(
            @RequestParam(name = "filtro", required = false) String filtro,
            Pageable pageable) {
        PageResponse<FamiliaResponse> response = familiaService.listarPaginado(filtro, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Familias listadas", response));
    }

    @GetMapping("/activos")
    @PreAuthorize("@moduloAccess.canRead(authentication, 'FAMILIAS', 'ARTICULOS')")
    @Operation(summary = "Listar familias activas")
    public ResponseEntity<ApiResponse<List<FamiliaResponse>>> listarActivos() {
        List<FamiliaResponse> response = familiaService.listarActivos();
        return ResponseEntity.ok(new ApiResponse<>(true, "Familias activas listadas", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@moduloAccess.canRead(authentication, 'FAMILIAS', 'ARTICULOS')")
    @Operation(summary = "Obtener familia por ID")
    public ResponseEntity<ApiResponse<FamiliaResponse>> obtenerPorId(@PathVariable("id") Integer id) {
        FamiliaResponse response = familiaService.obtenerPorId(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Familia obtenida", response));
    }

    @PostMapping
    @PreAuthorize("@moduloAccess.hasAccess(authentication, 'FAMILIAS')")
    @Operation(summary = "Crear familia")
    public ResponseEntity<ApiResponse<FamiliaResponse>> crear(@Valid @RequestBody FamiliaRequest request) {
        FamiliaResponse response = familiaService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Familia creada", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@moduloAccess.hasAccess(authentication, 'FAMILIAS')")
    @Operation(summary = "Actualizar familia")
    public ResponseEntity<ApiResponse<FamiliaResponse>> actualizar(
            @PathVariable("id") Integer id,
            @Valid @RequestBody FamiliaRequest request) {
        FamiliaResponse response = familiaService.actualizar(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Familia actualizada", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR') and @moduloAccess.hasAccess(authentication, 'FAMILIAS')")
    @Operation(summary = "Desactivar familia")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable("id") Integer id) {
        familiaService.cambiarEstado(id, "0");
        return ResponseEntity.ok(new ApiResponse<>(true, "Familia desactivada", null));
    }
}
