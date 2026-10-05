package pe.edu.unsm.almacen.service;

public interface IEgresoReporteService {

    /**
     * Genera el reporte del comprobante de egreso (autorización de salida) en formato PDF.
     *
     * @param idEgreso Identificador del egreso
     * @return Arreglo de bytes correspondiente al archivo PDF generado
     */
    byte[] generarReportePdf(Integer idEgreso);
}
