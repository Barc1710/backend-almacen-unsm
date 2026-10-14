package pe.edu.unsm.almacen.dto.response.dashboard;

public record MovimientoTendenciaResponse(
        String mes,
        String label,
        long ingresos,
        long egresos
) {}
