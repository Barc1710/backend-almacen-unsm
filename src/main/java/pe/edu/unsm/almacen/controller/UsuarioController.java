package pe.edu.unsm.almacen.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import pe.edu.unsm.almacen.dto.request.UsuarioCreateRequest;
import pe.edu.unsm.almacen.dto.request.UsuarioResetClaveRequest;
import pe.edu.unsm.almacen.dto.request.UsuarioUpdateRequest;
import pe.edu.unsm.almacen.dto.response.UsuarioResponse;
import pe.edu.unsm.almacen.service.IUsuarioService;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class UsuarioController {

    private final IUsuarioService usuarioService;

    @GetMapping
    @Operation(summary = "Listar usuarios paginado")
    public ResponseEntity<ApiResponse<PageResponse<UsuarioResponse>>> listar(
            @RequestParam(required = false) String filtro,
            @RequestParam(required = false) Integer idPerfil,
            @RequestParam(required = false) String estado,
            @PageableDefault(size = 10, sort = "nombre", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        PageResponse<UsuarioResponse> response = usuarioService.listar(filtro, idPerfil, estado, pageable);
        return ResponseEntity.ok(ApiResponse.ok("Usuarios listados", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por ID")
    public ResponseEntity<ApiResponse<UsuarioResponse>> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.ok("Usuario obtenido", usuarioService.obtenerPorId(id)));
    }

    @PostMapping
    @Operation(summary = "Crear usuario")
    public ResponseEntity<ApiResponse<UsuarioResponse>> crear(@Valid @RequestBody UsuarioCreateRequest request) {
        UsuarioResponse response = usuarioService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Usuario creado", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario")
    public ResponseEntity<ApiResponse<UsuarioResponse>> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody UsuarioUpdateRequest request
    ) {
        UsuarioResponse response = usuarioService.actualizar(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Usuario actualizado", response));
    }

    @PatchMapping("/{id}/reset-clave")
    @Operation(summary = "Restablecer contraseña")
    public ResponseEntity<ApiResponse<Void>> resetearClave(
            @PathVariable Integer id,
            @Valid @RequestBody UsuarioResetClaveRequest request
    ) {
        usuarioService.resetearClave(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Contraseña restablecida", null));
    }
}
