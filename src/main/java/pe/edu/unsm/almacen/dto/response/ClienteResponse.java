package pe.edu.unsm.almacen.dto.response;

public record ClienteResponse(
        Integer id,
        String nombre,
        String dni,
        String telefono,
        String celular,
        String correo,
        String direccion,
        String estado
) {
}
