package pe.edu.unsm.almacen.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import pe.edu.unsm.almacen.dto.request.DetalleEgresoRequest;
import pe.edu.unsm.almacen.dto.request.DetalleItemRequest;
import pe.edu.unsm.almacen.dto.request.EgresoCreateRequest;
import pe.edu.unsm.almacen.dto.request.IngresoCreateRequest;
import pe.edu.unsm.almacen.entity.Area;
import pe.edu.unsm.almacen.entity.Articulo;
import pe.edu.unsm.almacen.entity.Cliente;
import pe.edu.unsm.almacen.entity.DetalleEgreso;
import pe.edu.unsm.almacen.entity.DetalleIngreso;
import pe.edu.unsm.almacen.entity.Egreso;
import pe.edu.unsm.almacen.entity.Ingreso;
import pe.edu.unsm.almacen.entity.KardexMovimiento;
import pe.edu.unsm.almacen.entity.EncargadoAlmacen;
import pe.edu.unsm.almacen.entity.Proveedor;
import pe.edu.unsm.almacen.entity.TipoMovimiento;
import pe.edu.unsm.almacen.entity.UnidadMedida;
import pe.edu.unsm.almacen.entity.Usuario;
import pe.edu.unsm.almacen.exception.BusinessException;
import pe.edu.unsm.almacen.exception.StockInsuficienteException;
import pe.edu.unsm.almacen.repository.AreaRepository;
import pe.edu.unsm.almacen.repository.ArticuloRepository;
import pe.edu.unsm.almacen.repository.ClienteRepository;
import pe.edu.unsm.almacen.repository.DetalleEgresoRepository;
import pe.edu.unsm.almacen.repository.DetalleIngresoRepository;
import pe.edu.unsm.almacen.repository.EgresoRepository;
import pe.edu.unsm.almacen.repository.EncargadoAlmacenRepository;
import pe.edu.unsm.almacen.repository.EncargadoRepository;
import pe.edu.unsm.almacen.repository.IngresoRepository;
import pe.edu.unsm.almacen.repository.KardexMovimientoRepository;
import pe.edu.unsm.almacen.repository.ProveedorRepository;
import pe.edu.unsm.almacen.repository.UsuarioRepository;
import pe.edu.unsm.almacen.security.service.UserDetailsImpl;

@ExtendWith(MockitoExtension.class)
class MovimientosServiceTest {

    @Mock private IngresoRepository ingresoRepository;
    @Mock private EgresoRepository egresoRepository;
    @Mock private DetalleIngresoRepository detalleIngresoRepository;
    @Mock private DetalleEgresoRepository detalleEgresoRepository;
    @Mock private ArticuloRepository articuloRepository;
    @Mock private ProveedorRepository proveedorRepository;
    @Mock private ClienteRepository clienteRepository;
    @Mock private EncargadoRepository encargadoRepository;
    @Mock private AreaRepository areaRepository;
    @Mock private EncargadoAlmacenRepository encargadoAlmacenRepository;
    @Mock private KardexMovimientoRepository kardexMovimientoRepository;
    @Mock private UsuarioRepository usuarioRepository;

    @InjectMocks private IngresoServiceImpl ingresoService;
    @InjectMocks private EgresoServiceImpl egresoService;

    @BeforeEach
    void autenticar() {
        var usuario = Usuario.builder().idUsuario(1).nombre("Ana").apellido("Pérez").build();
        var principal = new UserDetailsImpl(1, "Ana", "Pérez", "ana", "", "USUARIO", false,
                true, List.of(new SimpleGrantedAuthority("ROLE_USUARIO")));
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.getAuthorities()));
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
    }

    @AfterEach
    void limpiarSesion() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void ingresoSumaExistenciasYRegistraKardex() {
        Articulo articulo = articuloConCincoUnidades();
        when(proveedorRepository.findById(1))
                .thenReturn(Optional.of(Proveedor.builder().id(1).razonSocial("Proveedor").build()));
        when(articuloRepository.findByIdWithLock(10)).thenReturn(Optional.of(articulo));
        when(ingresoRepository.save(any(Ingreso.class))).thenAnswer(invocation -> {
            Ingreso ingreso = invocation.getArgument(0);
            ingreso.setId(20);
            return ingreso;
        });
        when(detalleIngresoRepository.save(any(DetalleIngreso.class))).thenAnswer(invocation -> {
            DetalleIngreso detalle = invocation.getArgument(0);
            detalle.setId(30);
            return detalle;
        });

        var respuesta = ingresoService.registrar(new IngresoCreateRequest(1, "OC-1", "Compra",
                List.of(new DetalleItemRequest(10, new BigDecimal("2.00"), new BigDecimal("3.50")))));

        assertEquals(new BigDecimal("7.00"), articulo.getSaldo());
        assertEquals(new BigDecimal("7.00"), respuesta.total());
        var movimiento = ArgumentCaptor.forClass(KardexMovimiento.class);
        verify(kardexMovimientoRepository).save(movimiento.capture());
        assertEquals(TipoMovimiento.INGRESO, movimiento.getValue().getTipoMovimiento());
        assertEquals(new BigDecimal("7.00"), movimiento.getValue().getSaldoResultante());
    }

    @Test
    void egresoDescuentaExistenciasYRegistraKardex() {
        Articulo articulo = articuloConCincoUnidades();
        prepararEgreso(articulo);
        when(detalleEgresoRepository.save(any(DetalleEgreso.class))).thenAnswer(invocation -> {
            DetalleEgreso detalle = invocation.getArgument(0);
            detalle.setId(40);
            return detalle;
        });

        var respuesta = egresoService.registrar(solicitudEgreso("2.00"));

        assertEquals(new BigDecimal("3.00"), articulo.getSaldo());
        assertEquals(new BigDecimal("8.00"), respuesta.total());
        var movimiento = ArgumentCaptor.forClass(KardexMovimiento.class);
        verify(kardexMovimientoRepository).save(movimiento.capture());
        assertEquals(TipoMovimiento.EGRESO, movimiento.getValue().getTipoMovimiento());
        assertEquals(new BigDecimal("3.00"), movimiento.getValue().getSaldoResultante());
    }

    @Test
    void egresoRechazaCantidadMayorAlStock() {
        Articulo articulo = articuloConCincoUnidades();
        prepararEgreso(articulo);

        assertThrows(StockInsuficienteException.class, () -> egresoService.registrar(solicitudEgreso("6.00")));

        assertEquals(new BigDecimal("5.00"), articulo.getSaldo());
        verify(detalleEgresoRepository, never()).save(any());
        verify(kardexMovimientoRepository, never()).save(any());
    }

    @Test
    void anulacionDevuelveExistenciasYRegistraReverso() {
        Articulo articulo = articuloConCincoUnidades();
        Egreso egreso = Egreso.builder().id(21).prefijo("EGR").correlativo(1).estado("1").build();
        DetalleEgreso detalle = DetalleEgreso.builder().articulo(articulo)
                .cantidad(new BigDecimal("2.00")).precio(new BigDecimal("4.00")).build();
        when(egresoRepository.findByIdWithLock(21)).thenReturn(Optional.of(egreso));
        when(detalleEgresoRepository.findByEgreso_IdOrderByIdAsc(21)).thenReturn(List.of(detalle));
        when(articuloRepository.findByIdWithLock(10)).thenReturn(Optional.of(articulo));

        var respuesta = egresoService.anular(21);

        assertEquals("0", respuesta.estado());
        assertEquals(new BigDecimal("7.00"), articulo.getSaldo());
        var movimiento = ArgumentCaptor.forClass(KardexMovimiento.class);
        verify(kardexMovimientoRepository).save(movimiento.capture());
        assertEquals(TipoMovimiento.REVERSO_EGRESO, movimiento.getValue().getTipoMovimiento());
        assertEquals(new BigDecimal("7.00"), movimiento.getValue().getSaldoResultante());
    }

    @Test
    void articuloPorUnidadRechazaSalidaDecimal() {
        Articulo articulo = articuloConCincoUnidades();
        articulo.setUnidadMedida(UnidadMedida.builder().permiteDecimales(false).build());
        prepararEgreso(articulo);

        assertThrows(BusinessException.class, () -> egresoService.registrar(solicitudEgreso("1.50")));

        assertEquals(new BigDecimal("5.00"), articulo.getSaldo());
        verify(detalleEgresoRepository, never()).save(any());
        verify(kardexMovimientoRepository, never()).save(any());
    }

    private void prepararEgreso(Articulo articulo) {
        when(clienteRepository.findById(1)).thenReturn(Optional.of(Cliente.builder().id(1).nombre("Cliente").build()));
        when(areaRepository.findById(2)).thenReturn(Optional.of(Area.builder().id(2).nombre("Área").build()));
        when(encargadoAlmacenRepository.findById(3))
                .thenReturn(Optional.of(EncargadoAlmacen.builder().id(3).nombre("Almacenero").build()));
        when(articuloRepository.findByIdWithLock(10)).thenReturn(Optional.of(articulo));
        when(egresoRepository.saveAndFlush(any(Egreso.class))).thenAnswer(invocation -> {
            Egreso egreso = invocation.getArgument(0);
            egreso.setId(21);
            return egreso;
        });
    }

    private Articulo articuloConCincoUnidades() {
        return Articulo.builder().id(10).codigo("ART-10").descripcion("Artículo")
                .saldo(new BigDecimal("5.00")).precio(new BigDecimal("4.00"))
                .estado("1").activo(true).build();
    }

    private EgresoCreateRequest solicitudEgreso(String cantidad) {
        return new EgresoCreateRequest(1, null, "Responsable", null, 2, 3, "Aula", "EGR",
                List.of(new DetalleEgresoRequest(10, new BigDecimal(cantidad))));
    }
}
