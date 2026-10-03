package pe.edu.unsm.almacen.service;

public interface IIngresoReporteService {

    /**
     * Genera el reporte del comprobante de ingreso en formato PDF.
     *
     * @param idIngreso Identificador del ingreso
     * @return Arreglo de bytes correspondiente al archivo PDF generado
     */
    byte[] generarReportePdf(Integer idIngreso);
}
