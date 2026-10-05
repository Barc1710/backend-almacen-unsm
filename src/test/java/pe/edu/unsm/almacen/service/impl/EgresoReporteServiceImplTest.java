package pe.edu.unsm.almacen.service.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.unsm.almacen.entity.Area;
import pe.edu.unsm.almacen.entity.Articulo;
import pe.edu.unsm.almacen.entity.Cliente;
import pe.edu.unsm.almacen.entity.DetalleEgreso;
import pe.edu.unsm.almacen.entity.Egreso;
import pe.edu.unsm.almacen.entity.Encargado;
import pe.edu.unsm.almacen.entity.EncargadoAlmacen;
import pe.edu.unsm.almacen.entity.TipoEgreso;
import pe.edu.unsm.almacen.entity.UnidadMedida;
import pe.edu.unsm.almacen.repository.DetalleEgresoRepository;
import pe.edu.unsm.almacen.repository.EgresoRepository;
import pe.edu.unsm.almacen.repository.EncargadoRepository;

@ExtendWith(MockitoExtension.class)
class EgresoReporteServiceImplTest {

    @Mock
    private EgresoRepository egresoRepository;

    @Mock
    private DetalleEgresoRepository detalleEgresoRepository;

    @Mock
    private EncargadoRepository encargadoRepository;

    @InjectMocks
    private EgresoReporteServiceImpl egresoReporteService;

    @Test
    void generarReportePdf_retornaPdfValidoConTresFirmantes() {
        Area area = Area.builder().id(1).nombre("FACULTAD DE INGENIERIA").build();
        Cliente cliente = Cliente.builder().id(1).nombre("ING. CARLOS SANCHEZ").build();
        Encargado jefe = Encargado.builder().id(1).nombres("JUAN").apellidos("PEREZ").build();
        EncargadoAlmacen encargadoAlmacen = EncargadoAlmacen.builder().id(1).nombre("MARIO ALMACENERO").build();
        UnidadMedida um = UnidadMedida.builder().simbolo("UND").build();
        Articulo articulo = Articulo.builder().id(10).codigo("ART-001").descripcion("PAPEL BOND A4").unidadMedida(um).build();

        Egreso egreso = Egreso.builder()
                .id(100)
                .prefijo("E26")
                .correlativo(1)
                .area(area)
                .cliente(cliente)
                .encargado(jefe)
                .encargadoAlmacen(encargadoAlmacen)
                .ambiente("AULA 101 - PABELLON CENTRAL")
                .tipoEgreso(TipoEgreso.DESPACHO_ORDINARIO)
                .fecha(LocalDateTime.of(2026, 10, 3, 10, 30))
                .estado("1")
                .build();

        DetalleEgreso det = DetalleEgreso.builder()
                .id(501)
                .articulo(articulo)
                .cantidad(new BigDecimal("10"))
                .precio(new BigDecimal("25.00"))
                .build();

        when(egresoRepository.findById(100)).thenReturn(Optional.of(egreso));
        when(detalleEgresoRepository.findByEgreso_IdOrderByIdAsc(100)).thenReturn(List.of(det));

        byte[] pdf = egresoReporteService.generarReportePdf(100);

        assertNotNull(pdf);
        assertTrue(pdf.length > 500);
        // Validar que el archivo inicie con la cabecera mágica de PDF (%PDF)
        String header = new String(pdf, 0, 4);
        assertTrue(header.startsWith("%PDF"));
    }

    @Test
    void generarReportePdf_soportaEgresoAnuladoConMarcaDeAgua() {
        Area area = Area.builder().id(1).nombre("OFICINA DE SERVICIOS").build();
        Cliente cliente = Cliente.builder().id(1).nombre("LUCIA RAMIREZ").build();
        EncargadoAlmacen encargadoAlmacen = EncargadoAlmacen.builder().id(1).nombre("MARIO ALMACENERO").build();

        Egreso egreso = Egreso.builder()
                .id(101)
                .prefijo("E26")
                .correlativo(2)
                .area(area)
                .cliente(cliente)
                .encargadoAlmacen(encargadoAlmacen)
                .ambiente("DEPOSITO CENTRAL")
                .tipoEgreso(TipoEgreso.BAJA_DETERIORO)
                .fecha(LocalDateTime.now())
                .estado("0") // ANULADO
                .build();

        when(egresoRepository.findById(101)).thenReturn(Optional.of(egreso));
        when(detalleEgresoRepository.findByEgreso_IdOrderByIdAsc(101)).thenReturn(List.of());
        when(encargadoRepository.findByEstadoOrderByApellidosAsc("1")).thenReturn(List.of(
                Encargado.builder().nombres("DIRECTOR").apellidos("GENERAL").build()
        ));

        byte[] pdf = egresoReporteService.generarReportePdf(101);

        assertNotNull(pdf);
        assertTrue(pdf.length > 500);
        String header = new String(pdf, 0, 4);
        assertTrue(header.startsWith("%PDF"));
    }
}
