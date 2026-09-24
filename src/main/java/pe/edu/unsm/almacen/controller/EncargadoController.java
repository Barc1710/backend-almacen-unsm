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
import pe.edu.unsm.almacen.dto.request.EncargadoRequest;
import pe.edu.unsm.almacen.dto.response.EncargadoResponse;
import pe.edu.unsm.almacen.service.IEncargadoService;

@RestController
@RequestMapping("/encargados")
@RequiredArgsConstructor
@Tag(name = "Encargados")
public class EncargadoController {

    private final IEncargadoService encargadoService;

    @GetMapping
    @PreAuthorize("@moduloAccess.canRead(authentication, 'ENCARGADOS', 'EGRESOS')")
    @Operation(summary = "Listar encargados paginados")
    public ResponseEntity<ApiResponse<PageResponse<EncargadoResponse>>> listar(
            @RequestParam(name = "filtro", required = false) String filtro,
            Pageable pageable) {
        PageResponse<EncargadoResponse> response = encargadoService.listarPaginado(filtro, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Encargados listados", response));
    }

    @GetMapping("/activos")
    @PreAuthorize("@moduloAccess.canRead(authentication, 'ENCARGADOS', 'EGRESOS')")
    @Operation(summary = "Listar encargados activos")
    public ResponseEntity<ApiResponse<List<EncargadoResponse>>> listarActivos() {
        List<EncargadoResponse> response = encargadoService.listarActivos();
        return ResponseEntity.ok(new ApiResponse<>(true, "Encargados activos listados", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@moduloAccess.canRead(authentication, 'ENCARGADOS', 'EGRESOS')")
    @Operation(summary = "Obtener encargado por ID")
    public ResponseEntity<ApiResponse<EncargadoResponse>> obtenerPorId(@PathVariable("id") Integer id) {
        EncargadoResponse response = encargadoService.obtenerPorId(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Encargado obtenido", response));
    }

    @PostMapping
    @PreAuthorize("@moduloAccess.hasAccess(authentication, 'ENCARGADOS')")
    @Operation(summary = "Crear encargado")
    public ResponseEntity<ApiResponse<EncargadoResponse>> crear(@Valid @RequestBody EncargadoRequest request) {
        EncargadoResponse response = encargadoService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Encargado creado", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@moduloAccess.hasAccess(authentication, 'ENCARGADOS')")
    @Operation(summary = "Actualizar encargado")
    public ResponseEntity<ApiResponse<EncargadoResponse>> actualizar(
            @PathVariable("id") Integer id,
            @Valid @RequestBody EncargadoRequest request) {
        EncargadoResponse response = encargadoService.actualizar(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Encargado actualizado", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR') and @moduloAccess.hasAccess(authentication, 'ENCARGADOS')")
    @Operation(summary = "Desactivar encargado")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable("id") Integer id) {
        encargadoService.cambiarEstado(id, "0");
        return ResponseEntity.ok(new ApiResponse<>(true, "Encargado desactivado", null));
    }
}
