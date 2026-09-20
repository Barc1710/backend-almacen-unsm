package pe.edu.unsm.almacen.service;

import java.util.List;
import org.springframework.data.domain.Pageable;
import pe.edu.unsm.almacen.dto.common.PageResponse;
import pe.edu.unsm.almacen.dto.request.FamiliaRequest;
import pe.edu.unsm.almacen.dto.response.FamiliaResponse;

public interface IFamiliaService {

    PageResponse<FamiliaResponse> listarPaginado(String filtro, Pageable pageable);

    List<FamiliaResponse> listarActivos();

    FamiliaResponse obtenerPorId(Integer id);

    FamiliaResponse crear(FamiliaRequest request);

    FamiliaResponse actualizar(Integer id, FamiliaRequest request);

    void cambiarEstado(Integer id, String nuevoEstado);
}
