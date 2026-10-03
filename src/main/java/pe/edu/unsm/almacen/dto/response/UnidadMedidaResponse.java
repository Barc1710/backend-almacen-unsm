package pe.edu.unsm.almacen.dto.response;

public record UnidadMedidaResponse(
        Integer id,
        String codigoSunat,
        String nombre,
        String simbolo,
        Boolean permiteDecimales,
        String estado
) {
}
