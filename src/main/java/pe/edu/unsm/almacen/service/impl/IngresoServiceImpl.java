package pe.edu.unsm.almacen.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Set;
import org.springframework.security.access.AccessDeniedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.unsm.almacen.dto.common.PageResponse;
import pe.edu.unsm.almacen.dto.request.DetalleItemRequest;
import pe.edu.unsm.almacen.dto.request.IngresoCreateRequest;
import pe.edu.unsm.almacen.dto.response.DetalleIngresoResponse;
import pe.edu.unsm.almacen.dto.response.IngresoResponse;
import pe.edu.unsm.almacen.entity.Articulo;
import pe.edu.unsm.almacen.entity.DetalleIngreso;
import pe.edu.unsm.almacen.entity.Ingreso;
import pe.edu.unsm.almacen.entity.KardexMovimiento;
import pe.edu.unsm.almacen.entity.Proveedor;
import pe.edu.unsm.almacen.entity.TipoMovimiento;
import pe.edu.unsm.almacen.entity.Usuario;
import pe.edu.unsm.almacen.exception.BusinessException;
import pe.edu.unsm.almacen.exception.DuplicateResourceException;
import pe.edu.unsm.almacen.exception.ResourceNotFoundException;
import pe.edu.unsm.almacen.repository.ArticuloRepository;
import pe.edu.unsm.almacen.repository.DetalleIngresoRepository;
import pe.edu.unsm.almacen.repository.IngresoRepository;
import pe.edu.unsm.almacen.repository.KardexMovimientoRepository;
import pe.edu.unsm.almacen.repository.ProveedorRepository;
import pe.edu.unsm.almacen.repository.UsuarioRepository;
import pe.edu.unsm.almacen.security.service.UserDetailsImpl;
import pe.edu.unsm.almacen.service.IIngresoService;

@Service
@RequiredArgsConstructor
@Slf4j
public class IngresoServiceImpl implements IIngresoService {

    private final IngresoRepository ingresoRepository;
    private final DetalleIngresoRepository detalleIngresoRepository;
    private final ArticuloRepository articuloRepository;
    private final ProveedorRepository proveedorRepository;
    private final KardexMovimientoRepository kardexMovimientoRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<IngresoResponse> listar(Integer idProveedor, LocalDate desde, LocalDate hasta, Pageable pageable) {
        LocalDateTime desdeDateTime = desde != null ? desde.atStartOfDay() : null;
        LocalDateTime hastaDateTime = hasta != null ? hasta.atTime(23, 59, 59) : null;

        Page<Ingreso> page = ingresoRepository.listarPaginado(idProveedor, desdeDateTime, hastaDateTime, pageable);
        List<Integer> ids = page.getContent().stream().map(Ingreso::getId).toList();
        Map<Integer, BigDecimal> totales = ids.isEmpty() ? Map.of()
                : detalleIngresoRepository.sumarTotalesPorIngresoIds(ids).stream()
                        .collect(Collectors.toMap(row -> (Integer) row[0], row -> extraerMontoSeguro(row[1])));
        return PageResponse.of(page.map(ingreso -> construirIngresoResumenResponse(
                ingreso, totales.getOrDefault(ingreso.getId(), BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP))));
    }

    @Override
    @Transactional(readOnly = true)
    public IngresoResponse obtenerPorId(Integer id) {
        Ingreso ingreso = ingresoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ingreso no encontrado con ID: " + id));

        return construirIngresoResponse(ingreso);
    }

    @Override
    @Transactional
    public IngresoResponse registrar(IngresoCreateRequest request) {
        String ordenCompraLimpia = null;
        if (request.numeroOrdenCompra() != null && !request.numeroOrdenCompra().trim().isEmpty()) {
            ordenCompraLimpia = request.numeroOrdenCompra().trim().toUpperCase();
            if (ingresoRepository.existsByNumeroOrdenCompraAndEstado(ordenCompraLimpia, "1")) {
                throw new DuplicateResourceException("Orden de compra duplicada: " + ordenCompraLimpia);
            }
        }

        Set<Integer> articulosVistos = new HashSet<>();
        for (DetalleItemRequest item : request.detalles()) {
            if (!articulosVistos.add(item.idArticulo())) {
                throw new DuplicateResourceException("Artículo repetido en el ingreso: " + item.idArticulo());
            }
        }

        List<DetalleItemRequest> detallesOrdenados = request.detalles().stream()
                .sorted(Comparator.comparing(DetalleItemRequest::idArticulo))
                .toList();

        Proveedor proveedor = proveedorRepository.findById(request.idProveedor())
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado con ID: " + request.idProveedor()));

        Ingreso ingreso = Ingreso.builder()
                .proveedor(proveedor)
                .numeroOrdenCompra(ordenCompraLimpia)
                .descripcion(request.descripcion() != null ? request.descripcion().trim() : "")
                .fecha(LocalDateTime.now())
                .estado("1")
                .build();
        Ingreso ingresoGuardado = ingresoRepository.save(ingreso);

        Usuario usuarioActual = obtenerUsuarioActual();
        List<DetalleIngresoResponse> detallesResponse = new ArrayList<>();

        for (DetalleItemRequest item : detallesOrdenados) {
            Articulo articulo = articuloRepository.findByIdWithLock(item.idArticulo())
                    .orElseThrow(() -> new ResourceNotFoundException("Artículo no encontrado con ID: " + item.idArticulo()));

            validarCantidadSegunUnidad(articulo, item.cantidad());

            if (!"1".equals(articulo.getEstado())) {
                throw new BusinessException("Artículo dado de baja: " + articulo.getCodigo());
            }

            BigDecimal saldoAnterior = articulo.getSaldo() != null ? articulo.getSaldo() : BigDecimal.ZERO;
            BigDecimal cantidad = item.cantidad();
            BigDecimal nuevoSaldo = saldoAnterior.add(cantidad);

            articulo.setSaldo(nuevoSaldo);
            articulo.setPrecio(item.precio());
            articuloRepository.save(articulo);

            DetalleIngreso detalle = DetalleIngreso.builder()
                    .ingreso(ingresoGuardado)
                    .articulo(articulo)
                    .cantidad(cantidad)
                    .precio(item.precio())
                    .saldo(nuevoSaldo)
                    .fecha(LocalDateTime.now())
                    .tipo("i")
                    .build();
            DetalleIngreso detalleGuardado = detalleIngresoRepository.save(detalle);

            KardexMovimiento kardex = KardexMovimiento.builder()
                    .articulo(articulo)
                    .tipoMovimiento(TipoMovimiento.INGRESO)
                    .documentoTipo("INGRESO")
                    .documentoId(ingresoGuardado.getId())
                    .cantidadEntrada(cantidad)
                    .cantidadSalida(BigDecimal.ZERO)
                    .saldoResultante(nuevoSaldo)
                    .usuario(usuarioActual)
                    .fechaHora(LocalDateTime.now())
                    .build();
            kardexMovimientoRepository.save(kardex);

            detallesResponse.add(new DetalleIngresoResponse(
                    detalleGuardado.getId(),
                    articulo.getId(),
                    articulo.getCodigo(),
                    articulo.getDescripcion(),
                    detalleGuardado.getCantidad(),
                    detalleGuardado.getPrecio(),
                    detalleGuardado.getSaldo(),
                    detalleGuardado.getFecha(),
                    detalleGuardado.getTipo()
            ));
        }

        log.info("Ingreso ID={} registrado con {} artículos", ingresoGuardado.getId(), detallesResponse.size());

        return crearIngresoResponse(ingresoGuardado, calcularTotal(detallesResponse), detallesResponse);
    }

    private IngresoResponse construirIngresoResponse(Ingreso ingreso) {
        List<DetalleIngreso> detalles = detalleIngresoRepository.findByIngreso_IdOrderByIdAsc(ingreso.getId());
        List<DetalleIngresoResponse> detallesResponse = detalles.stream()
                .map(d -> new DetalleIngresoResponse(
                        d.getId(),
                        d.getArticulo() != null ? d.getArticulo().getId() : null,
                        d.getArticulo() != null ? d.getArticulo().getCodigo() : null,
                        d.getArticulo() != null ? d.getArticulo().getDescripcion() : null,
                        d.getCantidad(),
                        d.getPrecio(),
                        d.getSaldo(),
                        d.getFecha(),
                        d.getTipo()
                ))
                .toList();

        return crearIngresoResponse(ingreso, calcularTotal(detallesResponse), detallesResponse);
    }

    private IngresoResponse construirIngresoResumenResponse(Ingreso ingreso, BigDecimal total) {
        return crearIngresoResponse(ingreso, total, List.of());
    }

    private IngresoResponse crearIngresoResponse(Ingreso ingreso, BigDecimal total, List<DetalleIngresoResponse> detalles) {
        return new IngresoResponse(
                ingreso.getId(),
                ingreso.getProveedor() != null ? ingreso.getProveedor().getId() : null,
                ingreso.getProveedor() != null ? ingreso.getProveedor().getRazonSocial() : null,
                ingreso.getProveedor() != null ? ingreso.getProveedor().getRuc() : null,
                ingreso.getNumeroOrdenCompra(),
                ingreso.getDescripcion(),
                ingreso.getFecha(),
                ingreso.getEstado(),
                total,
                detalles
        );
    }

    private void validarCantidadSegunUnidad(Articulo articulo, BigDecimal cantidad) {
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("La cantidad debe ser mayor a cero.");
        }
        boolean permiteDecimales = articulo.getUnidadMedida() == null
                || Boolean.TRUE.equals(articulo.getUnidadMedida().getPermiteDecimales());
        if (!permiteDecimales) {
            if (cantidad.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) != 0) {
                throw new BusinessException("El artículo '" + articulo.getDescripcion() + "' no permite cantidades decimales.");
            }
        } else {
            if (cantidad.stripTrailingZeros().scale() > 2) {
                throw new BusinessException("La cantidad para el artículo '" + articulo.getDescripcion() + "' no puede superar 2 decimales.");
            }
        }
    }

    private BigDecimal calcularTotal(List<DetalleIngresoResponse> detalles) {
        return detalles.stream()
                .map(d -> d.cantidad().multiply(d.precio()).setScale(2, RoundingMode.HALF_UP))
                .reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
    }

    private Usuario obtenerUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetailsImpl userDetails) {
            return usuarioRepository.findById(userDetails.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado en base de datos"));
        }
        throw new AccessDeniedException("Usuario no autenticado.");
    }

    private BigDecimal extraerMontoSeguro(Object valor) {
        if (valor == null) return BigDecimal.ZERO;
        if (valor instanceof BigDecimal bd) return bd;
        if (valor instanceof Number num) return BigDecimal.valueOf(num.doubleValue());
        return new BigDecimal(valor.toString());
    }
}
