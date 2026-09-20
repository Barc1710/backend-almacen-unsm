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
import pe.edu.unsm.almacen.dto.request.EncargadoAlmacenRequest;
import pe.edu.unsm.almacen.dto.response.EncargadoAlmacenResponse;
import pe.edu.unsm.almacen.service.IEncargadoAlmacenService;

@RestController
@RequestMapping("/encargados-almacen")
@RequiredArgsConstructor
@Tag(name = "Encargados de Almacén", description = "Gestión del personal responsable de los almacenes")
public class EncargadoAlmacenController {

    private final IEncargadoAlmacenService encargadoAlmacenService;

    @GetMapping
    @Operation(summary = "Listar encargados de almacén paginados", description = "Retorna un listado paginado con filtro de búsqueda opcional por nombre")
    public ResponseEntity<ApiResponse<PageResponse<EncargadoAlmacenResponse>>> listar(
            @RequestParam(name = "filtro", required = false) String filtro,
            Pageable pageable) {
        PageResponse<EncargadoAlmacenResponse> response = encargadoAlmacenService.listarPaginado(filtro, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Encargados de almacén recuperados exitosamente", response));
    }

    @GetMapping("/activos")
    @Operation(summary = "Listar encargados de almacén activos", description = "Retorna todos los encargados de almacén activos para combos y selección")
    public ResponseEntity<ApiResponse<List<EncargadoAlmacenResponse>>> listarActivos() {
        List<EncargadoAlmacenResponse> response = encargadoAlmacenService.listarActivos();
        return ResponseEntity.ok(new ApiResponse<>(true, "Encargados de almacén activos recuperados exitosamente", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener encargado de almacén por ID", description = "Retorna el detalle de un encargado de almacén específico")
    public ResponseEntity<ApiResponse<EncargadoAlmacenResponse>> obtenerPorId(@PathVariable("id") Integer id) {
        EncargadoAlmacenResponse response = encargadoAlmacenService.obtenerPorId(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Encargado de almacén encontrado exitosamente", response));
    }

    @PostMapping
    @Operation(summary = "Crear nuevo encargado de almacén", description = "Registra un nuevo encargado de almacén en el sistema")
    public ResponseEntity<ApiResponse<EncargadoAlmacenResponse>> crear(@Valid @RequestBody EncargadoAlmacenRequest request) {
        EncargadoAlmacenResponse response = encargadoAlmacenService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Encargado de almacén creado exitosamente", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar encargado de almacén", description = "Actualiza los datos de un encargado de almacén existente")
    public ResponseEntity<ApiResponse<EncargadoAlmacenResponse>> actualizar(
            @PathVariable("id") Integer id,
            @Valid @RequestBody EncargadoAlmacenRequest request) {
        EncargadoAlmacenResponse response = encargadoAlmacenService.actualizar(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Encargado de almacén actualizado exitosamente", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Borrado lógico de encargado de almacén", description = "Desactiva un encargado de almacén cambiando su estado a '0'")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable("id") Integer id) {
        encargadoAlmacenService.cambiarEstado(id, "0");
        return ResponseEntity.ok(new ApiResponse<>(true, "Encargado de almacén desactivado exitosamente", null));
    }
}
