package pe.edu.unsm.almacen.dto.response;

public record UsuarioResponse(
        Integer id,
        String usuario,
        String nombre,
        String apellido,
        String nombreCompleto,
        String dni,
        String correo,
        String telefono,
        Integer idPerfil,
        String nombrePerfil,
        String estado,
        Boolean debeCambiarClave
) {
}
