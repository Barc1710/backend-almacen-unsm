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
import pe.edu.unsm.almacen.dto.request.ArticuloCreateRequest;
import pe.edu.unsm.almacen.dto.request.ArticuloUpdateRequest;
import pe.edu.unsm.almacen.dto.response.ArticuloResponse;
import pe.edu.unsm.almacen.dto.response.ArticuloResumenResponse;
import pe.edu.unsm.almacen.service.IArticuloService;

@RestController
@RequestMapping("/articulos")
@RequiredArgsConstructor
@Tag(name = "Artículos")
public class ArticuloController {

    private final IArticuloService articuloService;

    @GetMapping
    @PreAuthorize("@moduloAccess.canRead(authentication, 'ARTICULOS', 'INGRESOS', 'EGRESOS', 'KARDEX')")
    @Operation(summary = "Listar artículos paginados")
    public ResponseEntity<ApiResponse<PageResponse<ArticuloResponse>>> listar(
            @RequestParam(name = "filtro", required = false) String filtro,
            @RequestParam(name = "codigo", required = false) String codigo,
            @RequestParam(name = "descripcion", required = false) String descripcion,
            @RequestParam(name = "idFamilia", required = false) Integer idFamilia,
            @RequestParam(name = "estado", required = false) String estado,
            Pageable pageable) {
        PageResponse<ArticuloResponse> response = articuloService.listar(filtro, codigo, descripcion, idFamilia, estado, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Artículos listados", response));
    }

    @GetMapping("/buscar")
    @PreAuthorize("@moduloAccess.canRead(authentication, 'ARTICULOS', 'INGRESOS', 'EGRESOS', 'KARDEX')")
    @Operation(summary = "Buscar artículos")
    public ResponseEntity<ApiResponse<List<ArticuloResumenResponse>>> buscarPredictivo(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "soloConStock", required = false, defaultValue = "false") Boolean soloConStock) {
        List<ArticuloResumenResponse> response = articuloService.buscarPredictivo(q, soloConStock);
        return ResponseEntity.ok(new ApiResponse<>(true, "Artículos obtenidos", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@moduloAccess.canRead(authentication, 'ARTICULOS', 'INGRESOS', 'EGRESOS', 'KARDEX')")
    @Operation(summary = "Obtener artículo por ID")
    public ResponseEntity<ApiResponse<ArticuloResponse>> obtenerPorId(@PathVariable("id") Integer id) {
        ArticuloResponse response = articuloService.obtenerPorId(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Artículo obtenido", response));
    }

    @PostMapping
    @PreAuthorize("@moduloAccess.hasAccess(authentication, 'ARTICULOS')")
    @Operation(summary = "Crear artículo")
    public ResponseEntity<ApiResponse<ArticuloResponse>> crear(@Valid @RequestBody ArticuloCreateRequest request) {
        ArticuloResponse response = articuloService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Artículo creado", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@moduloAccess.hasAccess(authentication, 'ARTICULOS')")
    @Operation(summary = "Actualizar artículo")
    public ResponseEntity<ApiResponse<ArticuloResponse>> actualizar(
            @PathVariable("id") Integer id,
            @Valid @RequestBody ArticuloUpdateRequest request) {
        ArticuloResponse response = articuloService.actualizar(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Artículo actualizado", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR') and @moduloAccess.hasAccess(authentication, 'ARTICULOS')")
    @Operation(summary = "Desactivar artículo")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable("id") Integer id) {
        articuloService.cambiarEstado(id, "0");
        return ResponseEntity.ok(new ApiResponse<>(true, "Artículo desactivado", null));
    }

    @PatchMapping("/{id}/toggle-activo")
    @PreAuthorize("hasRole('ADMINISTRADOR') and @moduloAccess.hasAccess(authentication, 'ARTICULOS')")
    @Operation(summary = "Cambiar operatividad del artículo")
    public ResponseEntity<ApiResponse<ArticuloResponse>> toggleActivo(@PathVariable("id") Integer id) {
        ArticuloResponse response = articuloService.toggleActivo(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Operatividad cambiada", response));
    }
}
