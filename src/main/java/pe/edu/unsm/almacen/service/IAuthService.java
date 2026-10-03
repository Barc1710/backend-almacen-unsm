package pe.edu.unsm.almacen.service;

import java.util.List;
import pe.edu.unsm.almacen.dto.request.LoginRequest;
import pe.edu.unsm.almacen.dto.response.JwtResponse;
import pe.edu.unsm.almacen.dto.response.ModuloResponse;

public interface IAuthService {

    JwtResponse login(LoginRequest request);

    List<ModuloResponse> obtenerMisModulos(String username);
}
