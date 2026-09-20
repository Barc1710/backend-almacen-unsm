package pe.edu.unsm.almacen.service;

import pe.edu.unsm.almacen.dto.request.LoginRequest;
import pe.edu.unsm.almacen.dto.response.JwtResponse;

public interface IAuthService {

    JwtResponse login(LoginRequest request);
}
