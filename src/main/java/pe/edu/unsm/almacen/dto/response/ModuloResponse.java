package pe.edu.unsm.almacen.dto.response;

public record ModuloResponse(
        Integer id,
        String codigo,
        String nombre,
        String url,
        String icono,
        Integer orden
) {
}
