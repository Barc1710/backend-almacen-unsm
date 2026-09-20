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
import pe.edu.unsm.almacen.dto.request.MarcaRequest;
import pe.edu.unsm.almacen.dto.response.MarcaResponse;
import pe.edu.unsm.almacen.service.IMarcaService;

@RestController
@RequestMapping("/marcas")
@RequiredArgsConstructor
@Tag(name = "Marcas", description = "Gestión del catálogo de marcas de artículos")
public class MarcaController {

    private final IMarcaService marcaService;

    @GetMapping
    @Operation(summary = "Listar marcas paginadas", description = "Retorna un listado paginado con filtro de búsqueda opcional por nombre")
    public ResponseEntity<ApiResponse<PageResponse<MarcaResponse>>> listar(
            @RequestParam(name = "filtro", required = false) String filtro,
            Pageable pageable) {
        PageResponse<MarcaResponse> response = marcaService.listarPaginado(filtro, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Marcas recuperadas exitosamente", response));
    }

    @GetMapping("/activos")
    @Operation(summary = "Listar marcas activas", description = "Retorna todas las marcas activas para combos y selección")
    public ResponseEntity<ApiResponse<List<MarcaResponse>>> listarActivos() {
        List<MarcaResponse> response = marcaService.listarActivos();
        return ResponseEntity.ok(new ApiResponse<>(true, "Marcas activas recuperadas exitosamente", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener marca por ID", description = "Retorna el detalle de una marca específica")
    public ResponseEntity<ApiResponse<MarcaResponse>> obtenerPorId(@PathVariable("id") Integer id) {
        MarcaResponse response = marcaService.obtenerPorId(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Marca encontrada exitosamente", response));
    }

    @PostMapping
    @Operation(summary = "Crear nueva marca", description = "Registra una nueva marca en el sistema")
    public ResponseEntity<ApiResponse<MarcaResponse>> crear(@Valid @RequestBody MarcaRequest request) {
        MarcaResponse response = marcaService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Marca creada exitosamente", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar marca", description = "Actualiza los datos de una marca existente")
    public ResponseEntity<ApiResponse<MarcaResponse>> actualizar(
            @PathVariable("id") Integer id,
            @Valid @RequestBody MarcaRequest request) {
        MarcaResponse response = marcaService.actualizar(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Marca actualizada exitosamente", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Borrado lógico de marca", description = "Desactiva una marca cambiando su estado a '0'")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable("id") Integer id) {
        marcaService.cambiarEstado(id, "0");
        return ResponseEntity.ok(new ApiResponse<>(true, "Marca desactivada exitosamente", null));
    }
}
