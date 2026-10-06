package pe.edu.unsm.almacen.service;

import java.time.LocalDate;
import org.springframework.data.domain.Pageable;
import pe.edu.unsm.almacen.dto.common.PageResponse;
import pe.edu.unsm.almacen.dto.response.KardexMovimientoResponse;
import pe.edu.unsm.almacen.entity.TipoMovimiento;

public interface IKardexService {

    PageResponse<KardexMovimientoResponse> listarPorArticulo(
            Integer idArticulo,
            LocalDate desde,
            LocalDate hasta,
            TipoMovimiento tipoMovimiento,
            Pageable pageable
    );

    PageResponse<KardexMovimientoResponse> listarGeneral(
            LocalDate desde,
            LocalDate hasta,
            TipoMovimiento tipoMovimiento,
            Pageable pageable
    );

    default PageResponse<KardexMovimientoResponse> listarPorArticulo(
            Integer idArticulo,
            LocalDate desde,
            LocalDate hasta,
            Pageable pageable) {
        return listarPorArticulo(idArticulo, desde, hasta, null, pageable);
    }

    default PageResponse<KardexMovimientoResponse> listarGeneral(
            LocalDate desde,
            LocalDate hasta,
            Pageable pageable) {
        return listarGeneral(desde, hasta, null, pageable);
    }

    default PageResponse<KardexMovimientoResponse> listarPorArticulo(Integer idArticulo, Pageable pageable) {
        return listarPorArticulo(idArticulo, null, null, null, pageable);
    }
}
