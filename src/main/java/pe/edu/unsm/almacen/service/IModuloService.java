package pe.edu.unsm.almacen.service;

import java.util.List;
import pe.edu.unsm.almacen.dto.response.ModuloResponse;

public interface IModuloService {

    List<ModuloResponse> listarModulosActivos();
}
