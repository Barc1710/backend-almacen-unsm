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
import java.util.stream.Collectors;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.unsm.almacen.dto.common.PageResponse;
import pe.edu.unsm.almacen.dto.request.DetalleEgresoRequest;
import pe.edu.unsm.almacen.dto.request.EgresoCreateRequest;
import pe.edu.unsm.almacen.dto.response.DetalleEgresoResponse;
import pe.edu.unsm.almacen.dto.response.EgresoResponse;
import pe.edu.unsm.almacen.entity.Area;
import pe.edu.unsm.almacen.entity.Articulo;
import pe.edu.unsm.almacen.entity.Cliente;
import pe.edu.unsm.almacen.entity.DetalleEgreso;
import pe.edu.unsm.almacen.entity.Egreso;
import pe.edu.unsm.almacen.entity.Encargado;
import pe.edu.unsm.almacen.entity.EncargadoAlmacen;
import pe.edu.unsm.almacen.entity.KardexMovimiento;
import pe.edu.unsm.almacen.entity.TipoEgreso;
import pe.edu.unsm.almacen.entity.TipoMovimiento;
import pe.edu.unsm.almacen.entity.Usuario;
import pe.edu.unsm.almacen.exception.BusinessException;
import pe.edu.unsm.almacen.exception.DuplicateResourceException;
import pe.edu.unsm.almacen.exception.ResourceNotFoundException;
import pe.edu.unsm.almacen.exception.StockInsuficienteException;
import pe.edu.unsm.almacen.repository.AreaRepository;
import pe.edu.unsm.almacen.repository.ArticuloRepository;
import pe.edu.unsm.almacen.repository.ClienteRepository;
import pe.edu.unsm.almacen.repository.DetalleEgresoRepository;
import pe.edu.unsm.almacen.repository.DetalleIngresoRepository;
import pe.edu.unsm.almacen.repository.EgresoRepository;
import pe.edu.unsm.almacen.repository.EncargadoAlmacenRepository;
import pe.edu.unsm.almacen.repository.EncargadoRepository;
import pe.edu.unsm.almacen.repository.KardexMovimientoRepository;
import pe.edu.unsm.almacen.repository.UsuarioRepository;
import pe.edu.unsm.almacen.security.service.UserDetailsImpl;
import pe.edu.unsm.almacen.service.IEgresoService;

@Service
@RequiredArgsConstructor
@Slf4j
public class EgresoServiceImpl implements IEgresoService {

    private final EgresoRepository egresoRepository;
    private final DetalleEgresoRepository detalleEgresoRepository;
    private final DetalleIngresoRepository detalleIngresoRepository;
    private final ArticuloRepository articuloRepository;
    private final ClienteRepository clienteRepository;
    private final EncargadoRepository encargadoRepository;
    private final AreaRepository areaRepository;
    private final EncargadoAlmacenRepository encargadoAlmacenRepository;
    private final KardexMovimientoRepository kardexMovimientoRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EgresoResponse> listar(Integer idCliente, Integer idArea, LocalDate desde, LocalDate hasta, String estado, Pageable pageable) {
        LocalDateTime desdeDateTime = desde != null ? desde.atStartOfDay() : null;
        LocalDateTime hastaDateTime = hasta != null ? hasta.atTime(23, 59, 59) : null;

        Page<Egreso> page = egresoRepository.listarPaginado(idCliente, idArea, desdeDateTime, hastaDateTime, estado, pageable);
        List<Integer> ids = page.getContent().stream().map(Egreso::getId).toList();
        Map<Integer, BigDecimal> totales = ids.isEmpty() ? Map.of()
                : detalleEgresoRepository.sumarTotalesPorEgresoIds(ids).stream()
                        .collect(Collectors.toMap(row -> (Integer) row[0], row -> extraerMontoSeguro(row[1])));
        return PageResponse.of(page.map(egreso -> construirEgresoResumenResponse(
                egreso, totales.getOrDefault(egreso.getId(), BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP))));
    }

    @Override
    @Transactional(readOnly = true)
    public EgresoResponse obtenerPorId(Integer id) {
        Egreso egreso = egresoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Egreso no encontrado con ID: " + id));

        return construirEgresoResponse(egreso);
    }

    @Override
    @Transactional
    public EgresoResponse registrar(EgresoCreateRequest request) {
        Set<Integer> articulosVistos = new HashSet<>();
        for (DetalleEgresoRequest item : request.detalles()) {
            if (!articulosVistos.add(item.idArticulo())) {
                throw new DuplicateResourceException("Artículo repetido en el egreso: " + item.idArticulo());
            }
        }

        List<DetalleEgresoRequest> detallesOrdenados = request.detalles().stream()
                .sorted(Comparator.comparing(DetalleEgresoRequest::idArticulo))
                .toList();

        TipoEgreso tipoEgreso = request.tipoEgreso() != null ? request.tipoEgreso() : TipoEgreso.DESPACHO_ORDINARIO;
        if (tipoEgreso != TipoEgreso.DESPACHO_ORDINARIO) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean esAdmin = auth != null && auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"));
            if (!esAdmin) {
                throw new AccessDeniedException("Solo ADMINISTRADOR puede registrar una baja.");
            }
        }

        Cliente cliente = clienteRepository.findById(request.idCliente())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + request.idCliente()));

        Encargado encargado = null;
        String nombreEncargadoLibre = null;
        if (request.idEncargado() != null) {
            encargado = encargadoRepository.findById(request.idEncargado())
                    .orElseThrow(() -> new ResourceNotFoundException("Encargado no encontrado con ID: " + request.idEncargado()));
        } else if (request.nombreEncargadoLibre() != null && !request.nombreEncargadoLibre().trim().isEmpty()) {
            nombreEncargadoLibre = request.nombreEncargadoLibre().trim();
        } else {
            throw new BusinessException("Indica un encargado o su nombre.");
        }

        Area area = areaRepository.findById(request.idArea())
                .orElseThrow(() -> new ResourceNotFoundException("Área no encontrada con ID: " + request.idArea()));
        EncargadoAlmacen encargadoAlmacen = encargadoAlmacenRepository.findById(request.idEncargadoAlmacen())
                .orElseThrow(() -> new ResourceNotFoundException("Encargado de almacén no encontrado con ID: " + request.idEncargadoAlmacen()));

        Usuario usuarioActual = obtenerUsuarioActual();

        String prefijo = (request.prefijo() != null && !request.prefijo().isBlank())
                ? request.prefijo().trim().toUpperCase()
                : "A" + String.format("%02d", Year.now().getValue() % 100);

        Integer maxCorrelativo = egresoRepository.obtenerMaximoCorrelativo(prefijo);
        int nuevoCorrelativo = (maxCorrelativo != null ? maxCorrelativo : 0) + 1;

        Egreso egreso = Egreso.builder()
                .cliente(cliente)
                .encargado(encargado)
                .nombreEncargadoLibre(nombreEncargadoLibre)
                .area(area)
                .encargadoAlmacen(encargadoAlmacen)
                .usuario(usuarioActual)
                .ambiente(request.ambiente() != null ? request.ambiente().trim() : "")
                .prefijo(prefijo)
                .correlativo(nuevoCorrelativo)
                .tipoEgreso(tipoEgreso)
                .fecha(LocalDateTime.now())
                .estado("1")
                .build();
        Egreso egresoGuardado = egresoRepository.saveAndFlush(egreso);

        List<Integer> articuloIds = detallesOrdenados.stream()
                .map(DetalleEgresoRequest::idArticulo)
                .toList();
        Map<Integer, String> ordenesCompra = buscarOrdenesCompra(articuloIds);

        List<DetalleEgresoResponse> detallesResponse = new ArrayList<>();
        BigDecimal totalEgreso = BigDecimal.ZERO;

        for (DetalleEgresoRequest item : detallesOrdenados) {
            Articulo articulo = articuloRepository.findByIdWithLock(item.idArticulo())
                    .orElseThrow(() -> new ResourceNotFoundException("Artículo no encontrado con ID: " + item.idArticulo()));

            if (!"1".equals(articulo.getEstado()) || !Boolean.TRUE.equals(articulo.getActivo())) {
                throw new BusinessException("Artículo inactivo: " + articulo.getCodigo());
            }

            validarCantidadSegunUnidad(articulo, item.cantidad());

            BigDecimal saldoActual = articulo.getSaldo() != null ? articulo.getSaldo() : BigDecimal.ZERO;
            BigDecimal cantidad = item.cantidad();

            if (saldoActual.compareTo(cantidad) < 0) {
                throw new StockInsuficienteException("Stock insuficiente para " + articulo.getCodigo()
                        + ": disponible " + saldoActual + ", solicitado " + cantidad);
            }

            BigDecimal nuevoSaldo = saldoActual.subtract(cantidad);
            articulo.setSaldo(nuevoSaldo);
            articuloRepository.save(articulo);

            BigDecimal precioVigente = articulo.getPrecio() != null ? articulo.getPrecio() : BigDecimal.ZERO;
            BigDecimal subtotal = cantidad.multiply(precioVigente).setScale(2, RoundingMode.HALF_UP);
            totalEgreso = totalEgreso.add(subtotal);

            DetalleEgreso detalle = DetalleEgreso.builder()
                    .egreso(egresoGuardado)
                    .articulo(articulo)
                    .cantidad(cantidad)
                    .precio(precioVigente)
                    .saldo(nuevoSaldo)
                    .fecha(LocalDateTime.now())
                    .tipo("e")
                    .build();
            DetalleEgreso detalleGuardado = detalleEgresoRepository.save(detalle);

            TipoMovimiento tipoMovimientoKardex;
            String documentoTipoKardex;
            if (tipoEgreso == TipoEgreso.BAJA_DETERIORO) {
                tipoMovimientoKardex = TipoMovimiento.BAJA_DETERIORO;
                documentoTipoKardex = "ACTA_BAJA";
            } else if (tipoEgreso == TipoEgreso.BAJA_VENCIMIENTO) {
                tipoMovimientoKardex = TipoMovimiento.BAJA_VENCIMIENTO;
                documentoTipoKardex = "ACTA_BAJA";
            } else {
                tipoMovimientoKardex = TipoMovimiento.EGRESO;
                documentoTipoKardex = "EGRESO";
            }

            KardexMovimiento kardex = KardexMovimiento.builder()
                    .articulo(articulo)
                    .tipoMovimiento(tipoMovimientoKardex)
                    .documentoTipo(documentoTipoKardex)
                    .documentoId(egresoGuardado.getId())
                    .cantidadEntrada(BigDecimal.ZERO)
                    .cantidadSalida(cantidad)
                    .saldoResultante(nuevoSaldo)
                    .usuario(usuarioActual)
                    .fechaHora(LocalDateTime.now())
                    .build();
            kardexMovimientoRepository.save(kardex);

            String oc = ordenesCompra.get(articulo.getId());

            detallesResponse.add(new DetalleEgresoResponse(
                    detalleGuardado.getId(),
                    articulo.getId(),
                    articulo.getCodigo(),
                    articulo.getDescripcion(),
                    oc,
                    detalleGuardado.getCantidad(),
                    detalleGuardado.getPrecio(),
                    subtotal,
                    detalleGuardado.getSaldo(),
                    detalleGuardado.getFecha(),
                    detalleGuardado.getTipo()
            ));
        }

        log.info("Egreso {} (ID={}) registrado con {} ítems",
                egresoGuardado.getNumeroCompleto(), egresoGuardado.getId(), detallesResponse.size());

        return crearEgresoResponse(egresoGuardado, totalEgreso, detallesResponse);
    }

    @Override
    @Transactional
    public EgresoResponse anular(Integer id) {
        Egreso egreso = egresoRepository.findByIdWithLock(id)
                .orElseThrow(() -> new ResourceNotFoundException("Egreso no encontrado con ID: " + id));

        if (!"1".equals(egreso.getEstado())) {
            throw new BusinessException("Egreso inactivo o ya anulado: " + id);
        }

        egreso.setEstado("0");
        egresoRepository.save(egreso);

        Usuario usuarioActual = obtenerUsuarioActual();

        List<DetalleEgreso> detalles = detalleEgresoRepository.findByEgreso_IdOrderByIdAsc(egreso.getId());
        List<DetalleEgreso> detallesOrdenados = detalles.stream()
                .sorted(Comparator.comparing(d -> d.getArticulo().getId()))
                .toList();

        for (DetalleEgreso detalle : detallesOrdenados) {
            Articulo articulo = articuloRepository.findByIdWithLock(detalle.getArticulo().getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Artículo no encontrado con ID: " + detalle.getArticulo().getId()
                    ));

            BigDecimal saldoActual = articulo.getSaldo() != null ? articulo.getSaldo() : BigDecimal.ZERO;
            BigDecimal nuevoSaldo = saldoActual.add(detalle.getCantidad());

            articulo.setSaldo(nuevoSaldo);
            articuloRepository.save(articulo);

            KardexMovimiento kardex = KardexMovimiento.builder()
                    .articulo(articulo)
                    .tipoMovimiento(TipoMovimiento.REVERSO_EGRESO)
                    .documentoTipo("ANULACION_EGRESO")
                    .documentoId(egreso.getId())
                    .cantidadEntrada(detalle.getCantidad())
                    .cantidadSalida(BigDecimal.ZERO)
                    .saldoResultante(nuevoSaldo)
                    .usuario(usuarioActual)
                    .fechaHora(LocalDateTime.now())
                    .build();
            kardexMovimientoRepository.save(kardex);
        }

        log.info("Egreso {} (ID={}) anulado y stock restituido para {} artículos",
                egreso.getNumeroCompleto(), egreso.getId(), detalles.size());

        return construirEgresoResponse(egreso);
    }

    private EgresoResponse construirEgresoResponse(Egreso egreso) {
        List<DetalleEgreso> detalles = detalleEgresoRepository.findByEgreso_IdOrderByIdAsc(egreso.getId());
        List<Integer> articuloIds = detalles.stream()
                .filter(d -> d.getArticulo() != null)
                .map(d -> d.getArticulo().getId())
                .distinct()
                .toList();

        Map<Integer, String> ordenesCompra = buscarOrdenesCompra(articuloIds);

        BigDecimal totalEgreso = BigDecimal.ZERO;
        List<DetalleEgresoResponse> detallesResponse = new ArrayList<>();
        for (DetalleEgreso d : detalles) {
            BigDecimal cantidad = d.getCantidad() != null ? d.getCantidad() : BigDecimal.ZERO;
            BigDecimal precio = d.getPrecio() != null ? d.getPrecio() : BigDecimal.ZERO;
            BigDecimal subtotal = cantidad.multiply(precio).setScale(2, RoundingMode.HALF_UP);
            totalEgreso = totalEgreso.add(subtotal);

            Integer artId = d.getArticulo() != null ? d.getArticulo().getId() : null;
            String oc = artId != null ? ordenesCompra.get(artId) : null;

            detallesResponse.add(new DetalleEgresoResponse(
                    d.getId(),
                    artId,
                    d.getArticulo() != null ? d.getArticulo().getCodigo() : null,
                    d.getArticulo() != null ? d.getArticulo().getDescripcion() : null,
                    oc,
                    d.getCantidad(),
                    d.getPrecio(),
                    subtotal,
                    d.getSaldo(),
                    d.getFecha(),
                    d.getTipo()
            ));
        }

        return crearEgresoResponse(egreso, totalEgreso, detallesResponse);
    }

    private EgresoResponse construirEgresoResumenResponse(Egreso egreso, BigDecimal total) {
        return crearEgresoResponse(egreso, total, List.of());
    }

    private EgresoResponse crearEgresoResponse(Egreso egreso, BigDecimal total, List<DetalleEgresoResponse> detalles) {
        return new EgresoResponse(
                egreso.getId(),
                egreso.getCliente() != null ? egreso.getCliente().getId() : null,
                egreso.getCliente() != null ? egreso.getCliente().getNombre() : null,
                egreso.getEncargado() != null ? egreso.getEncargado().getId() : null,
                egreso.getEncargado() != null ? egreso.getEncargado().getNombreCompleto() : null,
                egreso.getNombreEncargadoLibre(),
                egreso.getArea() != null ? egreso.getArea().getId() : null,
                egreso.getArea() != null ? egreso.getArea().getNombre() : null,
                egreso.getEncargadoAlmacen() != null ? egreso.getEncargadoAlmacen().getId() : null,
                egreso.getEncargadoAlmacen() != null ? egreso.getEncargadoAlmacen().getNombre() : null,
                egreso.getUsuario() != null ? egreso.getUsuario().getId() : null,
                egreso.getUsuario() != null ? egreso.getUsuario().getNombreCompleto() : null,
                egreso.getAmbiente(),
                egreso.getPrefijo(),
                egreso.getCorrelativo(),
                egreso.getNumeroCompleto(),
                egreso.getTipoEgreso() != null ? egreso.getTipoEgreso().name() : "DESPACHO_ORDINARIO",
                egreso.getFecha(),
                egreso.getEstado(),
                total,
                detalles
        );
    }

    private Map<Integer, String> buscarOrdenesCompra(List<Integer> articuloIds) {
        Map<Integer, String> ordenes = new HashMap<>();
        if (articuloIds.isEmpty()) {
            return ordenes;
        }
        for (Object[] row : detalleIngresoRepository.findOrdenesCompraPorArticuloIds(articuloIds)) {
            ordenes.putIfAbsent((Integer) row[0], (String) row[1]);
        }
        return ordenes;
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

    private Usuario obtenerUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetailsImpl userDetails) {
            return usuarioRepository.findById(userDetails.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado en la base de datos"));
        }
        throw new BusinessException("Usuario no autenticado.");
    }

    private BigDecimal extraerMontoSeguro(Object valor) {
        if (valor == null) return BigDecimal.ZERO;
        if (valor instanceof BigDecimal bd) return bd;
        if (valor instanceof Number num) return BigDecimal.valueOf(num.doubleValue());
        return new BigDecimal(valor.toString());
    }
}
