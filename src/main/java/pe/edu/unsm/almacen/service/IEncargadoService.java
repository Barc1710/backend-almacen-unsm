package pe.edu.unsm.almacen.service;

import java.util.List;
import org.springframework.data.domain.Pageable;
import pe.edu.unsm.almacen.dto.common.PageResponse;
import pe.edu.unsm.almacen.dto.request.EncargadoRequest;
import pe.edu.unsm.almacen.dto.response.EncargadoResponse;

public interface IEncargadoService {

    PageResponse<EncargadoResponse> listarPaginado(String filtro, Pageable pageable);

    List<EncargadoResponse> listarActivos();

    EncargadoResponse obtenerPorId(Integer id);

    EncargadoResponse crear(EncargadoRequest request);

    EncargadoResponse actualizar(Integer id, EncargadoRequest request);

    void cambiarEstado(Integer id, String nuevoEstado);
}
