package pe.edu.unsm.almacen.dto.request;

import java.util.List;

public record IngresoRequest(
        Integer idProveedor,
        String descripcion,
        List<DetalleIngresoRequest> detalles) {
}
