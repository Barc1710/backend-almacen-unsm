package pe.edu.unsm.almacen.dto.response;

public record EncargadoResponse(
        Integer id,
        String siglaProfesion,
        String nombres,
        String apellidos,
        String nombreCompleto,
        String dni,
        String cargo,
        String estado
) {
}
