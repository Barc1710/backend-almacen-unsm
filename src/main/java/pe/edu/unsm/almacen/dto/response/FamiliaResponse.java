package pe.edu.unsm.almacen.dto.response;

public record FamiliaResponse(
        Integer id,
        String nombre,
        String inicial,
        Integer correlativo,
        String estado
) {
}
