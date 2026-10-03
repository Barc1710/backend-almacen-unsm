package pe.edu.unsm.almacen.service;

import java.time.LocalDate;
import org.springframework.data.domain.Pageable;
import pe.edu.unsm.almacen.dto.common.PageResponse;
import pe.edu.unsm.almacen.dto.request.EgresoCreateRequest;
import pe.edu.unsm.almacen.dto.response.EgresoResponse;

import pe.edu.unsm.almacen.entity.TipoEgreso;

public interface IEgresoService {

    PageResponse<EgresoResponse> listar(String filtro, Integer idCliente, Integer idArea, TipoEgreso tipoEgreso, LocalDate desde, LocalDate hasta, String estado, Pageable pageable);

    EgresoResponse obtenerPorId(Integer id);

    EgresoResponse registrar(EgresoCreateRequest request);

    EgresoResponse anular(Integer id);
}
