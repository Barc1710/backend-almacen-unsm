package pe.edu.unsm.almacen.service;

import java.time.LocalDate;
import org.springframework.data.domain.Pageable;
import pe.edu.unsm.almacen.dto.common.PageResponse;
import pe.edu.unsm.almacen.dto.response.KardexMovimientoResponse;

public interface IKardexService {

    PageResponse<KardexMovimientoResponse> listarPorArticulo(
            Integer idArticulo,
            LocalDate desde,
            LocalDate hasta,
            Pageable pageable
    );

    PageResponse<KardexMovimientoResponse> listarGeneral(
            LocalDate desde,
            LocalDate hasta,
            Pageable pageable
    );

    default PageResponse<KardexMovimientoResponse> listarPorArticulo(Integer idArticulo, Pageable pageable) {
        return listarPorArticulo(idArticulo, null, null, pageable);
    }
}
