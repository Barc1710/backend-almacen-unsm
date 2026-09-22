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
@Tag(name = "Artículos", description = "Gestión del catálogo maestro de artículos e inventario")
public class ArticuloController {

    private final IArticuloService articuloService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMIN', 'OPERADOR')")
    @Operation(summary = "Listar artículos paginados", description = "Retorna un listado administrativo paginado con filtros combinados (código, descripción, familia, estado)")
    public ResponseEntity<ApiResponse<PageResponse<ArticuloResponse>>> listar(
            @RequestParam(name = "filtro", required = false) String filtro,
            @RequestParam(name = "codigo", required = false) String codigo,
            @RequestParam(name = "descripcion", required = false) String descripcion,
            @RequestParam(name = "idFamilia", required = false) Integer idFamilia,
            @RequestParam(name = "estado", required = false) String estado,
            Pageable pageable) {
        PageResponse<ArticuloResponse> response = articuloService.listar(filtro, codigo, descripcion, idFamilia, estado, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Artículos recuperados exitosamente", response));
    }

    @GetMapping("/buscar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMIN', 'OPERADOR')")
    @Operation(summary = "Búsqueda predictiva de artículos", description = "Retorna lista liviana de artículos activos para autocompletado en despachos y consultas rápidas")
    public ResponseEntity<ApiResponse<List<ArticuloResumenResponse>>> buscarPredictivo(
            @RequestParam(name = "q", required = false) String q) {
        List<ArticuloResumenResponse> response = articuloService.buscarPredictivo(q);
        return ResponseEntity.ok(new ApiResponse<>(true, "Búsqueda predictiva realizada con éxito", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMIN', 'OPERADOR')")
    @Operation(summary = "Obtener artículo por ID", description = "Retorna el detalle completo de un artículo específico")
    public ResponseEntity<ApiResponse<ArticuloResponse>> obtenerPorId(@PathVariable("id") Integer id) {
        ArticuloResponse response = articuloService.obtenerPorId(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Artículo encontrado exitosamente", response));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMIN', 'OPERADOR')")
    @Operation(summary = "Crear nuevo artículo", description = "Registra un artículo nuevo en el sistema con saldo inicial en cero")
    public ResponseEntity<ApiResponse<ArticuloResponse>> crear(@Valid @RequestBody ArticuloCreateRequest request) {
        ArticuloResponse response = articuloService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Artículo creado exitosamente", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMIN', 'OPERADOR')")
    @Operation(summary = "Actualizar artículo", description = "Actualiza los datos maestros de un artículo existente sin modificar existencias")
    public ResponseEntity<ApiResponse<ArticuloResponse>> actualizar(
            @PathVariable("id") Integer id,
            @Valid @RequestBody ArticuloUpdateRequest request) {
        ArticuloResponse response = articuloService.actualizar(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Artículo actualizado exitosamente", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMIN')")
    @Operation(summary = "Borrado lógico de artículo", description = "Desactiva un artículo cambiando su estado a '0'")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable("id") Integer id) {
        articuloService.cambiarEstado(id, "0");
        return ResponseEntity.ok(new ApiResponse<>(true, "Artículo desactivado exitosamente", null));
    }

    @PatchMapping("/{id}/toggle-activo")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMIN')")
    @Operation(summary = "Alternar operatividad de artículo", description = "Habilita o deshabilita la operatividad de un artículo para movimientos")
    public ResponseEntity<ApiResponse<ArticuloResponse>> toggleActivo(@PathVariable("id") Integer id) {
        ArticuloResponse response = articuloService.toggleActivo(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Operatividad de artículo modificada exitosamente", response));
    }
}
