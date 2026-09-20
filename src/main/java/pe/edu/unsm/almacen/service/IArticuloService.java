package pe.edu.unsm.almacen.service;

import org.springframework.data.domain.Pageable;
import pe.edu.unsm.almacen.dto.common.PageResponse;
import pe.edu.unsm.almacen.dto.request.ArticuloRequest;
import pe.edu.unsm.almacen.dto.response.ArticuloResponse;

public interface IArticuloService {

    PageResponse<ArticuloResponse> listar(Pageable pageable);

    ArticuloResponse obtenerPorId(Integer id);

    ArticuloResponse crear(ArticuloRequest request);

    ArticuloResponse actualizar(Integer id, ArticuloRequest request);
}
