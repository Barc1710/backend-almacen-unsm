package pe.edu.unsm.almacen.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.unsm.almacen.dto.common.ApiResponse;
import pe.edu.unsm.almacen.dto.response.ModuloResponse;
import pe.edu.unsm.almacen.service.IModuloService;

@RestController
@RequestMapping("/modulos")
@RequiredArgsConstructor
@Tag(name = "Módulos")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class ModuloController {

    private final IModuloService moduloService;

    @GetMapping
    @Operation(summary = "Listar módulos disponibles")
    public ResponseEntity<ApiResponse<List<ModuloResponse>>> listarActivos() {
        return ResponseEntity.ok(ApiResponse.ok("Módulos listados", moduloService.listarModulosActivos()));
    }
}
