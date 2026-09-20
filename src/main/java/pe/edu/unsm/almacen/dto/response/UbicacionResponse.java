package pe.edu.unsm.almacen.dto.response;

public record UbicacionResponse(
        Integer id,
        String nombre,
        String descripcion,
        String estado
) {
}
