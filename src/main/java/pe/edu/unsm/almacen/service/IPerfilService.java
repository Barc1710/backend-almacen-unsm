package pe.edu.unsm.almacen.service;

import java.util.List;
import pe.edu.unsm.almacen.dto.request.AsignarPermisosRequest;
import pe.edu.unsm.almacen.dto.request.PerfilRequest;
import pe.edu.unsm.almacen.dto.response.PerfilPermisosResponse;
import pe.edu.unsm.almacen.dto.response.PerfilResponse;

public interface IPerfilService {

    List<PerfilResponse> listarActivos();

    PerfilResponse obtenerPorId(Integer id);

    PerfilResponse crear(PerfilRequest request);

    PerfilPermisosResponse obtenerModulosPorPerfil(Integer idPerfil);

    void actualizarPermisos(Integer idPerfil, AsignarPermisosRequest request);
}
