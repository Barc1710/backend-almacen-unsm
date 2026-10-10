package pe.edu.unsm.almacen.dto.response;

public record EncargadoAlmacenResponse(
        Integer id,
        String nombres,
        String apellidos,
        String nombreCompleto,
        String dni,
        String estado,
        Boolean esTitular
) {
}
