package pe.edu.unsm.almacen.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.unsm.almacen.dto.common.ApiResponse;
import pe.edu.unsm.almacen.dto.common.PageResponse;
import pe.edu.unsm.almacen.dto.request.IngresoCreateRequest;
import pe.edu.unsm.almacen.dto.response.IngresoResponse;
import pe.edu.unsm.almacen.service.IIngresoService;

@RestController
@RequestMapping("/ingresos")
@RequiredArgsConstructor
@Tag(name = "Ingresos", description = "Gestión de ingresos de artículos a almacén y auditoría Kardex")
public class IngresoController {

    private final IIngresoService ingresoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMIN', 'OPERADOR')")
    @Operation(summary = "Listar ingresos paginados", description = "Retorna listado paginado de ingresos con filtros opcionales de proveedor y rango de fechas")
    public ResponseEntity<ApiResponse<PageResponse<IngresoResponse>>> listar(
            @RequestParam(name = "idProveedor", required = false) Integer idProveedor,
            @RequestParam(name = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(name = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            Pageable pageable) {
        PageResponse<IngresoResponse> response = ingresoService.listar(idProveedor, desde, hasta, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Ingresos recuperados exitosamente", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMIN', 'OPERADOR')")
    @Operation(summary = "Obtener ingreso por ID", description = "Retorna el detalle completo de un ingreso con todas sus líneas de artículos")
    public ResponseEntity<ApiResponse<IngresoResponse>> obtenerPorId(@PathVariable("id") Integer id) {
        IngresoResponse response = ingresoService.obtenerPorId(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Ingreso encontrado exitosamente", response));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMIN', 'OPERADOR')")
    @Operation(summary = "Registrar nuevo ingreso", description = "Registra una entrada de artículos a almacén, actualiza el stock con bloqueo pesimista y genera asientos de auditoría en Kardex")
    public ResponseEntity<ApiResponse<IngresoResponse>> registrar(@Valid @RequestBody IngresoCreateRequest request) {
        IngresoResponse response = ingresoService.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Ingreso registrado exitosamente", response));
    }
}
