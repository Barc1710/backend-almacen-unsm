package pe.edu.unsm.almacen.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
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
@Tag(name = "Kardex", description = "Auditoría de movimientos de almacén e historial de Kardex físico/valorizado")
public class KardexController {

    private final IKardexService kardexService;

    @GetMapping({"/articulo/{idArticulo}", "/articulos/{idArticulo}"})
    @Operation(summary = "Consultar Kardex de un artículo", description = "Retorna el historial cronológico paginado de movimientos de un artículo con filtros opcionales de fecha")
    public ResponseEntity<ApiResponse<PageResponse<KardexMovimientoResponse>>> listarPorArticulo(
            @PathVariable("idArticulo") Integer idArticulo,
            @RequestParam(name = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(name = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            Pageable pageable) {
        PageResponse<KardexMovimientoResponse> response = kardexService.listarPorArticulo(idArticulo, desde, hasta, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Movimientos de kardex recuperados exitosamente para el artículo", response));
    }

    @GetMapping("/general")
    @Operation(summary = "Consultar historial general de auditoría de Kardex", description = "Retorna el historial paginado de todos los movimientos de almacén con filtros opcionales de rango de fechas")
    public ResponseEntity<ApiResponse<PageResponse<KardexMovimientoResponse>>> listarGeneral(
            @RequestParam(name = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(name = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            Pageable pageable) {
        PageResponse<KardexMovimientoResponse> response = kardexService.listarGeneral(desde, hasta, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Historial general de movimientos de kardex recuperado exitosamente", response));
    }
}
