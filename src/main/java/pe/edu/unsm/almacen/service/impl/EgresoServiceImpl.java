package pe.edu.unsm.almacen.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
        log.info("Iniciando registro de despacho/egreso para cliente ID: {}", request.idCliente());

        // 1. Validación de Unicidad en el Lote (Anti-duplicidad)
        Set<Integer> articulosVistos = new HashSet<>();
        for (DetalleEgresoRequest item : request.detalles()) {
            if (!articulosVistos.add(item.idArticulo())) {
                throw new DuplicateResourceException(
                        "El artículo con ID " + item.idArticulo() + " está duplicado en la lista de detalles del despacho"
                );
            }
        }

        // 2. Prevención de Deadlocks: Ordenamiento ascendente de los ítems por ID de artículo
        List<DetalleEgresoRequest> detallesOrdenados = request.detalles().stream()
                .sorted(Comparator.comparing(DetalleEgresoRequest::idArticulo))
                .toList();

        // 3. Validación y carga de entidades maestras
        Cliente cliente = clienteRepository.findById(request.idCliente())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + request.idCliente()));
        Encargado encargado = encargadoRepository.findById(request.idEncargado())
                .orElseThrow(() -> new ResourceNotFoundException("Encargado no encontrado con ID: " + request.idEncargado()));
        Area area = areaRepository.findById(request.idArea())
                .orElseThrow(() -> new ResourceNotFoundException("Área no encontrada con ID: " + request.idArea()));
        EncargadoAlmacen encargadoAlmacen = encargadoAlmacenRepository.findById(request.idEncargadoAlmacen())
                .orElseThrow(() -> new ResourceNotFoundException("Encargado de almacén no encontrado con ID: " + request.idEncargadoAlmacen()));

        // 4. Seguridad Estricta: Obtención del usuario autenticado sin fallback silencioso
        Usuario usuarioActual = obtenerUsuarioActual();

        // La base actual no tiene una restricción única para el correlativo.
        // Este cerrojo mantiene la numeración segura dentro de esta instancia,
        // incluyendo el commit de la transacción.
        String prefijo = (request.prefijo() != null && !request.prefijo().isBlank())
                ? request.prefijo().trim().toUpperCase()
                : "A" + String.format("%02d", Year.now().getValue() % 100);

        Integer maxCorrelativo = egresoRepository.obtenerMaximoCorrelativo(prefijo);
        int nuevoCorrelativo = (maxCorrelativo != null ? maxCorrelativo : 0) + 1;

        // 6. Registro de la cabecera del Egreso
        Egreso egreso = Egreso.builder()
                .cliente(cliente)
                .encargado(encargado)
                .area(area)
                .encargadoAlmacen(encargadoAlmacen)
                .ambiente(request.ambiente() != null ? request.ambiente().trim() : "")
                .prefijo(prefijo)
                .correlativo(nuevoCorrelativo)
                .fecha(LocalDateTime.now())
                .estado("1")
                .build();
        Egreso egresoGuardado = egresoRepository.saveAndFlush(egreso);

        List<DetalleEgresoResponse> detallesResponse = new ArrayList<>();
        BigDecimal totalEgreso = BigDecimal.ZERO;

        // 7. Procesamiento de líneas: Bloqueo pesimista, control anti-saldo negativo, deducción y auditoría Kardex
        for (DetalleEgresoRequest item : detallesOrdenados) {
            Articulo articulo = articuloRepository.findByIdWithLock(item.idArticulo())
                    .orElseThrow(() -> new ResourceNotFoundException("Artículo no encontrado con ID: " + item.idArticulo()));

            if (!"1".equals(articulo.getEstado()) || !Boolean.TRUE.equals(articulo.getActivo())) {
                throw new BusinessException("El artículo " + articulo.getCodigo() + " no está activo para despachos.");
            }

            BigDecimal saldoActual = articulo.getSaldo() != null ? articulo.getSaldo() : BigDecimal.ZERO;
            BigDecimal cantidad = item.cantidad();

            // Regla estricta: No permitir saldo negativo bajo ninguna condición
            if (saldoActual.compareTo(cantidad) < 0) {
                throw new StockInsuficienteException(
                        "Stock insuficiente para el artículo '" + articulo.getDescripcion()
                                + "' (Código: " + articulo.getCodigo()
                                + "). Stock disponible: " + saldoActual
                                + ", cantidad solicitada: " + cantidad
                );
            }

            BigDecimal nuevoSaldo = saldoActual.subtract(cantidad);
            articulo.setSaldo(nuevoSaldo);
            articuloRepository.save(articulo);

            // Valorización de salida con precio vigente del catálogo maestro
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

            // Registro en libro mayor Kardex (EGRESO)
            KardexMovimiento kardex = KardexMovimiento.builder()
                    .articulo(articulo)
                    .tipoMovimiento(TipoMovimiento.EGRESO)
                    .documentoTipo("EGRESO")
                    .documentoId(egresoGuardado.getId())
                    .cantidadEntrada(BigDecimal.ZERO)
                    .cantidadSalida(cantidad)
                    .saldoResultante(nuevoSaldo)
                    .usuario(usuarioActual)
                    .fechaHora(LocalDateTime.now())
                    .build();
            kardexMovimientoRepository.save(kardex);

            detallesResponse.add(new DetalleEgresoResponse(
                    detalleGuardado.getId(),
                    articulo.getId(),
                    articulo.getCodigo(),
                    articulo.getDescripcion(),
                    detalleGuardado.getCantidad(),
                    detalleGuardado.getPrecio(),
                    subtotal,
                    detalleGuardado.getSaldo(),
                    detalleGuardado.getFecha(),
                    detalleGuardado.getTipo()
            ));
        }

        log.info("Egreso {} (ID={}) registrado exitosamente con {} ítems",
                egresoGuardado.getNumeroCompleto(), egresoGuardado.getId(), detallesResponse.size());

        return new EgresoResponse(
                egresoGuardado.getId(),
                cliente.getId(),
                cliente.getNombre(),
                encargado.getId(),
                encargado.getNombreCompleto(),
                area.getId(),
                area.getNombre(),
                encargadoAlmacen.getId(),
                encargadoAlmacen.getNombre(),
                egresoGuardado.getAmbiente(),
                egresoGuardado.getPrefijo(),
                egresoGuardado.getCorrelativo(),
                egresoGuardado.getNumeroCompleto(),
                egresoGuardado.getFecha(),
                egresoGuardado.getEstado(),
                totalEgreso,
                detallesResponse
        );
    }

    @Override
    @Transactional
    public EgresoResponse anular(Integer id) {
        log.info("Iniciando anulación de egreso con ID: {}", id);

        // 1. Bloqueo Pesimista en Anulación (Anti-doble devolución)
        Egreso egreso = egresoRepository.findByIdWithLock(id)
                .orElseThrow(() -> new ResourceNotFoundException("Egreso no encontrado con ID: " + id));

        // 2. Verificación estricta de estado
        if (!"1".equals(egreso.getEstado())) {
            throw new BusinessException("El egreso con ID " + id + " ya se encuentra anulado o no está activo.");
        }

        // 3. Marcar egreso como anulado
        egreso.setEstado("0");
        egresoRepository.save(egreso);

        // 4. Seguridad Estricta: Obtención del usuario autenticado
        Usuario usuarioActual = obtenerUsuarioActual();

        // 5. Cargar detalles del egreso y ordenar por ID de artículo para prevenir deadlocks
        List<DetalleEgreso> detalles = detalleEgresoRepository.findByEgreso_IdOrderByIdAsc(egreso.getId());
        List<DetalleEgreso> detallesOrdenados = detalles.stream()
                .sorted(Comparator.comparing(d -> d.getArticulo().getId()))
                .toList();

        // 6. Devolver cantidades al saldo del artículo con bloqueo pesimista y asentar en Kardex
        for (DetalleEgreso detalle : detallesOrdenados) {
            Articulo articulo = articuloRepository.findByIdWithLock(detalle.getArticulo().getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Artículo no encontrado con ID: " + detalle.getArticulo().getId()
                    ));

            BigDecimal saldoActual = articulo.getSaldo() != null ? articulo.getSaldo() : BigDecimal.ZERO;
            BigDecimal nuevoSaldo = saldoActual.add(detalle.getCantidad());

            articulo.setSaldo(nuevoSaldo);
            articuloRepository.save(articulo);

            // Registro en Kardex con tipo REVERSO_EGRESO
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

        log.info("Egreso {} (ID={}) anulado exitosamente y stock restituido para {} artículos",
                egreso.getNumeroCompleto(), egreso.getId(), detalles.size());

        return construirEgresoResponse(egreso);
    }

    private EgresoResponse construirEgresoResponse(Egreso egreso) {
        List<DetalleEgreso> detalles = detalleEgresoRepository.findByEgreso_IdOrderByIdAsc(egreso.getId());
        BigDecimal totalEgreso = BigDecimal.ZERO;

        List<DetalleEgresoResponse> detallesResponse = new ArrayList<>();
        for (DetalleEgreso d : detalles) {
            BigDecimal cantidad = d.getCantidad() != null ? d.getCantidad() : BigDecimal.ZERO;
            BigDecimal precio = d.getPrecio() != null ? d.getPrecio() : BigDecimal.ZERO;
            BigDecimal subtotal = cantidad.multiply(precio).setScale(2, RoundingMode.HALF_UP);
            totalEgreso = totalEgreso.add(subtotal);

            detallesResponse.add(new DetalleEgresoResponse(
                    d.getId(),
                    d.getArticulo() != null ? d.getArticulo().getId() : null,
                    d.getArticulo() != null ? d.getArticulo().getCodigo() : null,
                    d.getArticulo() != null ? d.getArticulo().getDescripcion() : null,
                    d.getCantidad(),
                    d.getPrecio(),
                    subtotal,
                    d.getSaldo(),
                    d.getFecha(),
                    d.getTipo()
            ));
        }

        return new EgresoResponse(
                egreso.getId(),
                egreso.getCliente() != null ? egreso.getCliente().getId() : null,
                egreso.getCliente() != null ? egreso.getCliente().getNombre() : null,
                egreso.getEncargado() != null ? egreso.getEncargado().getId() : null,
                egreso.getEncargado() != null ? egreso.getEncargado().getNombreCompleto() : null,
                egreso.getArea() != null ? egreso.getArea().getId() : null,
                egreso.getArea() != null ? egreso.getArea().getNombre() : null,
                egreso.getEncargadoAlmacen() != null ? egreso.getEncargadoAlmacen().getId() : null,
                egreso.getEncargadoAlmacen() != null ? egreso.getEncargadoAlmacen().getNombre() : null,
                egreso.getAmbiente(),
                egreso.getPrefijo(),
                egreso.getCorrelativo(),
                egreso.getNumeroCompleto(),
                egreso.getFecha(),
                egreso.getEstado(),
                totalEgreso,
                detallesResponse
        );
    }

    private EgresoResponse construirEgresoResumenResponse(Egreso egreso, BigDecimal total) {
        return new EgresoResponse(
                egreso.getId(),
                egreso.getCliente() != null ? egreso.getCliente().getId() : null,
                egreso.getCliente() != null ? egreso.getCliente().getNombre() : null,
                egreso.getEncargado() != null ? egreso.getEncargado().getId() : null,
                egreso.getEncargado() != null ? egreso.getEncargado().getNombreCompleto() : null,
                egreso.getArea() != null ? egreso.getArea().getId() : null,
                egreso.getArea() != null ? egreso.getArea().getNombre() : null,
                egreso.getEncargadoAlmacen() != null ? egreso.getEncargadoAlmacen().getId() : null,
                egreso.getEncargadoAlmacen() != null ? egreso.getEncargadoAlmacen().getNombre() : null,
                egreso.getAmbiente(),
                egreso.getPrefijo(),
                egreso.getCorrelativo(),
                egreso.getNumeroCompleto(),
                egreso.getFecha(),
                egreso.getEstado(),
                total,
                List.of()
        );
    }

    private Usuario obtenerUsuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetailsImpl userDetails) {
            return usuarioRepository.findById(userDetails.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado en la base de datos"));
        }
        throw new BusinessException("Operación no permitida: No se encontró un usuario autenticado válido en la sesión de seguridad");
    }

    private BigDecimal extraerMontoSeguro(Object valor) {
        if (valor == null) return BigDecimal.ZERO;
        if (valor instanceof BigDecimal bd) return bd;
        if (valor instanceof Number num) return BigDecimal.valueOf(num.doubleValue());
        return new BigDecimal(valor.toString());
    }
}
