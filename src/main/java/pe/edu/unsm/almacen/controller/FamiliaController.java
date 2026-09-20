package pe.edu.unsm.almacen.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@Tag(name = "Familias", description = "Gestión del catálogo de familias de artículos y correlativos")
public class FamiliaController {

    private final IFamiliaService familiaService;

    @GetMapping
    @Operation(summary = "Listar familias paginadas", description = "Retorna un listado paginado con filtro de búsqueda opcional por nombre o inicial")
    public ResponseEntity<ApiResponse<PageResponse<FamiliaResponse>>> listar(
            @RequestParam(name = "filtro", required = false) String filtro,
            Pageable pageable) {
        PageResponse<FamiliaResponse> response = familiaService.listarPaginado(filtro, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Familias recuperadas exitosamente", response));
    }

    @GetMapping("/activos")
    @Operation(summary = "Listar familias activas", description = "Retorna todas las familias activas para combos y selección")
    public ResponseEntity<ApiResponse<List<FamiliaResponse>>> listarActivos() {
        List<FamiliaResponse> response = familiaService.listarActivos();
        return ResponseEntity.ok(new ApiResponse<>(true, "Familias activas recuperadas exitosamente", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener familia por ID", description = "Retorna el detalle de una familia específica")
    public ResponseEntity<ApiResponse<FamiliaResponse>> obtenerPorId(@PathVariable("id") Integer id) {
        FamiliaResponse response = familiaService.obtenerPorId(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Familia encontrada exitosamente", response));
    }

    @PostMapping
    @Operation(summary = "Crear nueva familia", description = "Registra una nueva familia e inicializa su correlativo inicial en 1")
    public ResponseEntity<ApiResponse<FamiliaResponse>> crear(@Valid @RequestBody FamiliaRequest request) {
        FamiliaResponse response = familiaService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Familia creada exitosamente", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar familia", description = "Actualiza los datos de una familia existente")
    public ResponseEntity<ApiResponse<FamiliaResponse>> actualizar(
            @PathVariable("id") Integer id,
            @Valid @RequestBody FamiliaRequest request) {
        FamiliaResponse response = familiaService.actualizar(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Familia actualizada exitosamente", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Borrado lógico de familia", description = "Desactiva una familia cambiando su estado a '0'")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable("id") Integer id) {
        familiaService.cambiarEstado(id, "0");
        return ResponseEntity.ok(new ApiResponse<>(true, "Familia desactivada exitosamente", null));
    }
}
