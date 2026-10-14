package pe.edu.unsm.almacen.dto.response.dashboard;

public record DashboardKpisResponse(
        long totalArticulos,
        long articulosActivos,
        long articulosStockBajo,
        long articulosSinStock,
        long ingresosMes,
        long egresosMes,
        long totalProveedores,
        long totalClientes
) {}
