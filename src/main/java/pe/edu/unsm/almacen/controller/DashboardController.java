package pe.edu.unsm.almacen.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.unsm.almacen.dto.common.ApiResponse;
import pe.edu.unsm.almacen.dto.response.dashboard.DashboardResumenResponse;
import pe.edu.unsm.almacen.service.IDashboardService;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Métricas y resumen ejecutivo para el panel principal")
public class DashboardController {

    private final IDashboardService dashboardService;

    @GetMapping("/resumen")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtener métricas, gráficos y resumen ejecutivo del almacén")
    public ResponseEntity<ApiResponse<DashboardResumenResponse>> obtenerResumen() {
        DashboardResumenResponse resumen = dashboardService.obtenerResumen();
        return ResponseEntity.ok(ApiResponse.ok("Resumen del dashboard obtenido correctamente", resumen));
    }
}
