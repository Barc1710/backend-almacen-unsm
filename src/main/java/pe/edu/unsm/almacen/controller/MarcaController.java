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
import pe.edu.unsm.almacen.dto.request.MarcaRequest;
import pe.edu.unsm.almacen.dto.response.MarcaResponse;
import pe.edu.unsm.almacen.service.IMarcaService;

@RestController
@RequestMapping("/marcas")
@RequiredArgsConstructor
@Tag(name = "Marcas")
public class MarcaController {

    private final IMarcaService marcaService;

    @GetMapping
    @PreAuthorize("@moduloAccess.canRead(authentication, 'MARCAS', 'ARTICULOS')")
    @Operation(summary = "Listar marcas paginadas")
    public ResponseEntity<ApiResponse<PageResponse<MarcaResponse>>> listar(
            @RequestParam(name = "filtro", required = false) String filtro,
            Pageable pageable) {
        PageResponse<MarcaResponse> response = marcaService.listarPaginado(filtro, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Marcas listadas", response));
    }

    @GetMapping("/activos")
    @PreAuthorize("@moduloAccess.canRead(authentication, 'MARCAS', 'ARTICULOS')")
    @Operation(summary = "Listar marcas activas")
    public ResponseEntity<ApiResponse<List<MarcaResponse>>> listarActivos() {
        List<MarcaResponse> response = marcaService.listarActivos();
        return ResponseEntity.ok(new ApiResponse<>(true, "Marcas activas listadas", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@moduloAccess.canRead(authentication, 'MARCAS', 'ARTICULOS')")
    @Operation(summary = "Obtener marca por ID")
    public ResponseEntity<ApiResponse<MarcaResponse>> obtenerPorId(@PathVariable("id") Integer id) {
        MarcaResponse response = marcaService.obtenerPorId(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Marca obtenida", response));
    }

    @PostMapping
    @PreAuthorize("@moduloAccess.hasAccess(authentication, 'MARCAS')")
    @Operation(summary = "Crear marca")
    public ResponseEntity<ApiResponse<MarcaResponse>> crear(@Valid @RequestBody MarcaRequest request) {
        MarcaResponse response = marcaService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Marca creada", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@moduloAccess.hasAccess(authentication, 'MARCAS')")
    @Operation(summary = "Actualizar marca")
    public ResponseEntity<ApiResponse<MarcaResponse>> actualizar(
            @PathVariable("id") Integer id,
            @Valid @RequestBody MarcaRequest request) {
        MarcaResponse response = marcaService.actualizar(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Marca actualizada", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR') and @moduloAccess.hasAccess(authentication, 'MARCAS')")
    @Operation(summary = "Desactivar marca")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable("id") Integer id) {
        marcaService.cambiarEstado(id, "0");
        return ResponseEntity.ok(new ApiResponse<>(true, "Marca desactivada", null));
    }
}
