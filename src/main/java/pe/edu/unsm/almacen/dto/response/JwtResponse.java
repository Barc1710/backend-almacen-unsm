package pe.edu.unsm.almacen.dto.response;

public record JwtResponse(
        String token,
        String usuario,
        String nombre,
        String perfil,
        Boolean debeCambiarClave
) {
}
