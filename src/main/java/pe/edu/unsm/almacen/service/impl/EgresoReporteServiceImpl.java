package pe.edu.unsm.almacen.service.impl;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfGState;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.unsm.almacen.entity.DetalleEgreso;
import pe.edu.unsm.almacen.entity.Egreso;
import pe.edu.unsm.almacen.entity.Encargado;
import pe.edu.unsm.almacen.exception.BusinessException;
import pe.edu.unsm.almacen.exception.ResourceNotFoundException;
import pe.edu.unsm.almacen.repository.DetalleEgresoRepository;
import pe.edu.unsm.almacen.repository.EgresoRepository;
import pe.edu.unsm.almacen.repository.EncargadoRepository;
import pe.edu.unsm.almacen.service.IEgresoReporteService;

/**
 * Servicio encargado de generar el comprobante de autorización de salida de materiales
 * en formato PDF, siguiendo la estructura, márgenes y tipografía institucional del documento modelo.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EgresoReporteServiceImpl implements IEgresoReporteService {

    private final EgresoRepository egresoRepository;
    private final DetalleEgresoRepository detalleEgresoRepository;
    private final EncargadoRepository encargadoRepository;

    private static final String LOGO_PATH = "/images/logo_unsm_doc.png";

    // --- Tipografías (Times New Roman institucional) ---
    private static final Font FONT_HEADER_TITLE = FontFactory.getFont(FontFactory.TIMES_BOLD, 14f, Color.BLACK);
    private static final Font FONT_HEADER_SUBTITLE = FontFactory.getFont(FontFactory.TIMES_BOLD, 10f, Color.BLACK);
    private static final Font FONT_HEADER_DIR = FontFactory.getFont(FontFactory.TIMES_ROMAN, 9f, Color.BLACK);
    private static final Font FONT_HEADER_EMAIL = FontFactory.getFont(FontFactory.TIMES_ROMAN, 9f, new Color(5, 99, 193));
    private static final Font FONT_NUM_SALIDA = FontFactory.getFont(FontFactory.TIMES_BOLD, 11f, Color.BLACK);
    private static final Font FONT_TITULO_DOC = FontFactory.getFont(FontFactory.TIMES_BOLD, 14f, Color.BLACK);
    private static final Font FONT_TABLA_HEAD = FontFactory.getFont(FontFactory.TIMES_BOLD, 9.5f, Color.BLACK);
    private static final Font FONT_TABLA_BODY = FontFactory.getFont(FontFactory.TIMES_ROMAN, 9f, Color.BLACK);
    private static final Font FONT_TEXTO_GENERAL = FontFactory.getFont(FontFactory.TIMES_ROMAN, 9.5f, Color.BLACK);
    private static final Font FONT_FIRMA_NOMBRE = FontFactory.getFont(FontFactory.TIMES_BOLD, 8.5f, Color.BLACK);
    private static final Font FONT_FIRMA_CARGO = FontFactory.getFont(FontFactory.TIMES_ROMAN, 8.5f, Color.BLACK);

    @Override
    @Transactional(readOnly = true)
    public byte[] generarReportePdf(Integer idEgreso) {
        Egreso egreso = egresoRepository.findById(idEgreso)
                .orElseThrow(() -> new ResourceNotFoundException("Egreso no encontrado con ID: " + idEgreso));

        List<DetalleEgreso> detalles = detalleEgresoRepository.findByEgreso_IdOrderByIdAsc(idEgreso);

        try {
            return construirPdf(egreso, detalles);
        } catch (Exception e) {
            log.error("Error al generar el reporte PDF para el egreso {}: {}", idEgreso, e.getMessage(), e);
            throw new BusinessException("No fue posible generar el reporte PDF del egreso: " + e.getMessage());
        }
    }

    private byte[] construirPdf(Egreso egreso, List<DetalleEgreso> detalles) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // Márgenes exactos en puntos: izquierda/derecha: 56.64f (2 cm), superior: 35.45f, inferior: 40f
        Document document = new Document(PageSize.A4, 56.64f, 56.64f, 35.45f, 40f);
        PdfWriter writer = PdfWriter.getInstance(document, baos);

        if ("0".equals(egreso.getEstado())) {
            writer.setPageEvent(new WatermarkPageEvent());
        }

        // Metadatos para el visor del navegador
        configurarMetadatos(document, egreso);

        document.open();

        // 1. Logo institucional en la esquina superior izquierda
        agregarLogo(document);

        // 2. Encabezado institucional centrado debajo del logo
        agregarEncabezado(document, egreso);

        // 3. Texto de Autorización y datos de destino (Área y Ambiente)
        agregarEspacio(document, 4f);
        agregarDatosAutorizacion(document, egreso);

        // 4. Tabla de Artículos
        agregarEspacio(document, 8f);
        agregarTablaDetalles(document, detalles);

        // 5. Conformidad y Fecha de emisión
        agregarEspacio(document, 14f);
        agregarConformidadYFecha(document, egreso.getFecha());

        // 6. Bloque de 3 firmas simétrico (Jefe USG, Encargado de Almacén, Encargado del Área / Cliente)
        agregarEspacio(document, 28f);
        agregarBloqueFirmas(document, egreso);
        agregarEspacio(document, 20f);

        document.close();
        return baos.toByteArray();
    }

    private void configurarMetadatos(Document document, Egreso egreso) {
        String numDoc = egreso.getNumeroCompleto() != null ? egreso.getNumeroCompleto() : ("#" + egreso.getId());
        document.addTitle("Salida " + numDoc);
        document.addSubject("Autorización de Salida de Materiales - UNSM");
        document.addAuthor("Universidad Nacional de San Martín");
        document.addCreator("Sistema de Gestión de Almacén - UNSM");
    }

    private void agregarLogo(Document document) {
        try (InputStream is = getClass().getResourceAsStream(LOGO_PATH)) {
            if (is != null) {
                Image logo = Image.getInstance(is.readAllBytes());
                logo.scaleAbsolute(154.03f, 59.5f);
                logo.setAlignment(Element.ALIGN_LEFT);
                logo.setSpacingAfter(8f);
                document.add(logo);
            } else {
                log.warn("No se encontró el logo institucional en: {}", LOGO_PATH);
            }
        } catch (Exception e) {
            log.warn("No se pudo cargar el logo institucional: {}", e.getMessage());
        }
    }

    private void agregarEncabezado(Document document, Egreso egreso) throws Exception {
        Paragraph pUniv = new Paragraph("UNIVERSIDAD NACIONAL DE SAN MARTIN", FONT_HEADER_TITLE);
        pUniv.setAlignment(Element.ALIGN_CENTER);
        pUniv.setSpacingAfter(3f);
        document.add(pUniv);

        Paragraph pArea = new Paragraph("OFICINA DE LA UNIDAD DE SERVICIOS GENERALES", FONT_HEADER_SUBTITLE);
        pArea.setAlignment(Element.ALIGN_CENTER);
        pArea.setSpacingAfter(3f);
        document.add(pArea);

        Paragraph pDir = new Paragraph();
        pDir.setAlignment(Element.ALIGN_CENTER);
        pDir.add(new Chunk("Jr.Tiwinza 2da. cuadra Villa Universitaria - (TEJACRETO) ", FONT_HEADER_DIR));
        Chunk emailChunk = new Chunk("osgr@unsm.edu.pe", FONT_HEADER_EMAIL);
        emailChunk.setUnderline(0.8f, -1.5f);
        pDir.add(emailChunk);
        pDir.setSpacingAfter(8f);
        document.add(pDir);

        String numCompleto = egreso.getNumeroCompleto() != null ? egreso.getNumeroCompleto() : "Exx-00xx";
        Paragraph pNum = new Paragraph("Número de Salida: " + numCompleto, FONT_NUM_SALIDA);
        pNum.setAlignment(Element.ALIGN_CENTER);
        pNum.setSpacingAfter(8f);
        document.add(pNum);

        Paragraph pTitulo = new Paragraph("AUTORIZACIÓN DE SALIDA DE MATERIALES", FONT_TITULO_DOC);
        pTitulo.setAlignment(Element.ALIGN_CENTER);
        pTitulo.setSpacingAfter(14f);
        document.add(pTitulo);
    }

    private void agregarDatosAutorizacion(Document document, Egreso egreso) throws Exception {
        String nombreArea = (egreso.getArea() != null && egreso.getArea().getNombre() != null)
                ? egreso.getArea().getNombre().trim() : "No especificada";
        String ambiente = (egreso.getAmbiente() != null && !egreso.getAmbiente().isBlank())
                ? egreso.getAmbiente().trim() : "No especificado";

        Paragraph pAutorizacion = new Paragraph();
        pAutorizacion.setAlignment(Element.ALIGN_JUSTIFIED);
        pAutorizacion.setLeading(14f);
        pAutorizacion.add(new Chunk("El jefe de la Unidad de Servicios Generales de la UNSM, autoriza la salida de materiales para el área de: " + nombreArea, FONT_TEXTO_GENERAL));
        pAutorizacion.setSpacingAfter(4f);
        document.add(pAutorizacion);

        Paragraph pAmbiente = new Paragraph();
        pAmbiente.setAlignment(Element.ALIGN_JUSTIFIED);
        pAmbiente.setLeading(14f);
        pAmbiente.add(new Chunk("Para ser utilizado en los ambientes de: " + ambiente, FONT_TEXTO_GENERAL));
        pAmbiente.setSpacingAfter(8f);
        document.add(pAmbiente);
    }

    private void agregarTablaDetalles(Document document, List<DetalleEgreso> detalles) throws Exception {
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100f);
        table.setWidths(new float[]{7f, 16f, 13f, 10f, 54f});

        agregarCeldaCabecera(table, "ITEM", Element.ALIGN_CENTER);
        agregarCeldaCabecera(table, "CÓDIGO", Element.ALIGN_CENTER);
        agregarCeldaCabecera(table, "CANTIDAD", Element.ALIGN_CENTER);
        agregarCeldaCabecera(table, "U.M.", Element.ALIGN_CENTER);
        agregarCeldaCabecera(table, "DESCRIPCIÓN", Element.ALIGN_LEFT);

        int index = 1;
        for (DetalleEgreso det : detalles) {
            String codigo = det.getArticulo() != null ? det.getArticulo().getCodigo() : "-";
            String cantidad = formatearCantidad(det);
            String um = "-";
            if (det.getArticulo() != null && det.getArticulo().getUnidadMedida() != null) {
                if (det.getArticulo().getUnidadMedida().getSimbolo() != null && !det.getArticulo().getUnidadMedida().getSimbolo().isBlank()) {
                    um = det.getArticulo().getUnidadMedida().getSimbolo().trim().toUpperCase();
                } else if (det.getArticulo().getUnidadMedida().getNombre() != null && !det.getArticulo().getUnidadMedida().getNombre().isBlank()) {
                    um = det.getArticulo().getUnidadMedida().getNombre().trim().toUpperCase();
                }
            }
            String descripcion = det.getArticulo() != null ? det.getArticulo().getDescripcion() : "-";

            agregarCeldaDato(table, String.valueOf(index++), Element.ALIGN_CENTER);
            agregarCeldaDato(table, codigo, Element.ALIGN_CENTER);
            agregarCeldaDato(table, cantidad, Element.ALIGN_CENTER);
            agregarCeldaDato(table, um, Element.ALIGN_CENTER);
            agregarCeldaDato(table, descripcion, Element.ALIGN_LEFT);
        }

        if (detalles.isEmpty()) {
            PdfPCell cVacia = new PdfPCell(new Phrase("Sin artículos registrados en este egreso", FONT_TABLA_BODY));
            cVacia.setColspan(5);
            cVacia.setBorder(Rectangle.NO_BORDER);
            cVacia.setHorizontalAlignment(Element.ALIGN_CENTER);
            cVacia.setPadding(8f);
            table.addCell(cVacia);
        }

        // Línea horizontal limpia de cierre inferior
        for (int i = 0; i < 5; i++) {
            PdfPCell cCierre = new PdfPCell();
            cCierre.setBorder(Rectangle.TOP);
            cCierre.setBorderWidthTop(0.75f);
            cCierre.setBorderColorTop(Color.GRAY);
            cCierre.setPadding(0);
            cCierre.setFixedHeight(1f);
            table.addCell(cCierre);
        }

        document.add(table);
    }

    private void agregarCeldaCabecera(PdfPTable table, String texto, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, FONT_TABLA_HEAD));
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPaddingTop(5f);
        cell.setPaddingBottom(5f);
        cell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
        cell.setBorderWidthTop(1.4f);
        cell.setBorderWidthBottom(1.4f);
        cell.setBorderColorTop(Color.BLACK);
        cell.setBorderColorBottom(Color.BLACK);
        table.addCell(cell);
    }

    private void agregarCeldaDato(PdfPTable table, String texto, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(texto != null ? texto : "", FONT_TABLA_BODY));
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPaddingTop(4f);
        cell.setPaddingBottom(4f);
        cell.setBorder(Rectangle.NO_BORDER);
        table.addCell(cell);
    }

    private String formatearCantidad(DetalleEgreso det) {
        if (det == null || det.getCantidad() == null) return "0";
        BigDecimal cant = det.getCantidad();
        if (cant.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
            return cant.toBigInteger().toString();
        }
        return cant.stripTrailingZeros().toPlainString();
    }

    private void agregarConformidadYFecha(Document document, LocalDateTime fechaEgreso) throws Exception {
        Paragraph pConformidad = new Paragraph("Para mayor conformidad se firma el presente.", FONT_TEXTO_GENERAL);
        pConformidad.setSpacingAfter(8f);
        document.add(pConformidad);

        String fechaTexto = formatearFechaEspanol(fechaEgreso != null ? fechaEgreso : LocalDateTime.now());
        Paragraph pFecha = new Paragraph("Tarapoto, " + fechaTexto, FONT_TEXTO_GENERAL);
        pFecha.setAlignment(Element.ALIGN_RIGHT);
        pFecha.setSpacingAfter(10f);
        document.add(pFecha);
    }

    private void agregarBloqueFirmas(Document document, Egreso egreso) throws Exception {
        PdfPTable firmasTable = new PdfPTable(5);
        firmasTable.setWidthPercentage(95f);
        firmasTable.setHorizontalAlignment(Element.ALIGN_CENTER);
        firmasTable.setWidths(new float[]{30f, 5f, 30f, 5f, 30f});
        firmasTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        firmasTable.setSpacingAfter(20f);

        // 1. Jefe USG - UNSM
        String nombreJefe = "";
        if (egreso.getEncargado() != null && egreso.getEncargado().getNombreCompleto() != null) {
            nombreJefe = egreso.getEncargado().getNombreCompleto().trim().toUpperCase();
        } else if (egreso.getNombreEncargadoLibre() != null && !egreso.getNombreEncargadoLibre().isBlank()) {
            nombreJefe = egreso.getNombreEncargadoLibre().trim().toUpperCase();
        } else {
            Encargado jefeDefecto = encargadoRepository.findByEstadoOrderByApellidosAsc("1").stream().findFirst().orElse(null);
            if (jefeDefecto != null && jefeDefecto.getNombreCompleto() != null) {
                nombreJefe = jefeDefecto.getNombreCompleto().trim().toUpperCase();
            }
        }

        // 2. Encargado de Almacén
        String nombreEncargadoAlmacen = "";
        if (egreso.getEncargadoAlmacen() != null && egreso.getEncargadoAlmacen().getNombre() != null) {
            nombreEncargadoAlmacen = egreso.getEncargadoAlmacen().getNombre().trim().toUpperCase();
        } else if (egreso.getUsuario() != null && egreso.getUsuario().getNombreCompleto() != null) {
            nombreEncargadoAlmacen = egreso.getUsuario().getNombreCompleto().trim().toUpperCase();
        }

        // 3. Encargado del Área (Cliente Receptor)
        String nombreEncargadoArea = "";
        if (egreso.getCliente() != null && egreso.getCliente().getNombre() != null) {
            nombreEncargadoArea = egreso.getCliente().getNombre().trim().toUpperCase();
        }

        // Casilla 1: Jefe USG-UNSM
        firmasTable.addCell(crearCeldaFirma(nombreJefe, "Jefe USG - UNSM"));

        // Casilla 2: Espacio separador
        PdfPCell cGap1 = new PdfPCell();
        cGap1.setBorder(Rectangle.NO_BORDER);
        firmasTable.addCell(cGap1);

        // Casilla 3: Encargado del Almacén
        firmasTable.addCell(crearCeldaFirma(nombreEncargadoAlmacen, "Encargado de Almacén"));

        // Casilla 4: Espacio separador
        PdfPCell cGap2 = new PdfPCell();
        cGap2.setBorder(Rectangle.NO_BORDER);
        firmasTable.addCell(cGap2);

        // Casilla 5: Encargado del Área (Cliente)
        firmasTable.addCell(crearCeldaFirma(nombreEncargadoArea, "Encargado del Área"));

        document.add(firmasTable);
    }

    private PdfPCell crearCeldaFirma(String nombre, String cargo) {
        PdfPTable inner = new PdfPTable(1);
        inner.setWidthPercentage(90f);
        inner.setHorizontalAlignment(Element.ALIGN_CENTER);

        // Línea horizontal superior
        PdfPCell linea = new PdfPCell();
        linea.setBorder(Rectangle.TOP);
        linea.setBorderWidthTop(0.85f);
        linea.setBorderColorTop(Color.BLACK);
        linea.setFixedHeight(2f);
        inner.addCell(linea);

        // Nombre centrado debajo de la línea
        String textoNombre = (nombre != null && !nombre.isBlank()) ? nombre.trim().toUpperCase() : " ";
        PdfPCell cNom = new PdfPCell(new Phrase(textoNombre, FONT_FIRMA_NOMBRE));
        cNom.setBorder(Rectangle.NO_BORDER);
        cNom.setHorizontalAlignment(Element.ALIGN_CENTER);
        cNom.setPaddingTop(4f);
        inner.addCell(cNom);

        // Cargo centrado debajo del nombre
        PdfPCell cCargo = new PdfPCell(new Phrase(cargo, FONT_FIRMA_CARGO));
        cCargo.setBorder(Rectangle.NO_BORDER);
        cCargo.setHorizontalAlignment(Element.ALIGN_CENTER);
        cCargo.setPaddingTop(2f);
        cCargo.setPaddingBottom(12f);
        inner.addCell(cCargo);

        PdfPCell outer = new PdfPCell(inner);
        outer.setBorder(Rectangle.NO_BORDER);
        outer.setHorizontalAlignment(Element.ALIGN_CENTER);
        outer.setVerticalAlignment(Element.ALIGN_TOP);
        outer.setPaddingBottom(16f);
        return outer;
    }

    private String formatearFechaEspanol(LocalDateTime fecha) {
        int dia = fecha.getDayOfMonth();
        int anio = fecha.getYear();
        String[] meses = {"", "enero", "febrero", "marzo", "abril", "mayo", "junio",
                "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"};
        int mesIndex = Math.clamp(fecha.getMonthValue(), 1, 12);
        return dia + " de " + meses[mesIndex] + " de " + anio;
    }

    private void agregarEspacio(Document document, float puntos) throws Exception {
        document.add(new Paragraph(" ", FontFactory.getFont(FontFactory.TIMES_ROMAN, puntos)));
    }

    /**
     * Marca de agua diagonal "ANULADO" visible en caso de anulación del comprobante.
     */
    private static class WatermarkPageEvent extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContentUnder();
            cb.saveState();
            PdfGState gs = new PdfGState();
            gs.setFillOpacity(0.18f);
            cb.setGState(gs);
            cb.setColorFill(new Color(220, 38, 38));

            Font fontMarca = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 68);
            cb.beginText();
            cb.setFontAndSize(fontMarca.getBaseFont(), 68);
            float x = (document.left() + document.right()) / 2;
            float y = (document.top() + document.bottom()) / 2;
            cb.showTextAligned(Element.ALIGN_CENTER, "ANULADO", x, y, 45);
            cb.endText();
            cb.restoreState();
        }
    }
}
