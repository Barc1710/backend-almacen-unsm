package pe.edu.unsm.almacen.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pe.edu.unsm.almacen.dto.request.LoginRequest;
import pe.edu.unsm.almacen.dto.response.JwtResponse;
import pe.edu.unsm.almacen.security.jwt.JwtProvider;
import pe.edu.unsm.almacen.security.service.UserDetailsImpl;
import pe.edu.unsm.almacen.service.IAuthService;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements IAuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;

    @Override
    public JwtResponse login(LoginRequest request) {
        log.info("Iniciando autenticación para el usuario: {}", request.usuario());

        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.usuario(), request.clave())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String jwt = jwtProvider.generateToken(userDetails);

        log.info("Usuario '{}' autenticado exitosamente con perfil '{}'", userDetails.getUsername(), userDetails.getPerfil());

        return new JwtResponse(
                jwt,
                userDetails.getUsername(),
                userDetails.getNombreCompleto(),
                userDetails.getPerfil(),
                userDetails.getDebeCambiarClave()
        );
    }
}
