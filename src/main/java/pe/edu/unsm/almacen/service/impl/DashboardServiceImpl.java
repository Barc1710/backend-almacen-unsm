package pe.edu.unsm.almacen.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.unsm.almacen.dto.response.dashboard.ArticuloStockCriticoResponse;
import pe.edu.unsm.almacen.dto.response.dashboard.DashboardKpisResponse;
import pe.edu.unsm.almacen.dto.response.dashboard.DashboardResumenResponse;
import pe.edu.unsm.almacen.dto.response.dashboard.MovimientoRecienteResponse;
import pe.edu.unsm.almacen.dto.response.dashboard.MovimientoTendenciaResponse;
import pe.edu.unsm.almacen.dto.response.dashboard.StockFamiliaResponse;
import pe.edu.unsm.almacen.dto.response.dashboard.TopArticuloConsumoResponse;
import pe.edu.unsm.almacen.entity.Articulo;
import pe.edu.unsm.almacen.entity.Egreso;
import pe.edu.unsm.almacen.entity.Ingreso;
import pe.edu.unsm.almacen.repository.ArticuloRepository;
import pe.edu.unsm.almacen.repository.ClienteRepository;
import pe.edu.unsm.almacen.repository.DetalleEgresoRepository;
import pe.edu.unsm.almacen.repository.EgresoRepository;
import pe.edu.unsm.almacen.repository.IngresoRepository;
import pe.edu.unsm.almacen.repository.ProveedorRepository;
import pe.edu.unsm.almacen.service.IDashboardService;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements IDashboardService {

    private final ArticuloRepository articuloRepository;
    private final IngresoRepository ingresoRepository;
    private final EgresoRepository egresoRepository;
    private final DetalleEgresoRepository detalleEgresoRepository;
    private final ProveedorRepository proveedorRepository;
    private final ClienteRepository clienteRepository;

    private static final String[] MESES_ABR = {
            "Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Set", "Oct", "Nov", "Dic"
    };

    @Override
    @Transactional(readOnly = true)
    public DashboardResumenResponse obtenerResumen() {
        LocalDateTime inicioMes = YearMonth.now().atDay(1).atStartOfDay();

        // 1. KPIs
        long totalArticulos = articuloRepository.countByEstado("1");
        long articulosActivos = articuloRepository.countByEstadoAndActivo("1", true);
        long articulosStockBajo = articuloRepository.contarStockBajo();
        long articulosSinStock = articuloRepository.contarSinStock();

        long ingresosMes = ingresoRepository.contarIngresosDesde(inicioMes);
        long egresosMes = egresoRepository.contarEgresosDesde(inicioMes);

        long totalProveedores = proveedorRepository.countByEstado("1");
        long totalClientes = clienteRepository.countByEstado("1");

        DashboardKpisResponse kpis = new DashboardKpisResponse(
                totalArticulos,
                articulosActivos,
                articulosStockBajo,
                articulosSinStock,
                ingresosMes,
                egresosMes,
                totalProveedores,
                totalClientes
        );

        // 2. Tendencia mensual últimos 6 meses
        List<MovimientoTendenciaResponse> tendencia = new ArrayList<>();
        LocalDate hoy = LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = YearMonth.from(hoy.minusMonths(i));
            LocalDateTime desde = ym.atDay(1).atStartOfDay();
            LocalDateTime hasta = ym.atEndOfMonth().atTime(23, 59, 59);

            long countIng = ingresoRepository.contarIngresosEntre(desde, hasta);
            long countEgr = egresoRepository.contarEgresosEntre(desde, hasta);
            String label = MESES_ABR[ym.getMonthValue() - 1] + " " + (ym.getYear() % 100);

            tendencia.add(new MovimientoTendenciaResponse(ym.toString(), label, countIng, countEgr));
        }

        // 3. Distribución por Familias
        List<Object[]> famRows = articuloRepository.obtenerStockPorFamilias();
        List<StockFamiliaResponse> familias = new ArrayList<>();
        if (famRows != null) {
            for (Object[] r : famRows) {
                Integer idFam = (Integer) r[0];
                String nombreFam = (String) r[1];
                long cantArt = ((Number) r[2]).longValue();
                BigDecimal saldo = (BigDecimal) r[3];
                familias.add(new StockFamiliaResponse(idFam, nombreFam, cantArt, saldo));
            }
        }

        // 4. Top 5 Artículos con Mayor Consumo (Físico)
        List<Object[]> topRows = detalleEgresoRepository.findTopArticulosConsumo(PageRequest.of(0, 5));
        List<TopArticuloConsumoResponse> topConsumo = new ArrayList<>();
        if (topRows != null && !topRows.isEmpty()) {
            for (Object[] r : topRows) {
                Integer idArt = r[0] != null ? ((Number) r[0]).intValue() : null;
                String desc = r[1] != null ? r[1].toString() : "Artículo";
                String um = r[2] != null ? r[2].toString() : null;
                BigDecimal cant = BigDecimal.ZERO;
                if (r[3] instanceof BigDecimal bd) {
                    cant = bd;
                } else if (r[3] instanceof Number num) {
                    cant = BigDecimal.valueOf(num.doubleValue());
                } else if (r[3] != null) {
                    try {
                        cant = new BigDecimal(r[3].toString());
                    } catch (Exception ignored) {
                        // ignore format error
                    }
                }
                topConsumo.add(new TopArticuloConsumoResponse(idArt, desc, um, cant));
            }
        } else {
            // Si aún no se registran egresos, mostrar los artículos con mayor existencia en inventario
            List<Articulo> fallbackArts = articuloRepository
                    .findAll(PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "saldo")))
                    .getContent();
            for (Articulo a : fallbackArts) {
                topConsumo.add(new TopArticuloConsumoResponse(
                        a.getId(),
                        a.getDescripcion(),
                        a.getUnidadMedida() != null ? a.getUnidadMedida().getSimbolo() : null,
                        a.getSaldo() != null ? a.getSaldo() : BigDecimal.ZERO
                ));
            }
        }

        // 5. Artículos en Alerta Crítica (Stock Bajo)
        List<Articulo> criticosList = articuloRepository.findArticulosStockCritico(PageRequest.of(0, 5));
        List<ArticuloStockCriticoResponse> articulosCriticos = new ArrayList<>();
        for (Articulo a : criticosList) {
            double pct = 0.0;
            if (a.getCantidadMinima() != null && a.getCantidadMinima().compareTo(BigDecimal.ZERO) > 0) {
                double saldoVal = a.getSaldo() != null ? a.getSaldo().doubleValue() : 0.0;
                pct = Math.round((saldoVal / a.getCantidadMinima().doubleValue()) * 1000.0) / 10.0;
            }
            articulosCriticos.add(new ArticuloStockCriticoResponse(
                    a.getId(),
                    a.getCodigo(),
                    a.getDescripcion(),
                    a.getFamilia() != null ? a.getFamilia().getNombre() : "Sin familia",
                    a.getSaldo(),
                    a.getCantidadMinima(),
                    pct,
                    a.getUnidadMedida() != null ? a.getUnidadMedida().getSimbolo() : null
            ));
        }

        // 6. Últimos movimientos (Ingresos y Egresos combinados, últimos 5)
        List<MovimientoRecienteResponse> ultimos = new ArrayList<>();

        // Ingresos recientes
        List<Ingreso> ultIngresos = ingresoRepository.findUltimosIngresos(PageRequest.of(0, 5));
        if (!ultIngresos.isEmpty()) {
            for (Ingreso ing : ultIngresos) {
                String entidad = ing.getProveedor() != null ? ing.getProveedor().getRazonSocial() : "Proveedor";
                ultimos.add(new MovimientoRecienteResponse(
                        "INGRESO",
                        ing.getId(),
                        ing.getNumeroCompleto(),
                        ing.getFecha(),
                        entidad,
                        ing.getEstado()
                ));
            }
        }

        // Egresos recientes
        List<Egreso> ultEgresos = egresoRepository.findUltimosEgresos(PageRequest.of(0, 5));
        if (!ultEgresos.isEmpty()) {
            for (Egreso egr : ultEgresos) {
                String entidad = egr.getCliente() != null
                        ? egr.getCliente().getNombre()
                        : (egr.getArea() != null ? egr.getArea().getNombre() : "Dependencia UNSM");
                ultimos.add(new MovimientoRecienteResponse(
                        "EGRESO",
                        egr.getId(),
                        egr.getNumeroCompleto(),
                        egr.getFecha(),
                        entidad,
                        egr.getEstado()
                ));
            }
        }

        // Ordenar combinados por fecha descendente y tomar los últimos 5
        ultimos.sort(Comparator.comparing(MovimientoRecienteResponse::fecha).reversed());
        if (ultimos.size() > 5) {
            ultimos = ultimos.subList(0, 5);
        }

        return new DashboardResumenResponse(
                kpis,
                tendencia,
                familias,
                topConsumo,
                articulosCriticos,
                ultimos
        );
    }
}
