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
import pe.edu.unsm.almacen.dto.request.UbicacionRequest;
import pe.edu.unsm.almacen.dto.response.UbicacionResponse;
import pe.edu.unsm.almacen.service.IUbicacionService;

@RestController
@RequestMapping("/ubicaciones")
@RequiredArgsConstructor
@Tag(name = "Ubicaciones", description = "Gestión del catálogo de ubicaciones físicas de almacenamiento")
public class UbicacionController {

    private final IUbicacionService ubicacionService;

    @GetMapping
    @Operation(summary = "Listar ubicaciones paginadas", description = "Retorna un listado paginado con filtro de búsqueda opcional por nombre o descripción")
    public ResponseEntity<ApiResponse<PageResponse<UbicacionResponse>>> listar(
            @RequestParam(name = "filtro", required = false) String filtro,
            Pageable pageable) {
        PageResponse<UbicacionResponse> response = ubicacionService.listarPaginado(filtro, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Ubicaciones recuperadas exitosamente", response));
    }

    @GetMapping("/activos")
    @Operation(summary = "Listar ubicaciones activas", description = "Retorna todas las ubicaciones activas para combos y selección")
    public ResponseEntity<ApiResponse<List<UbicacionResponse>>> listarActivos() {
        List<UbicacionResponse> response = ubicacionService.listarActivos();
        return ResponseEntity.ok(new ApiResponse<>(true, "Ubicaciones activas recuperadas exitosamente", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener ubicación por ID", description = "Retorna el detalle de una ubicación específica")
    public ResponseEntity<ApiResponse<UbicacionResponse>> obtenerPorId(@PathVariable("id") Integer id) {
        UbicacionResponse response = ubicacionService.obtenerPorId(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Ubicación encontrada exitosamente", response));
    }

    @PostMapping
    @Operation(summary = "Crear nueva ubicación", description = "Registra una nueva ubicación física en el almacén")
    public ResponseEntity<ApiResponse<UbicacionResponse>> crear(@Valid @RequestBody UbicacionRequest request) {
        UbicacionResponse response = ubicacionService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Ubicación creada exitosamente", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar ubicación", description = "Actualiza los datos de una ubicación existente")
    public ResponseEntity<ApiResponse<UbicacionResponse>> actualizar(
            @PathVariable("id") Integer id,
            @Valid @RequestBody UbicacionRequest request) {
        UbicacionResponse response = ubicacionService.actualizar(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Ubicación actualizada exitosamente", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Borrado lógico de ubicación", description = "Desactiva una ubicación cambiando su estado a '0'")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable("id") Integer id) {
        ubicacionService.cambiarEstado(id, "0");
        return ResponseEntity.ok(new ApiResponse<>(true, "Ubicación desactivada exitosamente", null));
    }
}
