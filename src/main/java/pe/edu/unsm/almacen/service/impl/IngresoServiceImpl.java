package pe.edu.unsm.almacen.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import pe.edu.unsm.almacen.entity.Encargado;
import pe.edu.unsm.almacen.entity.EncargadoAlmacen;
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
import pe.edu.unsm.almacen.repository.EncargadoAlmacenRepository;
import pe.edu.unsm.almacen.repository.EncargadoRepository;
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
    private final EncargadoAlmacenRepository encargadoAlmacenRepository;
    private final EncargadoRepository encargadoRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<IngresoResponse> listar(Integer idProveedor, LocalDate desde, LocalDate hasta, Pageable pageable) {
        LocalDateTime desdeDateTime = desde != null ? desde.atStartOfDay() : null;
        LocalDateTime hastaDateTime = hasta != null ? hasta.atTime(23, 59, 59) : null;

        Page<Ingreso> page = ingresoRepository.listarPaginado(idProveedor, desdeDateTime, hastaDateTime, pageable);
        List<Integer> ids = page.getContent().stream().map(Ingreso::getId).toList();

        Map<Integer, BigDecimal> totales = new java.util.HashMap<>();
        Map<Integer, Integer> itemsCount = new java.util.HashMap<>();

        if (!ids.isEmpty()) {
            for (Object[] row : detalleIngresoRepository.sumarTotalesYContarItemsPorIngresoIds(ids)) {
                Integer id = (Integer) row[0];
                BigDecimal total = extraerMontoSeguro(row[1]);
                Integer count = row[2] instanceof Number num ? num.intValue() : 0;
                totales.put(id, total);
                itemsCount.put(id, count);
            }
        }

        return PageResponse.of(page.map(ingreso -> {
            BigDecimal total = totales.getOrDefault(ingreso.getId(), BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
            int count = itemsCount.getOrDefault(ingreso.getId(), 0);
            return construirIngresoResumenResponse(ingreso, total, count);
        }));
    }

    @Override
    @Transactional(readOnly = true)
    public IngresoResponse obtenerPorId(Integer id) {
        Ingreso ingreso = ingresoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ingreso no encontrado con ID: " + id));

        return construirIngresoResponse(ingreso);
    }

    @Override
    @Transactional(readOnly = true)
    public String obtenerSiguienteNumeroIngreso() {
        String prefijo = generarPrefijoAnual();
        int nuevoCorrelativo = calcularSiguienteCorrelativo(prefijo);
        return String.format("%s-%04d", prefijo, nuevoCorrelativo);
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

        String prefijo = generarPrefijoAnual();
        int nuevoCorrelativo = calcularSiguienteCorrelativo(prefijo);

        Usuario usuarioActual = obtenerUsuarioActual();

        // Resolver Encargado de Almacén (titular activo por defecto si no se especificó)
        EncargadoAlmacen encargadoAlmacen = null;
        if (request.idEncargadoAlmacen() != null) {
            encargadoAlmacen = encargadoAlmacenRepository.findById(request.idEncargadoAlmacen())
                    .orElseThrow(() -> new ResourceNotFoundException("Encargado de almacén no encontrado con ID: " + request.idEncargadoAlmacen()));
        } else {
            encargadoAlmacen = encargadoAlmacenRepository.findFirstByEsTitularTrueAndEstado("1")
                    .orElseGet(() -> encargadoAlmacenRepository.findByEstadoOrderByNombreAsc("1").stream().findFirst().orElse(null));
        }
        String nombreEncargadoAlmacen = encargadoAlmacen != null ? encargadoAlmacen.getNombre() : null;

        // Resolver Jefe USG (primer jefe activo por defecto si no se especificó)
        Encargado jefe = null;
        if (request.idJefe() != null) {
            jefe = encargadoRepository.findById(request.idJefe())
                    .orElseThrow(() -> new ResourceNotFoundException("Jefe no encontrado con ID: " + request.idJefe()));
        } else {
            jefe = encargadoRepository.findByEstadoOrderByApellidosAsc("1").stream().findFirst().orElse(null);
        }
        String nombreJefe = jefe != null ? jefe.getNombreCompleto() : null;

        Ingreso ingreso = Ingreso.builder()
                .proveedor(proveedor)
                .usuario(usuarioActual)
                .encargadoAlmacen(encargadoAlmacen)
                .jefe(jefe)
                .nombreEncargadoAlmacen(nombreEncargadoAlmacen)
                .nombreJefe(nombreJefe)
                .prefijo(prefijo)
                .correlativo(nuevoCorrelativo)
                .numeroOrdenCompra(ordenCompraLimpia)
                .descripcion(request.descripcion() != null ? request.descripcion().trim() : "")
                .fecha(LocalDateTime.now())
                .estado("1")
                .build();
        Ingreso ingresoGuardado = ingresoRepository.save(ingreso);

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
            if (item.precio() != null && item.precio().compareTo(BigDecimal.ZERO) > 0) {
                articulo.setPrecio(item.precio());
            }
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

            detallesResponse.add(mapearDetalleResponse(detalleGuardado, articulo));
        }

        log.info("Ingreso {} (ID={}) registrado con {} artículos",
                ingresoGuardado.getNumeroCompleto(), ingresoGuardado.getId(), detallesResponse.size());

        return crearIngresoResponse(ingresoGuardado, calcularTotal(detallesResponse), detallesResponse.size(), detallesResponse);
    }

    private IngresoResponse construirIngresoResponse(Ingreso ingreso) {
        List<DetalleIngreso> detalles = detalleIngresoRepository.findByIngreso_IdOrderByIdAsc(ingreso.getId());
        List<DetalleIngresoResponse> detallesResponse = detalles.stream()
                .map(d -> mapearDetalleResponse(d, d.getArticulo()))
                .toList();

        return crearIngresoResponse(ingreso, calcularTotal(detallesResponse), detallesResponse.size(), detallesResponse);
    }

    private IngresoResponse construirIngresoResumenResponse(Ingreso ingreso, BigDecimal total, Integer totalItems) {
        return crearIngresoResponse(ingreso, total, totalItems, List.of());
    }

    @Override
    @Transactional
    public IngresoResponse anular(Integer id) {
        Ingreso ingreso = ingresoRepository.findByIdWithLock(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ingreso no encontrado con ID: " + id));

        if (!"1".equals(ingreso.getEstado())) {
            throw new BusinessException("Ingreso inactivo o ya anulado: " + id);
        }

        Usuario usuarioActual = obtenerUsuarioActual();

        List<DetalleIngreso> detalles = detalleIngresoRepository.findByIngreso_IdOrderByIdAsc(ingreso.getId());
        List<DetalleIngreso> detallesOrdenados = detalles.stream()
                .sorted(Comparator.comparing(d -> d.getArticulo().getId()))
                .toList();

        // 1. Cargar artículos con bloqueo pesimista y validar suficiencia de existencias en una sola pasada
        Map<Integer, Articulo> articulosMap = new HashMap<>();
        for (DetalleIngreso detalle : detallesOrdenados) {
            Integer idArticulo = detalle.getArticulo().getId();
            Articulo articulo = articuloRepository.findByIdWithLock(idArticulo)
                    .orElseThrow(() -> new ResourceNotFoundException("Artículo no encontrado con ID: " + idArticulo));
            articulosMap.put(idArticulo, articulo);

            BigDecimal saldoActual = articulo.getSaldo() != null ? articulo.getSaldo() : BigDecimal.ZERO;
            BigDecimal cantidadRevertir = detalle.getCantidad() != null ? detalle.getCantidad() : BigDecimal.ZERO;

            if (saldoActual.compareTo(cantidadRevertir) < 0) {
                throw new BusinessException(
                        "No se puede anular el ingreso " + ingreso.getNumeroCompleto() +
                        ": El artículo '" + articulo.getDescripcion() + "' (" + articulo.getCodigo() +
                        ") cuenta con un saldo actual de " + saldoActual +
                        ", insuficiente para descontar la cantidad ingresada de " + cantidadRevertir +
                        ". Es probable que dicho stock ya haya sido consumido o despachado en egresos posteriores."
                );
            }
        }

        // 2. Revertir existencias utilizando las entidades ya cargadas y registrar movimiento en Kardex
        for (DetalleIngreso detalle : detallesOrdenados) {
            Articulo articulo = articulosMap.get(detalle.getArticulo().getId());
            BigDecimal saldoActual = articulo.getSaldo() != null ? articulo.getSaldo() : BigDecimal.ZERO;
            BigDecimal cantidadRevertir = detalle.getCantidad() != null ? detalle.getCantidad() : BigDecimal.ZERO;
            BigDecimal nuevoSaldo = saldoActual.subtract(cantidadRevertir);

            articulo.setSaldo(nuevoSaldo);
            articuloRepository.save(articulo);

            KardexMovimiento kardex = KardexMovimiento.builder()
                    .articulo(articulo)
                    .tipoMovimiento(TipoMovimiento.REVERSO_INGRESO)
                    .documentoTipo("ANULACION_INGRESO")
                    .documentoId(ingreso.getId())
                    .cantidadEntrada(BigDecimal.ZERO)
                    .cantidadSalida(cantidadRevertir)
                    .saldoResultante(nuevoSaldo)
                    .usuario(usuarioActual)
                    .fechaHora(LocalDateTime.now())
                    .build();
            kardexMovimientoRepository.save(kardex);
        }

        // 3. Marcar el ingreso con estado anulado (0)
        ingreso.setEstado("0");
        ingresoRepository.save(ingreso);

        log.info("Ingreso {} (ID={}) anulado con éxito por usuario {}. Stock revertido para {} artículos.",
                ingreso.getNumeroCompleto(), ingreso.getId(), usuarioActual.getUsuario(), detalles.size());

        return construirIngresoResponse(ingreso);
    }

    private DetalleIngresoResponse mapearDetalleResponse(DetalleIngreso detalle, Articulo articulo) {
        String simbolo = articulo != null && articulo.getUnidadMedida() != null
                ? articulo.getUnidadMedida().getSimbolo() : null;
        Boolean permiteDec = articulo != null && articulo.getUnidadMedida() != null
                ? articulo.getUnidadMedida().getPermiteDecimales() : null;

        return new DetalleIngresoResponse(
                detalle.getId(),
                articulo != null ? articulo.getId() : null,
                articulo != null ? articulo.getCodigo() : null,
                articulo != null ? articulo.getDescripcion() : null,
                simbolo,
                permiteDec,
                detalle.getCantidad(),
                detalle.getPrecio(),
                detalle.getSaldo(),
                detalle.getFecha(),
                detalle.getTipo()
        );
    }

    private String generarPrefijoAnual() {
        return "I" + String.format("%02d", Year.now().getValue() % 100);
    }

    private int calcularSiguienteCorrelativo(String prefijo) {
        Integer maxCorrelativo = ingresoRepository.obtenerMaximoCorrelativo(prefijo);
        return (maxCorrelativo != null ? maxCorrelativo : 0) + 1;
    }

    private IngresoResponse crearIngresoResponse(Ingreso ingreso, BigDecimal total, Integer totalItems, List<DetalleIngresoResponse> detalles) {
        return new IngresoResponse(
                ingreso.getId(),
                ingreso.getProveedor() != null ? ingreso.getProveedor().getId() : null,
                ingreso.getProveedor() != null ? ingreso.getProveedor().getRazonSocial() : null,
                ingreso.getProveedor() != null ? ingreso.getProveedor().getRuc() : null,
                ingreso.getUsuario() != null ? ingreso.getUsuario().getId() : null,
                ingreso.getUsuario() != null ? ingreso.getUsuario().getNombreCompleto() : null,
                ingreso.getEncargadoAlmacen() != null ? ingreso.getEncargadoAlmacen().getId() : null,
                ingreso.getNombreEncargadoAlmacen() != null ? ingreso.getNombreEncargadoAlmacen() : (ingreso.getEncargadoAlmacen() != null ? ingreso.getEncargadoAlmacen().getNombre() : null),
                ingreso.getJefe() != null ? ingreso.getJefe().getId() : null,
                ingreso.getNombreJefe() != null ? ingreso.getNombreJefe() : (ingreso.getJefe() != null ? ingreso.getJefe().getNombreCompleto() : null),
                ingreso.getPrefijo(),
                ingreso.getCorrelativo(),
                ingreso.getNumeroCompleto(),
                ingreso.getNumeroOrdenCompra(),
                ingreso.getDescripcion(),
                ingreso.getFecha(),
                ingreso.getEstado(),
                total,
                totalItems,
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
