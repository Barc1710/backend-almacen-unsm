package pe.edu.unsm.almacen.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.unsm.almacen.dto.common.ApiResponse;
import pe.edu.unsm.almacen.dto.common.PageResponse;
import pe.edu.unsm.almacen.dto.response.KardexMovimientoResponse;
import pe.edu.unsm.almacen.service.IKardexService;

@RestController
@RequestMapping("/kardex")
@RequiredArgsConstructor
@Tag(name = "Kardex")
public class KardexController {

    private final IKardexService kardexService;

    @GetMapping({"/articulo/{idArticulo}", "/articulos/{idArticulo}"})
    @PreAuthorize("@moduloAccess.canRead(authentication, 'KARDEX', 'ARTICULOS')")
    @Operation(summary = "Consultar Kardex de un artículo")
    public ResponseEntity<ApiResponse<PageResponse<KardexMovimientoResponse>>> listarPorArticulo(
            @PathVariable("idArticulo") Integer idArticulo,
            @RequestParam(name = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(name = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            Pageable pageable) {
        PageResponse<KardexMovimientoResponse> response = kardexService.listarPorArticulo(idArticulo, desde, hasta, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Kardex del artículo", response));
    }

    @GetMapping("/general")
    @PreAuthorize("@moduloAccess.hasAccess(authentication, 'KARDEX')")
    @Operation(summary = "Consultar movimientos de Kardex")
    public ResponseEntity<ApiResponse<PageResponse<KardexMovimientoResponse>>> listarGeneral(
            @RequestParam(name = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(name = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            Pageable pageable) {
        PageResponse<KardexMovimientoResponse> response = kardexService.listarGeneral(desde, hasta, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Movimientos listados", response));
    }
}
