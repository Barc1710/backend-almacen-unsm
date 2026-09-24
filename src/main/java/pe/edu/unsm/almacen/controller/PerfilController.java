package pe.edu.unsm.almacen.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.unsm.almacen.dto.common.ApiResponse;
import pe.edu.unsm.almacen.dto.request.AsignarPermisosRequest;
import pe.edu.unsm.almacen.dto.request.PerfilRequest;
import pe.edu.unsm.almacen.dto.response.PerfilPermisosResponse;
import pe.edu.unsm.almacen.dto.response.PerfilResponse;
import pe.edu.unsm.almacen.service.IPerfilService;

@RestController
@RequestMapping("/perfiles")
@RequiredArgsConstructor
@Tag(name = "Perfiles")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class PerfilController {

    private final IPerfilService perfilService;

    @GetMapping
    @Operation(summary = "Listar perfiles activos")
    public ResponseEntity<ApiResponse<List<PerfilResponse>>> listarActivos() {
        return ResponseEntity.ok(ApiResponse.ok("Perfiles listados", perfilService.listarActivos()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener perfil por ID")
    public ResponseEntity<ApiResponse<PerfilResponse>> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.ok("Perfil obtenido", perfilService.obtenerPorId(id)));
    }

    @PostMapping
    @Operation(summary = "Crear perfil")
    public ResponseEntity<ApiResponse<PerfilResponse>> crear(@Valid @RequestBody PerfilRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Perfil creado", perfilService.crear(request)));
    }

    @GetMapping("/{id}/modulos")
    @Operation(summary = "Obtener módulos del perfil")
    public ResponseEntity<ApiResponse<PerfilPermisosResponse>> obtenerModulosPorPerfil(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.ok("Módulos listados", perfilService.obtenerModulosPorPerfil(id)));
    }

    @PutMapping("/{id}/permisos")
    @Operation(summary = "Actualizar permisos del perfil")
    public ResponseEntity<ApiResponse<Void>> actualizarPermisos(
            @PathVariable Integer id,
            @Valid @RequestBody AsignarPermisosRequest request
    ) {
        perfilService.actualizarPermisos(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Permisos actualizados", null));
    }
}
