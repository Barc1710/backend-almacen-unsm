package pe.edu.unsm.almacen.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pe.edu.unsm.almacen.dto.common.ApiResponse;
import pe.edu.unsm.almacen.dto.common.PageResponse;
import pe.edu.unsm.almacen.dto.request.EgresoCreateRequest;
import pe.edu.unsm.almacen.dto.response.EgresoResponse;
import pe.edu.unsm.almacen.service.IEgresoService;

@RestController
@RequestMapping("/egresos")
@RequiredArgsConstructor
@Tag(name = "Egresos")
public class EgresoController {

    private final IEgresoService egresoService;

    @GetMapping
    @PreAuthorize("@moduloAccess.hasAccess(authentication, 'EGRESOS')")
    @Operation(summary = "Listar egresos paginados")
    public ResponseEntity<ApiResponse<PageResponse<EgresoResponse>>> listar(
            @RequestParam(name = "idCliente", required = false) Integer idCliente,
            @RequestParam(name = "idArea", required = false) Integer idArea,
            @RequestParam(name = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(name = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(name = "estado", required = false) String estado,
            Pageable pageable) {
        PageResponse<EgresoResponse> response = egresoService.listar(idCliente, idArea, desde, hasta, estado, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Egresos listados", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@moduloAccess.hasAccess(authentication, 'EGRESOS')")
    @Operation(summary = "Obtener egreso por ID")
    public ResponseEntity<ApiResponse<EgresoResponse>> obtenerPorId(@PathVariable("id") Integer id) {
        EgresoResponse response = egresoService.obtenerPorId(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Egreso obtenido", response));
    }

    @PostMapping
    @PreAuthorize("@moduloAccess.hasAccess(authentication, 'EGRESOS')")
    @Operation(summary = "Registrar egreso")
    public ResponseEntity<ApiResponse<EgresoResponse>> registrar(@Valid @RequestBody EgresoCreateRequest request) {
        EgresoResponse response = egresoService.registrar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location)
                .body(new ApiResponse<>(true, "Egreso registrado", response));
    }

    @PostMapping("/{id}/anular")
    @PreAuthorize("hasRole('ADMINISTRADOR') and @moduloAccess.hasAccess(authentication, 'EGRESOS')")
    @Operation(summary = "Anular egreso")
    public ResponseEntity<ApiResponse<EgresoResponse>> anular(@PathVariable("id") Integer id) {
        EgresoResponse response = egresoService.anular(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Egreso anulado", response));
    }
}
