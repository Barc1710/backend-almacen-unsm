package pe.edu.unsm.almacen.service;

import java.util.List;
import org.springframework.data.domain.Pageable;
import pe.edu.unsm.almacen.dto.common.PageResponse;
import pe.edu.unsm.almacen.dto.request.ArticuloCreateRequest;
import pe.edu.unsm.almacen.dto.request.ArticuloUpdateRequest;
import pe.edu.unsm.almacen.dto.response.ArticuloResponse;
import pe.edu.unsm.almacen.dto.response.ArticuloResumenResponse;

public interface IArticuloService {

    PageResponse<ArticuloResponse> listar(
            String filtro,
            String codigo,
            String descripcion,
            Integer idFamilia,
            String estado,
            Pageable pageable
    );

    List<ArticuloResumenResponse> buscarPredictivo(String termino, Boolean soloConStock);

    ArticuloResponse obtenerPorId(Integer id);

    ArticuloResponse crear(ArticuloCreateRequest request);

    ArticuloResponse actualizar(Integer id, ArticuloUpdateRequest request);

    void cambiarEstado(Integer id, String nuevoEstado);

    ArticuloResponse toggleActivo(Integer id);
}
