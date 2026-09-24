package pe.edu.unsm.almacen.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.unsm.almacen.dto.common.ApiResponse;
import pe.edu.unsm.almacen.dto.request.LoginRequest;
import pe.edu.unsm.almacen.dto.response.JwtResponse;
import pe.edu.unsm.almacen.dto.response.ModuloResponse;
import pe.edu.unsm.almacen.service.IAuthService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación")
public class AuthController {

    private final IAuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión")
    public ResponseEntity<ApiResponse<JwtResponse>> login(@Valid @RequestBody LoginRequest request) {
        JwtResponse jwtResponse = authService.login(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Sesión iniciada", jwtResponse));
    }

    @GetMapping("/mis-modulos")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtener módulos del usuario en sesión")
    public ResponseEntity<ApiResponse<List<ModuloResponse>>> misModulos(Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "";
        List<ModuloResponse> modulos = authService.obtenerMisModulos(username);
        return ResponseEntity.ok(ApiResponse.ok("Módulos obtenidos", modulos));
    }
}
