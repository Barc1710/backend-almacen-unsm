package pe.edu.unsm.almacen.service;

import java.time.LocalDate;
import org.springframework.data.domain.Pageable;
import pe.edu.unsm.almacen.dto.common.PageResponse;
import pe.edu.unsm.almacen.dto.request.EgresoCreateRequest;
import pe.edu.unsm.almacen.dto.response.EgresoResponse;

public interface IEgresoService {

    PageResponse<EgresoResponse> listar(Integer idCliente, Integer idArea, LocalDate desde, LocalDate hasta, String estado, Pageable pageable);

    EgresoResponse obtenerPorId(Integer id);

    EgresoResponse registrar(EgresoCreateRequest request);

    EgresoResponse anular(Integer id);
}
