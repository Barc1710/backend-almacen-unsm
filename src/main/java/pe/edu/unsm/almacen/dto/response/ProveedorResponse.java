package pe.edu.unsm.almacen.dto.response;

public record ProveedorResponse(
        Integer id,
        String ruc,
        String razonSocial,
        String telefono,
        String celular,
        String correo,
        String direccion,
        String contacto,
        String banco,
        String cuentaCorriente,
        String estado
) {
}
