package pe.edu.unsm.almacen.service;

import java.util.List;
import org.springframework.data.domain.Pageable;
import pe.edu.unsm.almacen.dto.common.PageResponse;
import pe.edu.unsm.almacen.dto.request.UnidadMedidaRequest;
import pe.edu.unsm.almacen.dto.response.UnidadMedidaResponse;

public interface IUnidadMedidaService {

    PageResponse<UnidadMedidaResponse> listarPaginado(String filtro, Pageable pageable);

    List<UnidadMedidaResponse> listarActivos();

    UnidadMedidaResponse obtenerPorId(Integer id);

    UnidadMedidaResponse crear(UnidadMedidaRequest request);

    UnidadMedidaResponse actualizar(Integer id, UnidadMedidaRequest request);

    void cambiarEstado(Integer id, String nuevoEstado);
}
