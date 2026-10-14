package pe.edu.unsm.almacen.dto.response.dashboard;

import java.time.LocalDateTime;

public record MovimientoRecienteResponse(
        String tipo, // "INGRESO" o "EGRESO"
        Integer id,
        String codigo,
        LocalDateTime fecha,
        String entidad, // Proveedor o Cliente/Área
        String estado
) {}
