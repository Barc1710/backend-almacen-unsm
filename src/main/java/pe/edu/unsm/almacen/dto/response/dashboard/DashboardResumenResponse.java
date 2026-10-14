package pe.edu.unsm.almacen.dto.response.dashboard;

import java.util.List;

public record DashboardResumenResponse(
        DashboardKpisResponse kpis,
        List<MovimientoTendenciaResponse> tendenciaMensual,
        List<StockFamiliaResponse> distribucionFamilias,
        List<TopArticuloConsumoResponse> topArticulosConsumo,
        List<ArticuloStockCriticoResponse> articulosCriticos,
        List<MovimientoRecienteResponse> ultimosMovimientos
) {}
