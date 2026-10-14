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
import pe.edu.unsm.almacen.dto.request.CrearUsuarioEncargadoRequest;
import pe.edu.unsm.almacen.dto.request.EncargadoAlmacenRequest;
import pe.edu.unsm.almacen.dto.response.EncargadoAlmacenResponse;
import pe.edu.unsm.almacen.service.IEncargadoAlmacenService;

@RestController
@RequestMapping("/encargados-almacen")
@RequiredArgsConstructor
@Tag(name = "Encargados de Almacén")
public class EncargadoAlmacenController {

    private final IEncargadoAlmacenService encargadoAlmacenService;

    @GetMapping
    @PreAuthorize("@moduloAccess.hasAccess(authentication, 'ENCARGADOS_ALMACEN') or @moduloAccess.hasAccess(authentication, 'EGRESOS')")
    @Operation(summary = "Listar encargados de almacén paginados")
    public ResponseEntity<ApiResponse<PageResponse<EncargadoAlmacenResponse>>> listar(
            @RequestParam(name = "filtro", required = false) String filtro,
            @RequestParam(name = "estado", required = false) String estado,
            Pageable pageable) {
        PageResponse<EncargadoAlmacenResponse> response = encargadoAlmacenService.listarPaginado(filtro, estado, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Encargados de almacén listados", response));
    }

    @GetMapping("/activos")
    @PreAuthorize("@moduloAccess.hasAccess(authentication, 'ENCARGADOS_ALMACEN') or @moduloAccess.hasAccess(authentication, 'EGRESOS') or @moduloAccess.hasAccess(authentication, 'INGRESOS')")
    @Operation(summary = "Listar encargados de almacén activos")
    public ResponseEntity<ApiResponse<List<EncargadoAlmacenResponse>>> listarActivos() {
        List<EncargadoAlmacenResponse> response = encargadoAlmacenService.listarActivos();
        return ResponseEntity.ok(new ApiResponse<>(true, "Encargados de almacén activos listados", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@moduloAccess.hasAccess(authentication, 'ENCARGADOS_ALMACEN') or @moduloAccess.hasAccess(authentication, 'EGRESOS')")
    @Operation(summary = "Obtener encargado de almacén por ID")
    public ResponseEntity<ApiResponse<EncargadoAlmacenResponse>> obtenerPorId(@PathVariable("id") Integer id) {
        EncargadoAlmacenResponse response = encargadoAlmacenService.obtenerPorId(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Encargado de almacén obtenido", response));
    }

    @PostMapping
    @PreAuthorize("@moduloAccess.hasAccess(authentication, 'ENCARGADOS_ALMACEN') or @moduloAccess.hasAccess(authentication, 'EGRESOS')")
    @Operation(summary = "Crear encargado de almacén")
    public ResponseEntity<ApiResponse<EncargadoAlmacenResponse>> crear(@Valid @RequestBody EncargadoAlmacenRequest request) {
        EncargadoAlmacenResponse response = encargadoAlmacenService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Encargado de almacén creado", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@moduloAccess.hasAccess(authentication, 'ENCARGADOS_ALMACEN') or @moduloAccess.hasAccess(authentication, 'EGRESOS')")
    @Operation(summary = "Actualizar encargado de almacén")
    public ResponseEntity<ApiResponse<EncargadoAlmacenResponse>> actualizar(
            @PathVariable("id") Integer id,
            @Valid @RequestBody EncargadoAlmacenRequest request) {
        EncargadoAlmacenResponse response = encargadoAlmacenService.actualizar(id, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Encargado de almacén actualizado", response));
    }

    @PatchMapping("/{id}/titular")
    @PreAuthorize("@moduloAccess.hasAccess(authentication, 'ENCARGADOS_ALMACEN') or @moduloAccess.hasAccess(authentication, 'EGRESOS')")
    @Operation(summary = "Alternar condición de titular de turno de forma atómica")
    public ResponseEntity<ApiResponse<EncargadoAlmacenResponse>> alternarTitular(@PathVariable("id") Integer id) {
        EncargadoAlmacenResponse response = encargadoAlmacenService.alternarTitular(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Titularidad actualizada exitosamente", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@moduloAccess.hasAccess(authentication, 'ENCARGADOS_ALMACEN') or @moduloAccess.hasAccess(authentication, 'EGRESOS')")
    @Operation(summary = "Desactivar encargado de almacén")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable("id") Integer id) {
        encargadoAlmacenService.cambiarEstado(id, "0");
        return ResponseEntity.ok(new ApiResponse<>(true, "Encargado de almacén desactivado", null));
    }

    @PatchMapping("/{id}/activar")
    @PreAuthorize("@moduloAccess.hasAccess(authentication, 'ENCARGADOS_ALMACEN') or @moduloAccess.hasAccess(authentication, 'EGRESOS')")
    @Operation(summary = "Activar encargado de almacén")
    public ResponseEntity<ApiResponse<Void>> activar(@PathVariable("id") Integer id) {
        encargadoAlmacenService.cambiarEstado(id, "1");
        return ResponseEntity.ok(new ApiResponse<>(true, "Encargado de almacén activado exitosamente", null));
    }

    @PostMapping("/{id}/crear-usuario")
    @PreAuthorize("@moduloAccess.hasAccess(authentication, 'ENCARGADOS_ALMACEN') or @moduloAccess.hasAccess(authentication, 'SEGURIDAD_USUARIOS')")
    @Operation(summary = "Crear y vincular cuenta de usuario al personal de almacén")
    public ResponseEntity<ApiResponse<EncargadoAlmacenResponse>> crearUsuario(
            @PathVariable("id") Integer id,
            @Valid @RequestBody CrearUsuarioEncargadoRequest request) {
        EncargadoAlmacenResponse response = encargadoAlmacenService.crearUsuario(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Usuario creado y vinculado exitosamente", response));
    }
}
