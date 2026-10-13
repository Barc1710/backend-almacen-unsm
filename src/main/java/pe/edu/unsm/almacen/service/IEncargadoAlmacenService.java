package pe.edu.unsm.almacen.service;

import java.util.List;
import org.springframework.data.domain.Pageable;
import pe.edu.unsm.almacen.dto.common.PageResponse;
import pe.edu.unsm.almacen.dto.request.CrearUsuarioEncargadoRequest;
import pe.edu.unsm.almacen.dto.request.EncargadoAlmacenRequest;
import pe.edu.unsm.almacen.dto.response.EncargadoAlmacenResponse;

public interface IEncargadoAlmacenService {

    PageResponse<EncargadoAlmacenResponse> listarPaginado(String filtro, String estado, Pageable pageable);

    List<EncargadoAlmacenResponse> listarActivos();

    EncargadoAlmacenResponse obtenerPorId(Integer id);

    EncargadoAlmacenResponse crear(EncargadoAlmacenRequest request);

    EncargadoAlmacenResponse actualizar(Integer id, EncargadoAlmacenRequest request);

    void cambiarEstado(Integer id, String nuevoEstado);

    EncargadoAlmacenResponse alternarTitular(Integer id);

    EncargadoAlmacenResponse crearUsuario(Integer id, CrearUsuarioEncargadoRequest request);
}
