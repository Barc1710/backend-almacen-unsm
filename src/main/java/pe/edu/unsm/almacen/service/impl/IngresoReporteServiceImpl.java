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
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.unsm.almacen.entity.DetalleIngreso;
import pe.edu.unsm.almacen.entity.Ingreso;
import pe.edu.unsm.almacen.exception.BusinessException;
import pe.edu.unsm.almacen.exception.ResourceNotFoundException;
import pe.edu.unsm.almacen.repository.DetalleIngresoRepository;
import pe.edu.unsm.almacen.repository.IngresoRepository;
import pe.edu.unsm.almacen.service.IIngresoReporteService;

/**
 * Servicio encargado de generar el comprobante de ingreso de materiales en formato PDF,
 * siguiendo la estructura, márgenes y tipografía institucional del documento modelo.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IngresoReporteServiceImpl implements IIngresoReporteService {

    private final IngresoRepository ingresoRepository;
    private final DetalleIngresoRepository detalleIngresoRepository;

    private static final String LOGO_PATH = "/images/logo_unsm_doc.png";
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // --- Tipografías (Times New Roman institucional) ---
    private static final Font FONT_HEADER_TITLE = FontFactory.getFont(FontFactory.TIMES_BOLD, 14f, Color.BLACK);
    private static final Font FONT_HEADER_SUBTITLE = FontFactory.getFont(FontFactory.TIMES_BOLD, 10f, Color.BLACK);
    private static final Font FONT_HEADER_DIR = FontFactory.getFont(FontFactory.TIMES_ROMAN, 9f, Color.BLACK);
    private static final Font FONT_HEADER_EMAIL = FontFactory.getFont(FontFactory.TIMES_ROMAN, 9f, new Color(5, 99, 193));
    private static final Font FONT_NUM_INGRESO = FontFactory.getFont(FontFactory.TIMES_BOLD, 11f, Color.BLACK);
    private static final Font FONT_TITULO_DOC = FontFactory.getFont(FontFactory.TIMES_BOLD, 14f, Color.BLACK);
    private static final Font FONT_META_LABEL = FontFactory.getFont(FontFactory.TIMES_BOLD, 10f, Color.BLACK);
    private static final Font FONT_META_VALUE = FontFactory.getFont(FontFactory.TIMES_ROMAN, 10f, Color.BLACK);
    private static final Font FONT_TABLA_HEAD = FontFactory.getFont(FontFactory.TIMES_BOLD, 9.5f, Color.BLACK);
    private static final Font FONT_TABLA_BODY = FontFactory.getFont(FontFactory.TIMES_ROMAN, 9f, Color.BLACK);
    private static final Font FONT_TEXTO_GENERAL = FontFactory.getFont(FontFactory.TIMES_ROMAN, 9.5f, Color.BLACK);

    @Override
    @Transactional(readOnly = true)
    public byte[] generarReportePdf(Integer idIngreso) {
        Ingreso ingreso = ingresoRepository.findById(idIngreso)
                .orElseThrow(() -> new ResourceNotFoundException("Ingreso no encontrado con ID: " + idIngreso));

        List<DetalleIngreso> detalles = detalleIngresoRepository.findByIngreso_IdOrderByIdAsc(idIngreso);

        try {
            return construirPdf(ingreso, detalles);
        } catch (Exception e) {
            log.error("Error al generar el reporte PDF para el ingreso {}: {}", idIngreso, e.getMessage(), e);
            throw new BusinessException("No fue posible generar el reporte PDF del ingreso: " + e.getMessage());
        }
    }

    private byte[] construirPdf(Ingreso ingreso, List<DetalleIngreso> detalles) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // Márgenes exactos en puntos: izquierda/derecha: 56.64f (2 cm), superior: 35.45f, inferior: 40f
        Document document = new Document(PageSize.A4, 56.64f, 56.64f, 35.45f, 40f);
        PdfWriter writer = PdfWriter.getInstance(document, baos);

        if ("0".equals(ingreso.getEstado())) {
            writer.setPageEvent(new WatermarkPageEvent());
        }

        // Metadatos para el visor del navegador
        configurarMetadatos(document, ingreso);

        document.open();

        // 1. Logo institucional en la esquina superior izquierda
        agregarLogo(document);

        // 2. Encabezado institucional centrado debajo del logo
        agregarEncabezado(document, ingreso);

        // 3. Metadatos (Proveedor, Fecha, Referencias)
        agregarEspacio(document, 4f);
        agregarMetadatos(document, ingreso);

        // 4. Tabla de Artículos
        agregarEspacio(document, 6f);
        agregarTablaDetalles(document, detalles);

        // 5. Conformidad y Fecha de emisión
        agregarEspacio(document, 14f);
        agregarConformidadYFecha(document, ingreso.getFecha());

        // 6. Bloque de firmas centrado y simétrico
        agregarEspacio(document, 28f);
        agregarBloqueFirmas(document, ingreso);
        agregarEspacio(document, 20f);

        document.close();
        return baos.toByteArray();
    }

    private void configurarMetadatos(Document document, Ingreso ingreso) {
        String numDoc = ingreso.getNumeroCompleto() != null ? ingreso.getNumeroCompleto() : ("#" + ingreso.getId());
        document.addTitle("Ingreso " + numDoc);
        document.addSubject("Comprobante de Ingreso de Materiales - UNSM");
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

    private void agregarEncabezado(Document document, Ingreso ingreso) throws Exception {
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

        String numCompleto = ingreso.getNumeroCompleto() != null ? ingreso.getNumeroCompleto() : "Ixx-00xx";
        Paragraph pNum = new Paragraph("Número de Ingreso: " + numCompleto, FONT_NUM_INGRESO);
        pNum.setAlignment(Element.ALIGN_CENTER);
        pNum.setSpacingAfter(8f);
        document.add(pNum);

        Paragraph pTitulo = new Paragraph("INGRESO DE MATERIALES", FONT_TITULO_DOC);
        pTitulo.setAlignment(Element.ALIGN_CENTER);
        pTitulo.setSpacingAfter(14f);
        document.add(pTitulo);
    }

    private void agregarMetadatos(Document document, Ingreso ingreso) throws Exception {
        PdfPTable metaTable = new PdfPTable(2);
        metaTable.setWidthPercentage(100f);
        metaTable.setWidths(new float[]{14f, 86f});
        metaTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        // Fila 1: Proveedor
        String nombreProveedor = "";
        if (ingreso.getProveedor() != null) {
            nombreProveedor = ingreso.getProveedor().getRazonSocial();
            if (ingreso.getProveedor().getRuc() != null && !ingreso.getProveedor().getRuc().isBlank()) {
                nombreProveedor += " (RUC: " + ingreso.getProveedor().getRuc() + ")";
            }
        }
        agregarFilaMetadato(metaTable, "Proveedor:", nombreProveedor);

        // Fila 2: Fecha
        String fechaStr = ingreso.getFecha() != null ? ingreso.getFecha().format(FORMATO_FECHA_HORA) : "";
        agregarFilaMetadato(metaTable, "Fecha:", fechaStr);

        // Fila 3: Referencias
        StringBuilder referencias = new StringBuilder();
        if (ingreso.getNumeroOrdenCompra() != null && !ingreso.getNumeroOrdenCompra().isBlank()) {
            referencias.append("O/C: ").append(ingreso.getNumeroOrdenCompra());
        }
        if (ingreso.getDescripcion() != null && !ingreso.getDescripcion().isBlank()) {
            if (referencias.length() > 0) referencias.append(" - ");
            referencias.append(ingreso.getDescripcion());
        }
        agregarFilaMetadato(metaTable, "Referencias:", referencias.toString());

        document.add(metaTable);
    }

    private void agregarFilaMetadato(PdfPTable table, String label, String value) {
        PdfPCell cLabel = new PdfPCell(new Phrase(label, FONT_META_LABEL));
        cLabel.setBorder(Rectangle.NO_BORDER);
        cLabel.setPaddingTop(2f);
        cLabel.setPaddingBottom(3f);
        table.addCell(cLabel);

        PdfPCell cValue = new PdfPCell(new Phrase(value != null ? value : "", FONT_META_VALUE));
        cValue.setBorder(Rectangle.NO_BORDER);
        cValue.setPaddingTop(2f);
        cValue.setPaddingBottom(3f);
        table.addCell(cValue);
    }

    private void agregarTablaDetalles(Document document, List<DetalleIngreso> detalles) throws Exception {
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100f);
        table.setWidths(new float[]{7f, 16f, 13f, 10f, 54f});

        agregarCeldaCabecera(table, "ITEM", Element.ALIGN_CENTER);
        agregarCeldaCabecera(table, "CÓDIGO", Element.ALIGN_CENTER);
        agregarCeldaCabecera(table, "CANTIDAD", Element.ALIGN_CENTER);
        agregarCeldaCabecera(table, "U.M.", Element.ALIGN_CENTER);
        agregarCeldaCabecera(table, "DESCRIPCIÓN", Element.ALIGN_LEFT);

        int index = 1;
        for (DetalleIngreso det : detalles) {
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
            PdfPCell cVacia = new PdfPCell(new Phrase("Sin artículos registrados en este ingreso", FONT_TABLA_BODY));
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

    private String formatearCantidad(DetalleIngreso det) {
        if (det == null || det.getCantidad() == null) return "0";
        BigDecimal cant = det.getCantidad();
        if (cant.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
            return cant.toBigInteger().toString();
        }
        return cant.stripTrailingZeros().toPlainString();
    }

    private void agregarConformidadYFecha(Document document, LocalDateTime fechaIngreso) throws Exception {
        Paragraph pConformidad = new Paragraph("Para mayor conformidad se firma el presente.", FONT_TEXTO_GENERAL);
        pConformidad.setSpacingAfter(8f);
        document.add(pConformidad);

        String fechaTexto = formatearFechaEspanol(fechaIngreso != null ? fechaIngreso : LocalDateTime.now());
        Paragraph pFecha = new Paragraph("Tarapoto, " + fechaTexto, FONT_TEXTO_GENERAL);
        pFecha.setAlignment(Element.ALIGN_RIGHT);
        pFecha.setSpacingAfter(10f);
        document.add(pFecha);
    }

    private void agregarBloqueFirmas(Document document, Ingreso ingreso) throws Exception {
        PdfPTable firmasTable = new PdfPTable(3);
        firmasTable.setWidthPercentage(88f);
        firmasTable.setHorizontalAlignment(Element.ALIGN_CENTER);
        firmasTable.setWidths(new float[]{42f, 16f, 42f});
        firmasTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        firmasTable.setSpacingAfter(20f);

        // 1. Jefe USG-UNSM (inmutable desde snapshot o relación)
        String nombreJefe = "";
        if (ingreso.getNombreJefe() != null && !ingreso.getNombreJefe().isBlank()) {
            nombreJefe = ingreso.getNombreJefe().trim().toUpperCase();
        } else if (ingreso.getJefe() != null && ingreso.getJefe().getNombreCompleto() != null) {
            nombreJefe = ingreso.getJefe().getNombreCompleto().trim().toUpperCase();
        }

        // 2. Encargado de Almacén (inmutable desde snapshot o relación)
        String nombreEncargadoAlmacen = "";
        if (ingreso.getNombreEncargadoAlmacen() != null && !ingreso.getNombreEncargadoAlmacen().isBlank()) {
            nombreEncargadoAlmacen = ingreso.getNombreEncargadoAlmacen().trim().toUpperCase();
        } else if (ingreso.getEncargadoAlmacen() != null && ingreso.getEncargadoAlmacen().getNombre() != null) {
            nombreEncargadoAlmacen = ingreso.getEncargadoAlmacen().getNombre().trim().toUpperCase();
        } else if (ingreso.getUsuario() != null && ingreso.getUsuario().getNombreCompleto() != null) {
            nombreEncargadoAlmacen = ingreso.getUsuario().getNombreCompleto().trim().toUpperCase();
        }

        // Casilla Izquierda: Jefe USG-UNSM
        firmasTable.addCell(crearCeldaFirma(nombreJefe, "Jefe USG-UNSM"));

        // Casilla Central: Espacio en blanco separador
        PdfPCell cGap = new PdfPCell();
        cGap.setBorder(Rectangle.NO_BORDER);
        firmasTable.addCell(cGap);

        // Casilla Derecha: Encargado de Almacén
        firmasTable.addCell(crearCeldaFirma(nombreEncargadoAlmacen, "Encargado de Almacén"));

        document.add(firmasTable);
    }

    private PdfPCell crearCeldaFirma(String nombre, String cargo) {
        PdfPTable inner = new PdfPTable(1);
        inner.setWidthPercentage(85f);
        inner.setHorizontalAlignment(Element.ALIGN_CENTER);

        // Línea horizontal superior
        PdfPCell linea = new PdfPCell();
        linea.setBorder(Rectangle.TOP);
        linea.setBorderWidthTop(0.85f);
        linea.setBorderColorTop(Color.BLACK);
        linea.setFixedHeight(2f);
        inner.addCell(linea);

        // Nombre centrado debajo de la línea (si no hay nombre asignado, espacio en blanco para mantener la simetría)
        String textoNombre = (nombre != null && !nombre.isBlank()) ? nombre : " ";
        PdfPCell cNom = new PdfPCell(new Phrase(textoNombre, FONT_TEXTO_GENERAL));
        cNom.setBorder(Rectangle.NO_BORDER);
        cNom.setHorizontalAlignment(Element.ALIGN_CENTER);
        cNom.setPaddingTop(4f);
        inner.addCell(cNom);

        // Cargo centrado debajo del nombre
        PdfPCell cCargo = new PdfPCell(new Phrase(cargo, FONT_TEXTO_GENERAL));
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
